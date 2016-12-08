/*
* AUTOR: Marius Nemtanu, Pablo Piedrafita
* NIA: 605472, 691812
* FICHERO: Chat.java
* TIEMPO: 4 horas toda la practica
* DESCRIPCIÓN: Este fichero contiene la clase TotalOrderMulticast que
* implementa el algoritmo de Ricart y Agrawala usado para mandar mensajes
* multicast. Esta clase es un thread que envia todo el rato lo que haya en la
* mensajes que mantiene internamente.
*/

package ssdd.ms;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class TotalOrderMulticast extends Thread {
    private MessageSystem msystem;
    // Usado para tratar de enviar solo un mensaje a la vez
    private Semaphore mutex;
    private BlockingQueue<Serializable> messagesToSend;
    private Serializable currentMessage;
    private boolean requestingCS;
    private boolean replyDeferred[];
    private int numAcksNotReceived;

    public TotalOrderMulticast(MessageSystem ms) {
        msystem = ms;
        mutex = new Semaphore(1);
        requestingCS = false;
        replyDeferred = new boolean[msystem.getNumAdd()];
        numAcksNotReceived = msystem.getNumAdd() - 1;
        messagesToSend = new LinkedBlockingQueue<>();
    }

    public void sendMulticast(Serializable message) {
        try {
            messagesToSend.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                mutex.acquire();
                currentMessage = messagesToSend.take();
                requestingCS = true;
                msystem.sendMulticast(new Payload(Payload.Type.REQ));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Envelope receiveMulticast() {
        while (true) {
            Envelope e = msystem.receive();
            Payload p = (Payload) e.getPayload();
            if (p.getType() != Payload.Type.USER) {
                switch (p.getType()) {
                case ACK:
                    msystem.setClockAfterReceiving(e);
                    if (--numAcksNotReceived == 0) {
                        numAcksNotReceived = msystem.getNumAdd() - 1;
                        msystem.sendMulticast(currentMessage);
                        requestingCS = false;
                        for (int i = 0; i < replyDeferred.length; i++) {
                            if (replyDeferred[i]) {
                                msystem.send(i + 1,
                                        new Payload(Payload.Type.ACK));
                                replyDeferred[i] = false;
                            }
                        }
                        mutex.release();
                        return new Envelope(msystem.getPid(), msystem.getPid(),
                                currentMessage, msystem.getClock());
                    }
                    break;
                case REQ:
                    if (requestingCS && ((msystem.getClock() < e
                            .getTimestamp())
                            || (msystem.getClock() == e.getTimestamp()
                                    && msystem.getPid() < e.getSource()))) {
                        replyDeferred[e.getSource() - 1] = true;
                        msystem.setClockAfterReceiving(e);
                    } else {
                        msystem.setClockAfterReceiving(e);
                        msystem.send(e.getSource(),
                                new Payload(Payload.Type.ACK));
                    }
                    break;
                default:
                    break;
                }
            } else {
                msystem.setClockAfterReceiving(e);
                return e;
            }
        }
    }
}
