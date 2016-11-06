import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class WorkerServer implements Worker {

    private static String dir = "";

    public static void main(String[] args) {
	if (args.length > 1)
	    throw new IllegalArgumentException("El parametro es [IP_registro]");
	if (args.length == 1)
	    dir = args[0];
	else
	    dir = "localhost";

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
    public ArrayList<Integer> encuentraPrimos(int min, int max) throws RemoteException {
	long t1 = System.nanoTime() / 1000000;
	ArrayList<Integer> primos = new ArrayList<Integer>();
	// initially assume all integers are prime
	boolean[] isPrime = new boolean[max + 1];
	for (int i = 2; i <= max; i++) {
	    isPrime[i] = true;
	}
	// mark non-primes <= n using Sieve of Eratosthenes
	for (int factor = 2; factor * factor <= max; factor++) {

	    // if factor is prime, then mark multiples of factor as nonprime
	    // suffices to consider mutiples factor, factor+1, ..., n/factor
	    if (isPrime[factor]) {
		for (int j = factor; factor * j <= max; j++) {
		    isPrime[factor * j] = false;
		}
	    }
	}
	long t2 = System.nanoTime() / 1000000;
	print("hello sin añadr " + (t2 - t1));

	// Añade los primos a la arraylist que hay que devolver
	for (int i = min; i <= max; i++) {
	    if (isPrime[i])
		primos.add(i);
	}
	t2 = System.nanoTime() / 1000000;
	print("hello añadir " + (t2 - t1));

	return primos;
    }

    public static synchronized void print(String str) {
	System.err.println(str);
    }

}
