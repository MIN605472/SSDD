/*
* AUTOR: Marius Nemtanu, Pablo Piedrafita
* NIA: 605472, 691812
* FICHERO: Chat.java
* TIEMPO: 4 horas toda la practica
* DESCRIPCIÓN: Este fichero contiene el metodo principal muestra el chat 
* y pone en funcionamiento el sistema de mensajes
*/

package ssdd.chat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

import ssdd.ms.Envelope;
import ssdd.ms.MessageSystem;
import ssdd.ms.Payload;
import ssdd.ms.TotalOrderMulticast;

public class Chat {

    public static void main(String[] args) {
        if (args.length != 2 && args.length != 3) {
            throw new IllegalArgumentException(
                    "Los parametros son incorrectos");
        }
        boolean debug = false;
        int src = 0;
        String networkFile = null;
        try {
            if (args.length == 3) {
                if (args[0].equals("-d")) {
                    debug = true;
                } else {
                    throw new IllegalArgumentException(
                            "Los parametros son incorrectos");
                }
                src = Integer.parseInt(args[1]);
                networkFile = args[2];
            } else {
                src = Integer.parseInt(args[0]);
                networkFile = args[1];
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Los parametros son incorrectos");
        }

        MessageSystem ms = null;
        try {
            ms = new MessageSystem(src, networkFile, debug);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        TotalOrderMulticast totalOrderMulticast = new TotalOrderMulticast(ms);
        totalOrderMulticast.start();
        ChatDialog dialog = new ChatDialog();
        dialog.setOnClickActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                totalOrderMulticast.sendMulticast(
                        new Payload(Payload.Type.USER, dialog.text()));
            }
        });
        while (true) {
            Envelope e = totalOrderMulticast.receiveMulticast();
            String message = ((Payload) e.getPayload()).getMessage();
            StringBuffer strBuf = new StringBuffer();
            if (e.getSource() == src) {
                strBuf.append("Yo: ");
            } else {
                strBuf.append(e.getSource());
                strBuf.append(": ");
            }
            strBuf.append(message);
            dialog.addMessage(strBuf.toString());
        }
    }
}
