/**
 * Projet : Labo 1 PRR
 * Auteur : Antoine Drabble & Simon Baehler
 * Date : 22.09.2016
 * Description : Master
 */

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        long shift = 0; // Current time shift

        // Create the multicast socket to receive messages from the master
        MulticastSocket multicastSocket = new MulticastSocket(4445);
        InetAddress multicastGroup = InetAddress.getByName("228.5.6.7");
        multicastSocket.setNetworkInterface(NetworkInterface.getByName("wlan0"));
        multicastSocket.joinGroup(multicastGroup);

        // Create point to point socket to send messages to the master
        DatagramSocket pointToPointSocket = new DatagramSocket();

        System.out.println("Started the sockets!");

        while (true) {
            // Retrieve master current time
            byte[] longTampon = new byte[8];
            DatagramPacket timePacket = new DatagramPacket(longTampon, longTampon.length);
            multicastSocket.receive(timePacket);
            long receivedValue = 0;
            byte[] receivedBuffer = timePacket.getData();
            for (int i = 0; i < receivedBuffer.length; i++)
            {
                receivedValue = (receivedValue << 8) + (receivedBuffer[i] & 0xff);
            }
            System.out.println("Received value : " + receivedValue);

            // Calculate new shift
            shift = System.currentTimeMillis() - receivedValue;

            // Send new shift
            longTampon = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(shift).array();
            InetAddress address = InetAddress.getByName("10.192.94.152");
            DatagramPacket shiftPacket = new DatagramPacket(longTampon, longTampon.length, address,  4444);
            pointToPointSocket.send(shiftPacket);
            System.out.println("Shift sent : " + shift);

            // Receive new shift
            DatagramPacket newShiftPacket = new DatagramPacket(longTampon, longTampon.length);
            multicastSocket.receive(newShiftPacket);
            receivedValue = 0;
            receivedBuffer = newShiftPacket.getData();
            for (int i = 0; i < receivedBuffer.length; i++)
            {
                receivedValue = (receivedValue << 8) + (receivedBuffer[i] & 0xff);
            }
            System.out.println("Received value : " + receivedValue);
            shift = receivedValue - System.currentTimeMillis();
            System.out.println("New shift : " + shift);
        }

        //multicastSocket.leaveGroup(multicastGroup);
        //multicastSocket.close();
        //pointToPointSocket.close();
    }
}
