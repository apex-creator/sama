import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class networkUtils {

    public static String FindIp(){
        try {
            Enumeration<NetworkInterface> NetInt = NetworkInterface.getNetworkInterfaces();
            while (NetInt.hasMoreElements()){
                NetworkInterface iface = NetInt.nextElement();

                if (iface.isLoopback() || !iface.isUp()){
                    continue;
                }

                Enumeration<InetAddress> address = iface.getInetAddresses();
                while (address.hasMoreElements())   {
                    InetAddress addr = address.nextElement();
                    if (addr instanceof Inet4Address){
                        return addr.getHostAddress();
                    }
                }
            }
        }catch (Exception e){
            return null;
        }

        return null;
    }

}
