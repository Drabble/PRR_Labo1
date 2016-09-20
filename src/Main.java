import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Hello World!");
        long decalage = 10000;
        Calendar cal_0 = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM dd, yyyy HH:mm");
        Date result = new Date(System.currentTimeMillis());
        while (true) {
            Thread.sleep(1000);
            result = new Date(System.currentTimeMillis()+decalage);
            System.out.println(result);
        }
    }
}
