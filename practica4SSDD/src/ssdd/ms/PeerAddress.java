/*
* AUTOR: Marius Nemtanu, Pablo Piedrafita
* NIA: 605472, 691812
* FICHERO: PeerAddress.java
* TIEMPO: 5 horas
* DESCRIPCION: Este fichero contiene una clase que representa una dirección
*/
package ssdd.ms;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class PeerAddress {
    private String address;
    private int port;

    public PeerAddress(String a, int p) {
        address = a;
        port = p;
    }

    public Socket connect() throws UnknownHostException, IOException {
        return new Socket(address, port);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
