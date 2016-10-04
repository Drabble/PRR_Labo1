/**
 * Projet : Labo 1 PRR
 * Auteur : Antoine Drabble & Simon Baehler
 * Date : 22.09.2016
 * Description : Slave
 *
 * This program receive the demand of the master to send him its time after that he receive the max value of all salves
 * who subscribed to the master then he set its time to this value (master time + shift max ) - salve time
 */

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        long shift = 0; // Current time shift

        final String multicastAddress = "228.5.6.7";
        final int datagramPacketPort = 4444 ;
        final int multicastPort = 4445 ;
        final String networkInterface = "wlan0";

        // Create the multicast socket to receive messages from the master
        MulticastSocket multicastSocket = new MulticastSocket(multicastPort);
        InetAddress multicastGroup = InetAddress.getByName(multicastAddress);
        // Specify the network interface if it is not choosing the right one by default
        multicastSocket.setNetworkInterface(NetworkInterface.getByName(networkInterface));
        multicastSocket.joinGroup(multicastGroup);

        // Create point to point socket to send messages to the master
        DatagramSocket pointToPointSocket = new DatagramSocket();

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

        System.out.println("Started the sockets!");

        while (true) {
            // Retrieve master current time
            byte[] longTampon = new byte[8];
            DatagramPacket timePacket = new DatagramPacket(longTampon, longTampon.length);
            multicastSocket.receive(timePacket);
            long receivedMasterTime =  bytesToLong(timePacket.getData());
            System.out.println("Received master time : " + sdf.format(new Date(receivedMasterTime)));

            // Calculate new shift
            long currentTime = System.currentTimeMillis();
            shift = currentTime - receivedMasterTime;

            // Send new shift
            longTampon = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(shift).array();

            //String s = timePacket.getAddress().getHostAddress();
            InetAddress address = InetAddress.getByName(timePacket.getAddress().getHostAddress());
            DatagramPacket shiftPacket = new DatagramPacket(longTampon, longTampon.length, address,  datagramPacketPort);
            pointToPointSocket.send(shiftPacket);
            System.out.println("Shift sent : " + shift + " ms");

            // Receive new shift
            DatagramPacket newShiftPacket = new DatagramPacket(longTampon, longTampon.length);
            multicastSocket.receive(newShiftPacket);
            shift = bytesToLong(newShiftPacket.getData()) - currentTime;
            System.out.println("New shift received : " + shift + " ms");
        }
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
