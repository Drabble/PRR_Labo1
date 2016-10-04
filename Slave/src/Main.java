/**
 * Projet : Labo 1 PRR
 * Auteur : Antoine Drabble & Simon Baehler
 * Date : 22.09.2016
 * Description : Master
 */

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        long shift = 0; // Current time shift

        // Create the multicast socket to receive messages from the master
        MulticastSocket multicastSocket = new MulticastSocket(4445);
        InetAddress multicastGroup = InetAddress.getByName("228.5.6.7");
        // Specify the network interface if it is not choosing the right one by default
        multicastSocket.setNetworkInterface(NetworkInterface.getByName("wlan0"));
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
            long receivedValue =  bytesToLong(timePacket.getData());
            System.out.println("Received master time : " + sdf.format(new Date(receivedValue)));

            // Calculate new shift
            long currentTime = System.currentTimeMillis();
            shift = currentTime - receivedValue;

            // Send new shift

            longTampon = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(shift).array();

            //String s = timePacket.getAddress().getHostAddress();
            InetAddress address = InetAddress.getByName(timePacket.getAddress().getHostAddress());
            DatagramPacket shiftPacket = new DatagramPacket(longTampon, longTampon.length, address,  4444);
            pointToPointSocket.send(shiftPacket);
            System.out.println("Shift sent : " + shift + " ms");

            // Receive new shift
            DatagramPacket newShiftPacket = new DatagramPacket(longTampon, longTampon.length);
            multicastSocket.receive(newShiftPacket);
            shift = bytesToLong(newShiftPacket.getData()) - currentTime;
            System.out.println("New shift received : " + shift + " ms");
        }

        //multicastSocket.leaveGroup(multicastGroup);
        //multicastSocket.close();
        //pointToPointSocket.close();
    }

    private static long bytesToLong(byte[] bytes){
        long result = 0;
        for (int i = 0; i < bytes.length; i++)
        {
            result = (result << 8) + (bytes[i] & 0xff);
        }
        return result;
    }
}
