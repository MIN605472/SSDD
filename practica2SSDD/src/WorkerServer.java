import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;

public class WorkerServer // implements Worker
{

    private static String dir = "";

    public static void main(String[] args) throws RemoteException {
	if (args.length > 1)
	    throw new IllegalArgumentException("El parametro es [IP_registro]");
	if (args.length == 1)
	    dir = args[0];
	else
	    dir = "localhost";

	/*
	 * try { WorkerServer server = new WorkerServer(); Worker stub =
	 * (Worker) UnicastRemoteObject.exportObject(server, 0); Registry
	 * registry = LocateRegistry.getRegistry(dir); String nombre =
	 * generarNombre(registry.list()); registry.bind(nombre, stub);
	 * 
	 * } catch (RemoteException e) { e.printStackTrace(); } catch
	 * (AlreadyBoundException e) { e.printStackTrace(); }
	 */
	ArrayList<Integer> arr = encuentraPrimos(0, 100000000);
	/*
	 * for (Integer i : arr) { System.err.println(i); }
	 */
	System.err.println("SHIEEET");
	arr = encuentraPrimos(0, 100000000);
	/*
	 * for (Integer i : arr) { System.err.println(i); }
	 */

    }

    private static String generarNombre(String[] list) {
	String nombre = "";
	Random r = new Random();
	do {
	    nombre = "Worker" + r.nextInt(1000);
	} while (estaNombre(nombre, list));

	return nombre;

    }

    private static boolean estaNombre(String str, String[] list) {
	for (String s : list) {
	    if (str.equals(s))
		return true;
	}
	return false;
    }

    // http://introcs.cs.princeton.edu/java/14array/PrimeSieve.java.html
    public static ArrayList<Integer> encuentraPrimos(int min, int max) throws RemoteException {
	long t1 = System.nanoTime();
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
	min = min % 2 == 0 ? min + 1 : min;

	for (int i = min; i < max; i += 2) {
	    if (!criba[i]) {
		listaConPrimos.add(i);
	    }
	}
	long t2 = System.nanoTime();
	System.err.println(t2 - t1);
	return listaConPrimos;
    }

    public static synchronized void print(String str) {
	System.err.println(str);
    }

}
