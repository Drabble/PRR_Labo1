/**
 * Project : Labo 1 PRR
 * Author : Antoine Drabble & Simon Baehler
 * Date : 22.09.2016
 * Description : Master
 * This program plays the role of the master. Each 10 seconds, the master will send its current time to the slaves
 * on a UDP multicast socket. The slave will be listening to the multicast address and receive the master's current
 * time. They will then send their time shift so the master can calculate the maximum shift between the slaves (the value
 * is not absolute, we only want to go forward in the time). Finally the master will send the maximum shift to the
 * slaves and they will synchronise their clock.
 *
 * Test : We ran the master and slave on localhost, on only one computer : it works well
 *        We ran one master and one slave on two different computers at school : At first it didn't work, the packet was
 *        sent but lost in the space (we observed it with wireshark). We then noticed that if we launched an additional slave on
 *        the same computer as the master, it worked well. This is probably due to some strange configuration
 *        on the HEIG lan.
 *        We ran the master and the slave on two different computer on a home lan : it works well
 *
 * Running the program : You must fist set the number of slaves you want to use.
 *                       If the program doesn't use the correct network interface, you have to uncomment
 *                       the line with "setNetworkInterface" and specify the interface in the networkInterface constant.
 *                       Finally you have to first start all the slaves and finally you can start the master. The
 *                       synchronisation will occure every 10 seconds.
 */

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Main class which starts a master
 *
 * @author : Antoine Drabble & Simon Baehler
 * @date 22.09.2016
 */
public class Main {

    /**
     * Starts a master
     *
     * @param args
     * @throws InterruptedException
     * @throws IOException
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        int nbSlaves = 1; // Number of slaves to synchronise
        final String multicastAddress = "228.5.6.7";
        final int PointToPointPort = 4444 ;
        final int datagramPacketPort = 4445;
        final int multicastPort = 4446 ;
        final String networkInterface = "wlan0";

        // Multicast socket to communicate with slaves
        InetAddress multicastGroup = InetAddress.getByName(multicastAddress);
        MulticastSocket multicastSocket = new MulticastSocket(multicastPort);

        // Specify the network interface if it is not choosing the right one by default
       // multicastSocket.setNetworkInterface(NetworkInterface.getByName(networkInterface));

        // Join group was not necessary on my home lan
        //multicastSocket.joinGroup(multicastGroup);

        // Point to point socket to receive message from slaves
        DatagramSocket pointToPointSocket = new DatagramSocket(PointToPointPort);

        // Date
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

        while (true) {
            // Retrieve the current time and send it on the multicast
            long currentTime = System.currentTimeMillis();
            byte[] longBuffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(currentTime).array();
            DatagramPacket timePacket = new DatagramPacket(longBuffer, longBuffer.length, multicastGroup, datagramPacketPort);
            multicastSocket.send(timePacket);
            System.out.println("Current time sent : " + sdf.format(new Date(currentTime)));

            // Receive every response from the slaves
            LinkedList<Long> shifts = new LinkedList<Long>();
            for(int i = 0; i < nbSlaves; i++){
                longBuffer = new byte[8];
                DatagramPacket shiftPacket = new DatagramPacket(longBuffer, longBuffer.length);
                pointToPointSocket.receive(shiftPacket);
                long receivedValue = bytesToLong(shiftPacket.getData());
                System.out.println("Shift received : " + receivedValue + " ms");
                shifts.push(receivedValue);
            }

            // Calculate maximum shift and send it
            long maxShift = Collections.max(shifts);
            longBuffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(maxShift + currentTime).array();
            DatagramPacket maxShiftPacket = new DatagramPacket(longBuffer, longBuffer.length, multicastGroup, 4445);
            multicastSocket.send(maxShiftPacket);
            System.out.println("Max shift sent : " + maxShift + " ms");

            // Wait 10 seconds
            Thread.sleep(10000);
        }

        //multicastSocket.leaveGroup(multicastGroup);
        //multicastSocket.close();
        //pointToPointSocket.close();
    }

    /**
     * Transforms a data of bytes to a long
     *
     * @author : Antoine Drabble & Simon Baehler
     * @date 22.09.2016
     * @param bytes - data of bytes
     * @return long - value of the data of bytes in a long format
     */
    private static long bytesToLong(byte[] bytes){
        long result = 0;
        for (int i = 0; i < bytes.length; i++)
        {
            result = (result << 8) + (bytes[i] & 0xff);
        }
        return result;
    }
}
