package ssdd.ms;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;

public class MailBox extends Thread {

    private int port;
    private ServerSocket socket;
    private Queue<Envelope> queue;

    public MailBox(int p) {
        port = p;
        queue = new LinkedList<>();
    }

    // Servidor secuencial
    public void run() {
        try {
            socket = new ServerSocket(port);
            while (true) {
                Socket s = socket.accept();
                ObjectInputStream objectIS = new ObjectInputStream(
                        s.getInputStream());
                queue.add((Envelope) objectIS.readObject());
            }
        } catch (SocketException e) {
            System.err.println("Cerrando buzón.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Envelope getNextMessage() {
        return queue.remove();
    }

    public void closeMailbox() throws IOException {
        socket.close();
    }

}
