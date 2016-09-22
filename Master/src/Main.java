/**
 * Project : Labo 1 PRR
 * Author : Antoine Drabble & Simon Baehler
 * Date : 22.09.2016
 * Description : Slave
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.*;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        int nbSlaves = 1;

        InetAddress groupe = InetAddress.getByName("228.5.6.7");
        MulticastSocket multicastSocket = new MulticastSocket(4446);

        byte[] longTampon = new byte[8];
        DatagramSocket pointToPointSocket = new DatagramSocket(4444);

        int cnt = 0;
        while (cnt < 1000) {
            cnt++;

            /*SimpleDateFormat sdf = new SimpleDateFormat("MM dd, yyyy HH:mm");
            Date result = new Date(System.currentTimeMillis());
            result = new Date(System.currentTimeMillis()+decalage);*/

            // Retrieve the current time and send it on the multicast
            long currentTime = System.currentTimeMillis();
            byte[] bytes = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(currentTime).array();
            DatagramPacket paquet = new DatagramPacket(bytes, bytes.length, groupe, 4445);
            multicastSocket.send(paquet);
            System.out.println("Current time envoye : " + currentTime);

            // Receive every response from the slaves
            LinkedList<Long> decalages = new LinkedList<Long>();
            for(int i = 0; i < nbSlaves; i++){
                DatagramPacket paquet2 = new DatagramPacket(longTampon, longTampon.length);
                pointToPointSocket.receive(paquet2);
                long valeurRecue = 0;
                byte[] byteRecu = paquet2.getData();
                for (int j = 0; j < byteRecu.length; j++)
                {
                    valeurRecue = (valeurRecue << 8) + (byteRecu[j] & 0xff);
                }
                System.out.println("Decalage recue : " + valeurRecue);
                decalages.push(valeurRecue);
            }

            // Calculate maximum decalage and send it
            long decalageMax = Collections.max(decalages);
            byte[] bytes2 = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(decalageMax + System.currentTimeMillis()).array();
            DatagramPacket paquet2 = new DatagramPacket(bytes2, bytes2.length, groupe, 4445);
            multicastSocket.send(paquet2);
            System.out.println("Decalage max recu : " + decalageMax);


            Thread.sleep(10000);
        }
        multicastSocket.close();
        pointToPointSocket.close();
    }
}
