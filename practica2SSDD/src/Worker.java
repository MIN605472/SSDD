
/*
 * AUTOR: Marius Nemtanu, Pablo Piedrafita
 * NIA: 605472, 691812
 * FICHERO: Worker.java
 * TIEMPO: 16 comunes horas todo el proyecto
 * DESCRIPCION: el fichero contiene la interfaz de un objeto remoto 
 * que encuentra los primos en un intervalo
 * 
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interfaz del objeto remoto que encuentra primos
 * 
 */
public interface Worker extends Remote {

    /**
     * Metodo que encuentra los numeros primos en un intervalo dado
     * 
     * @param min
     *            parte baja del intervalo (inclusive)
     * @param max
     *            parte alta del intervalo (inclusive)
     * @return ArrayList con los numeros primos entre min y max, ambos
     *         inclusive
     * @throws RemoteException
     */
    java.util.ArrayList<Integer> encuentraPrimos(int min, int max)
            throws RemoteException;

}
