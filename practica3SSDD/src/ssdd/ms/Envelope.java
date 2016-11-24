/*
* AUTOR: Marius Nemtanu, Pablo Piedrafita
* NIA: 605472, 691812
* FICHERO: Envelope.java
* TIEMPO: 5 horas
* DESCRIPCI’ON: Este fichero contiene la clase Envelope que representa un sobre,
* indicando remitente y destinatario
*/
package ssdd.ms;

import java.io.Serializable;

public class Envelope implements Serializable {
    private static final long serialVersionUID = 1L;
    private int source;
    private int destination;
    private Serializable payload;

    public Envelope(int s, int d, Serializable p) {
        source = s;
        destination = d;
        payload = p;
    }

    public int getSource() {
        return source;
    }

    public int getDestination() {
        return destination;
    }

    public Serializable getPayload() {
        return payload;
    }
}
