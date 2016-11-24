/*
* AUTOR: Marius Nemtanu, Pablo Piedrafita
* NIA: 605472, 691812
* FICHERO: MailBox.java
* TIEMPO: 5 horas
* DESCRIPCI�N: Este fichero contiene la clase Mailbox que representa un buz�n de mensajes
*/
package ssdd.ms;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MailBox extends Thread {

    private int port;
    private ServerSocket socket;
    private BlockingQueue<Envelope> queue;

    public MailBox(int p) {
        port = p;
        queue = new LinkedBlockingQueue<>();
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
                s.close();
            }
        } catch (SocketException e) {
            System.err.println("Cerrando buz�n.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Envelope getNextMessage() {
    	Envelope env = null;
        try {
			env= queue.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return env;
    }

    public void closeMailbox() throws IOException {
        socket.close();
    }

}
