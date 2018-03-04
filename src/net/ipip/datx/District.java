package net.ipip.datx;

import java.nio.charset.Charset;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

public class District
{
    private byte[] data;

    private long indexSize;

    public District(String name) throws IOException
    {
        Path path = Paths.get(name);
        data = Files.readAllBytes(path);

        indexSize = bytesToLong(data[0], data[1], data[2], data[3]);   
    }

    public String[] find(String ips)
    {
        long val = ip2long(ips);
        int startOff = 262148;
        int low = 0;
        int mid = 0;
        int high = new Long((indexSize - 262144 - 262148) / 13).intValue() - 1;
        int pos = 0;
        while (low <= high)
        {
            mid = Integer.valueOf((low + high) / 2);
            pos = mid * 13;

            long start = bytesToLong(data[startOff + pos], data[startOff + pos+1], data[startOff + pos+2], data[startOff + pos+3]);
            long end = bytesToLong(data[startOff + pos+4], data[startOff + pos+5], data[startOff + pos+6], data[startOff + pos+7]);
            if (val > end) {
                low = mid + 1;
            } else if (val < start) {
                high = mid - 1;
            } else {
                long off = bytesToLong(data[startOff+pos+11], data[startOff+pos+10],data[startOff+pos+9],data[startOff+pos+8]);
                int len = new Byte(data[startOff+pos+12]).intValue();
    
                int offset = new Long(off - 262144 + indexSize).intValue();

                byte[] loc = Arrays.copyOfRange(data, offset, offset+len);

                return new String(loc, Charset.forName("UTF-8")).split("\t", -1);
            }
        }

        return null;
    }

 
    private static long bytesToLong(byte a, byte b, byte c, byte d) {
        return int2long((((a & 0xff) << 24) | ((b & 0xff) << 16) | ((c & 0xff) << 8) | (d & 0xff)));
    }

    private static int str2Ip(String ip)  {
        String[] ss = ip.split("\\.");
        int a, b, c, d;
        a = Integer.parseInt(ss[0]);
        b = Integer.parseInt(ss[1]);
        c = Integer.parseInt(ss[2]);
        d = Integer.parseInt(ss[3]);
        return (a << 24) | (b << 16) | (c << 8) | d;
    }

    private static int str2Ip2(String ip)  {
        try {
            byte[] bytes = java.net.InetAddress.getByName(ip).getAddress();

            return ((bytes[0] & 0xFF) << 24) |
                    ((bytes[1] & 0xFF) << 16) |
                    ((bytes[2] & 0xFF) << 8) |
                    bytes[3];
        } catch (java.net.UnknownHostException e) {

        }

        return 0;
    }


    private static long ip2long(String ip)  {
        return int2long(str2Ip(ip));
    }

    private static long int2long(int i) {
        long l = i & 0x7fffffffL;
        if (i < 0) {
            l |= 0x080000000L;
        }
        return l;
    }

    public static void main(String[] args) {
        try {
            District c = new District("c:/work/tiantexin/framework/library/ip/quxian.datx");

            System.out.println(Arrays.toString(c.find("123.181.153.72")));
            
        } catch (IOException ioex){

        }
    }
}