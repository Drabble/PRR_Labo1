/**
 * Project : Labo 1 PRR
 * Author : Antoine Drabble & Simon Baehler
 * Date : 22.09.2016
 * Description : Slave
 */

/*SimpleDateFormat sdf = new SimpleDateFormat("MM dd, yyyy HH:mm");
Date result = new Date(System.currentTimeMillis());
result = new Date(System.currentTimeMillis()+decalage);*/

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        int nbSlaves = 1; // Number of slaves to synchronise

        // Multicast socket to communicate with slaves
        InetAddress multicastGroup = InetAddress.getByName("228.5.6.7");
        MulticastSocket multicastSocket = new MulticastSocket(4446);
        multicastSocket.setNetworkInterface(NetworkInterface.getByName("wlan0"));
        multicastSocket.joinGroup(multicastGroup);

        // Point to point socket to receive message from slaves
        DatagramSocket pointToPointSocket = new DatagramSocket(4444);

        while (true) {
            // Retrieve the current time and send it on the multicast
            long currentTime = System.currentTimeMillis();
            byte[] longBuffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(currentTime).array();
            DatagramPacket timePacket = new DatagramPacket(longBuffer, longBuffer.length, multicastGroup, 4445);
            multicastSocket.send(timePacket);
            System.out.println("Current time sent : " + currentTime);

            // Receive every response from the slaves
            LinkedList<Long> shifts = new LinkedList<Long>();
            for(int i = 0; i < nbSlaves; i++){
                longBuffer = new byte[8];
                DatagramPacket shiftPacket = new DatagramPacket(longBuffer, longBuffer.length);
                pointToPointSocket.receive(shiftPacket);
                long receivedValue = bytesToLong(shiftPacket.getData());
                System.out.println("Shift received : " + receivedValue);
                shifts.push(receivedValue);
            }

            // Calculate maximum shift and send it
            long maxShift = Collections.max(shifts);
            // System.currentTimeMillis ??
            longBuffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(maxShift + System.currentTimeMillis()).array();
            DatagramPacket maxShiftPacket = new DatagramPacket(longBuffer, longBuffer.length, multicastGroup, 4445);
            multicastSocket.send(maxShiftPacket);
            System.out.println("Max shift received : " + maxShift);

            // Wait 10 seconds
            Thread.sleep(10000);
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
