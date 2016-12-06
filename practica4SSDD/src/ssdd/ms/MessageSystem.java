/*
* AUTOR: Marius Nemtanu, Pablo Piedrafita
* NIA: 605472, 691812
* FICHERO: MessageSystem.java
* TIEMPO: 5 horas
* DESCRIPCI�N: Este fichero contiene la clase MessageSystem que permite recibir
* y enviar mensajes a los contactos especificados en el fichero peers
*/
package ssdd.ms;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MessageSystem {
    private int pid;
    private boolean showDebugMsgs;
    private ArrayList<PeerAddress> addresses = new ArrayList<PeerAddress>();
    private MailBox mailbox;
    private int clock;

    public MessageSystem(int source, String networkFile, boolean debug)
            throws FileNotFoundException {
        clock = 0;
        showDebugMsgs = debug;
        pid = source;
        int port = loadPeerAddresses(networkFile);
        mailbox = new MailBox(port);
        mailbox.start();
    }

    public void send(int dst, Serializable message) {
        clock++;
        if (showDebugMsgs) {
            System.err.println("[Ts: " + clock + "] Sending "
                    + message.toString() + " from " + pid + " to " + dst);
        }
        Socket socket = null;
        try {
            socket = addresses.get(dst - 1).connect();
            ObjectOutputStream objectOS = new ObjectOutputStream(
                    socket.getOutputStream());
            objectOS.writeObject(new Envelope(pid, dst, message, clock));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMulticast(Serializable message) {
        clock++;
        if (showDebugMsgs) {
            System.err.println("[Ts: " + clock + "] Sending "
                    + message.toString() + " from " + pid + " to all");
        }
        Payload pa = (Payload) message;
        for (int i = 0; i < addresses.size(); i++) {
            if (pa.getType() == Payload.Type.USER
                    || (pa.getType() != Payload.Type.USER && i != pid - 1)) {
                PeerAddress p = addresses.get(i);
                Socket socket = null;
                try {
                    socket = p.connect();
                    ObjectOutputStream outputStream = new ObjectOutputStream(
                            socket.getOutputStream());
                    outputStream.writeObject(
                            new Envelope(pid, i + 1, message, clock));
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public Envelope receive() {
        Envelope envelope = mailbox.getNextMessage();
        if (showDebugMsgs) {
            System.err.println("[Ts: " + envelope.getTimestamp() + "] "
                    + "Receiving " + envelope.getPayload().toString()
                    + " from " + envelope.getSource() + " to "
                    + envelope.getDestination());
        }
        return envelope;
    }

    public void setClockAfterReceiving(Envelope envelope) {
        clock = Math.max(clock, envelope.getTimestamp()) + 1;
    }

    public void stopMailbox() {
        try {
            mailbox.closeMailbox();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getNumAdd() {
        return addresses.size();

    }

    private int loadPeerAddresses(String networkFile)
            throws FileNotFoundException {
        BufferedReader in = new BufferedReader(new FileReader(networkFile));
        String line;
        int port = 0;
        int n = 0;
        try {
            while ((line = in.readLine()) != null) {
                ++n;
                int sep = line.indexOf(':');
                if (sep != -1) {
                    addresses.add(new PeerAddress(line.substring(0, sep),
                            Integer.parseInt(line.substring(sep + 1))));
                    if (n == pid) {
                        port = addresses.get(addresses.size() - 1).getPort();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {

            }
        }
        return port;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getClock() {
        return clock;
    }

    public void setClock(int clock) {
        this.clock = clock;
    }
}
