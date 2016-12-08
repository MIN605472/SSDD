package ssdd.test;

import java.io.FileNotFoundException;

import ssdd.ms.MessageSystem;
import ssdd.ms.Payload;
import ssdd.ms.TotalOrderMulticast;

public class SendingUser1 {
    private static int src = 3;
    private static String file = "peers.txt";
    private static String msg = "Hola soy 3";

    public static void main(String args[]) throws FileNotFoundException {
        MessageSystem ms = new MessageSystem(src, file, true);
        TotalOrderMulticast mc = new TotalOrderMulticast(ms);
        mc.start();
        mc.sendMulticast(new Payload(Payload.Type.USER, msg));
        while (true) {
            mc.receiveMulticast();
        }

    }

}
