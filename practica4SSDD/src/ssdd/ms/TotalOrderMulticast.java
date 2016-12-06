package ssdd.ms;

import java.io.Serializable;
import java.util.Arrays;

public class TotalOrderMulticast {
    private MessageSystem msystem;
    private Serializable message;
    private boolean requestingCS;
    private boolean replyDeferred[];
    private int ackNotRecieved;

    public TotalOrderMulticast(MessageSystem ms) {
        msystem = ms;
        requestingCS = false;
        replyDeferred = new boolean[msystem.getNumAdd()];
        Arrays.fill(replyDeferred, false);
        ackNotRecieved = msystem.getNumAdd() - 1;
    }
    
    public void sendMulticast(Serializable message) {
        this.message = message;
        requestingCS = true;
        msystem.sendMulticast(new Payload(Payload.Type.REQ));
    }

    public Envelope receiveMulticast() {
        while (true) {
            Envelope e = msystem.receive();
            Payload p = (Payload) e.getPayload();
            if (p.getType() != Payload.Type.USER) {
                switch (p.getType()) {
                case ACK:
                    msystem.setClockAfterReceiving(e);
                    if (--ackNotRecieved == 0) {
                        ackNotRecieved = msystem.getNumAdd() - 1;
                        msystem.sendMulticast(message);
                        requestingCS = false;
                        for (int i = 0; i < replyDeferred.length; i++) {
                            if (replyDeferred[i]) {
                                msystem.send(i + 1,
                                        new Payload(Payload.Type.ACK));
                                replyDeferred[i] = false;
                            }
                        }
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
