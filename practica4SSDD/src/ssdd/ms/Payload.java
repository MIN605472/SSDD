/*
* AUTOR: Marius Nemtanu, Pablo Piedrafita
* NIA: 605472, 691812
* FICHERO: Payload.java
* TIEMPO: 4 horas toda la practica
* DESCRIPCIÓN: Este fichero contiene la clase Payload que representa 
* lo que va dentro de un sobre, los mensajes que los procesos se intercambian.
* 
*/

package ssdd.ms;

import java.io.Serializable;

public class Payload implements Serializable {
    private static final long serialVersionUID = 1L;

    public static enum Type {
        USER, ACK, REQ
    }

    private Type type;
    private String message;

    public Payload(Type type) {
        this.type = type;
    }

    public Payload(Type type, String message) {
        this.type = type;
        this.message = message;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return type + " " + message;

    }
}
