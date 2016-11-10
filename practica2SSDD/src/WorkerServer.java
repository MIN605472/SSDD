
/*
 * AUTOR: Marius Nemtanu, Pablo Piedrafita
 * NIA: 605472, 691812
 * FICHERO: WorkerServer.java
 * TIEMPO: 16 comunes horas todo el proyecto
 * DESCRIPCION: el fichero contiene la clase que implementa la interfaz Worker
 * 
 */

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;

/**
 * Clase que implementa la interfaz Worker y lo registra en el registro
 *
 */
public class WorkerServer implements Worker {

    private static String dir = "";

    public static void main(String[] args) {
        if (args.length > 1) {
            throw new IllegalArgumentException(
                    "El parametro del WorkerServer es: [IP_registro]");
        }
        if (args.length == 1) {
            dir = args[0];
        } else {
            dir = "localhost";
        }

        try {
            WorkerServer server = new WorkerServer();
            Worker stub = (Worker) UnicastRemoteObject.exportObject(server, 0);
            Registry registry = LocateRegistry.getRegistry(dir);
            String nombre = generarNombre(registry.list());
            registry.bind(nombre, stub);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo que genera nombres para los Workers
     * 
     * @param list
     *            array con los nombres que ya existen en el registro
     * @return String con un nombre unico, que no existe en la lista list
     */
    private static String generarNombre(String[] list) {
        String nombre = "";
        Random r = new Random();
        do {
            nombre = "Worker" + r.nextInt(Integer.MAX_VALUE);
        } while (estaNombre(nombre, list));

        return nombre;

    }

    /**
     * Metodo que mira si una String dada esta en un array de String dado
     * 
     * @param str
     *            String a comprobar
     * @param list
     *            array con el que comprobar
     * @return verdadero en caso de que str este dentro de list, false en caso
     *         de que no este dentro de list
     */
    private static boolean estaNombre(String str, String[] list) {
        for (String s : list) {
            if (str.equals(s))
                return true;
        }
        return false;
    }

    public ArrayList<Integer> encuentraPrimos(int min, int max)
            throws RemoteException {
        if (max < min) {
            throw new IllegalArgumentException(
                    "El parametro max no puede ser menor que min");
        }
        if (min < 0) {
            throw new IllegalArgumentException(
                    "El parmetro min ha de ser positiovo");
        }
        boolean criba[] = new boolean[max + 1];
        criba[0] = true;
        criba[1] = true;
        ArrayList<Integer> listaConPrimos = new ArrayList<>();
        for (int factor = 3; factor * factor <= max; factor += 2) {
            if (!criba[factor]) {
                for (int j = factor * factor; j <= max; j += 2 * factor) {
                    criba[j] = true;
                }
            }
        }
        if (min <= 2) {
            listaConPrimos.add(2);
        }
        min = (min % 2 == 0) ? min + 1 : min;

        for (int i = min; i <= max; i += 2) {
            if (!criba[i]) {
                listaConPrimos.add(i);
            }
        }
        return listaConPrimos;
    }
}
