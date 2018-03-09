package net.ipip.datx;

import java.nio.charset.Charset;
import java.io.IOException;
import java.util.Arrays;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

public class District
{
    private byte[] data;

    private long indexSize;


    public District(String name) throws IOException {
        Path path = Paths.get(name);
        data = Files.readAllBytes(path);

        indexSize = Util.bytesToLong(data[0], data[1], data[2], data[3]);
    }

    public String[] find(String ips) throws IPv4FormatException {
        if (!Util.isIPv4Address(ips)) {
            throw new IPv4FormatException();
        }
        long val = Util.ip2long(ips);
        int low = 0;
        int mid = 0;
        int high = new Long((indexSize - 262144 - 262148) / 13).intValue() - 1;
        int pos = 0;
        while (low <= high)
        {
            mid = Integer.valueOf((low + high) / 2);
            pos = mid * 13 + 262148;

            long start = Util.bytesToLong(data[pos], data[pos+1], data[pos+2], data[pos+3]);
            long end = Util.bytesToLong(data[pos+4], data[pos+5], data[pos+6], data[pos+7]);
            if (val > end) {
                low = mid + 1;
            } else if (val < start) {
                high = mid - 1;
            } else {
                long off = Util.bytesToLong(data[pos+11], data[pos+10],data[pos+9],data[pos+8]);
                int len = new Byte(data[pos+12]).intValue();
    
                int offset = new Long(off - 262144 + indexSize).intValue();

                byte[] loc = Arrays.copyOfRange(data, offset, offset+len);

                return new String(loc, Charset.forName("UTF-8")).split("\t", -1);
            }
        }

        return null;
    }
}