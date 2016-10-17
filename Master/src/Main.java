/**
 * Project : Labo 1 PRR
 * Author : Antoine Drabble & Simon Baehler
 * Date : 22.09.2016
 * Description : Master
 * This program play the role of the master, each 10 second the master demand the time of it subscribers between the
 * master time and the subscriber time. After that he found the bigest value and send it to the salves (the value is
 * not absolute, we only want to go forward in the time)
 *
 * Test : We ran the program at local, on only on computer : it works well
 *        We ran the program on two different t computers at school : unfortunately it didn't work, the packet was
 *        send but lose in the space (we observed it with wireshark). We also notice if we launch a slave on
 *        the master it's works well.
 */

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
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
        multicastSocket.setNetworkInterface(NetworkInterface.getByName(networkInterface));
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
     * name : bytesToLong
     * Author : Antoine Drabble & Simon Baehler
     * Date : 22.09.2016
     * Description : transforme a data of bytes to a long
     * param : byte[] bytes - data of bytes
     * out : long - value of the data of bytes in a long format
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
