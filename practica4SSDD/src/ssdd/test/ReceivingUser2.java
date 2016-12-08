package ssdd.test;

import java.io.FileNotFoundException;

import ssdd.ms.MessageSystem;
import ssdd.ms.TotalOrderMulticast;
import ssdd.ms.Envelope;

public class ReceivingUser2 {

    private static int src = 2;
    private static String file = "peers.txt";

    public static void main(String args[]) throws FileNotFoundException {
        MessageSystem ms = new MessageSystem(src, file, true);
        TotalOrderMulticast mc = new TotalOrderMulticast(ms);
        while (true) {
            Envelope e = mc.receiveMulticast();
        }

    }

}
