# datx-java
IPIP.net官方支持的解析datx格式的Java代码

## 示例代码
<pre>
<code>
import net.ipip.datx.*;

import java.io.IOException;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {

        try {
            City city = new City("/path/to/mydata4vipday4.datx"); // 城市库

            System.out.println(Arrays.toString(city.find("8.8.8.8")));
            System.out.println(Arrays.toString(city.find("255.255.255.255")));

            District district = new District("/path/to/quxian.datx");//区县库

            System.out.println(Arrays.toString(district.find("1.12.0.0")));
            System.out.println(Arrays.toString(district.find("223.255.127.250")));

            BaseStation baseStation = new BaseStation("/path/to/station_ip.datx"); // 基站库

            System.out.println(Arrays.toString(baseStation.find("8.8.8.8")));
            System.out.println(Arrays.toString(baseStation.find("223.221.121.0")));

        } catch (IOException ioex) {
            ioex.printStackTrace();
        } catch (IPv4FormatException ipex) {
            ipex.printStackTrace();
        }
    }
}
</code>
</pre>