
/*
 * AUTOR: Marius Nemtanu, Pablo Piedrafita
 * NIA: 605472, 691812
 * FICHERO: WorkerFactory.java
 * TIEMPO: 16 comunes horas todo el proyecto
 * DESCRIPCION: el fichero contiene la interfaz de un objeto remoto 
 * que encuentra Workers
 * 
 */

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * 
 * Interfaz del objeto remoto que encuentra Workers
 *
 */
public interface WorkerFactory extends Remote {

    /**
     * Metodo que devuevle una lista de hasta n referencias a objetos Worker
     * 
     * @param n
     *            numero de Workers que se quiere obtener
     * @return ArrayList con los n Workers pedidos, null en caso de que no haya
     *         exactamente n Workers
     * @throws RemoteException
     */
    ArrayList<Worker> dameWorkers(int n) throws RemoteException;
}
