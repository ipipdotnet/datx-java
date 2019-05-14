package net.ipip.datx;

import java.nio.charset.Charset;
import java.io.IOException;
import java.util.Arrays;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

public class City
{
    private byte[] data;

    private long indexSize;

    public City(String name) throws IOException
    {
        Path path = Paths.get(name);
        data = Files.readAllBytes(path);

        indexSize = Util.bytesToLong(data[0], data[1], data[2], data[3]);
    }

    public String[] find(String ips) throws IPv4FormatException {

        if (!Util.isIPv4Address(ips)) {
            throw new IPv4FormatException();
        }

        long val = Util.ip2long(ips);
        int start = 262148;
        int low = 0;
        int mid = 0;
        int high = new Long((indexSize - 262144 - 262148) / 9).intValue() - 1;
        int pos = 0;
        while (low <= high)
        {
            mid = new Double((low + high) / 2).intValue();
            pos = mid * 9;

            long s = 0;
            if (mid > 0)
            {
                int pos1 = (mid - 1) * 9;
                s = Util.bytesToLong(data[start + pos1], data[start + pos1+1], data[start + pos1+2], data[start + pos1+3]);
            }

            long end = Util.bytesToLong(data[start + pos], data[start + pos+1], data[start + pos+2], data[start + pos+3]);
            if (val > end) {
                low = mid + 1;
            } else if (val <= s) {
                high = mid - 1;
            } else {

                byte b =0;
                long off = Util.bytesToLong(b, data[start+pos+6],data[start+pos+5],data[start+pos+4]);
                long len = Util.bytesToLong(b, b, data[start+pos+7], data[start+pos+8]);

                int offset = new Long(off - 262144 + indexSize).intValue();

                byte[] loc = Arrays.copyOfRange(data, offset, offset+new Long(len).intValue());

                return new String(loc, Charset.forName("UTF-8")).split("\t", -1);
            }
        }

        return null;
    }

    /**
     * Save .mmdb to .csv file
     * CSV file format to "start_address,end_address,country,region,city", eg. 1.2.8.0,1.2.8.255,中国,北京,北京
     *
     * @param csvPath
     * @throws Exception
     */
    public void checkAndStore2CSV(String csvPath) throws Exception {

        FileWriter fw = new FileWriter(new File(csvPath));
        HashSet<String> dedup = new HashSet<String>();
        String header = "start_address,end_address,country,region,city\n";
        dedup.add(header);
        fw.write(header);

        int start = 262148;
        int low = 0;
        int high = new Long((indexSize - 262144 - 262148) / 9).intValue() - 1;
        int pos = 0;
        while (low++ < high) {
            pos = low * 9;
            int pos1 = pos;
            if (low > 0) {
                pos1 = (low - 1) * 9;
            }
            long ipLongBegin = Util.bytesToLong(data[start + pos1], data[start + pos1 + 1], data[start + pos1 + 2], data[start + pos1 + 3]);

            long ipLongEnd = Util.bytesToLong(data[start + pos], data[start + pos + 1], data[start + pos + 2], data[start + pos + 3]);

            byte b = 0;
            long off = Util.bytesToLong(b, data[start + pos + 6], data[start + pos + 5], data[start + pos + 4]);
            long len = Util.bytesToLong(b, b, data[start + pos + 7], data[start + pos + 8]);
            int offset = new Long(off - 262144 + indexSize).intValue();
            byte[] loc = Arrays.copyOfRange(data, offset, offset + new Long(len).intValue());
            String[] split = new String(loc, Charset.forName("UTF-8")).split("\t", -1);

            String ipStringBegin = Util.ipLong2String(ipLongBegin);
            String[] ipStrings = ipStringBegin.split("\\.");
            int i = ipStrings.length - 1;
            while (i >= 0) {
                int intValue = Integer.parseInt(ipStrings[i]);
                int res = (intValue + 1) % 256;
                ipStrings[i] = res + "";
                if (res != 0) {
                    break;
                }
                i--;
            }
            ipStringBegin = ipStrings[0] + "." + ipStrings[1] + "." + ipStrings[2] + "." + ipStrings[3];
            String ipStringEnd = Util.ipLong2String(ipLongEnd);

            String[] checkStart = find(ipStringBegin);
            String[] checkEnd = find(ipStringEnd);

            String startString = checkStart[0] + "," + checkStart[1] + "," + checkStart[2];
            String endString = checkEnd[0] + "," + checkEnd[1] + "," + checkEnd[2];
            String saveString = split[0] + "," + split[1] + "," + split[2];
            if (!(startString.equals(endString) && endString.equals(saveString))) {
                    throw new Exception("Datx file parse to csv error! Different with start_address and end_address. Start_address: '"
                            + startString + "', End_address: '" + endString + "'");
            }

            String ret = String.format("%s,%s,%s,%s,%s\n", ipStringBegin, ipStringEnd, split[0], split[1], split[2]);
            if (dedup.contains(ret)) {
                System.out.println("Create datx2CSV success.");
                fw.close();
                return;
            } else {
                dedup.add(ret);
                fw.write(ret);
            }
        }
    }
 }