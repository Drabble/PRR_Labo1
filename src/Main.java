import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Main {

    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("Hello World!");
        long decalage = 10000;
        Calendar cal_0 = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM dd, yyyy HH:mm");
        Date result = new Date(System.currentTimeMillis());
        MulticastSocket ms = new MulticastSocket(1222);
        InetAddress adrLocale = InetAddress.getLocalHost();
        //InetAddress[] adrServeurs = InetAddress.getByAddress();
        byte[] tampon = new byte[256];
        //tampon = result+decalage;
        //DatagramPacket dm = new DatagramPacket();
        ms.joinGroup(adrLocale);
        int i = 0;
        while (i < 1000) {
            Thread.sleep(1000);
            result = new Date(System.currentTimeMillis()+decalage);
            System.out.println(result);
            i++;
        }
        ms.leaveGroup(adrLocale);
    }
}
