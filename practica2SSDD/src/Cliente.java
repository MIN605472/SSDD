import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.LinkedList;

public class Cliente {

    private static String dir = "";
    private static ArrayList<ArrayList<Integer>> global;

    private static class PalThread implements Runnable {

	private int min, max, i;
	private Worker worker;

	public PalThread(Worker worker, int min, int max, int i) {
	    this.worker = worker;
	    this.min = min;
	    this.max = max;
	    this.i = i;
	}

	public void run() {
	    try {
		long t1 = System.currentTimeMillis();
		global.add(i, worker.encuentraPrimos(min, max));
		long t2 = System.currentTimeMillis();
		print("Soy " + i + " y he terminado en : " + ((t2 - t1)));
	    } catch (RemoteException e) {
		e.printStackTrace();
	    }

	}
    }

    public static synchronized void print(String str) {
	System.err.println(str);
    }

    public static void main(String[] args) {
	if (args.length > 4 || args.length < 3)
	    throw new IllegalArgumentException("Los parametros son min max n [IP_registro]");
	if (args.length == 3)
	    dir = "localhost";
	else
	    dir = args[3];

	int min = Integer.parseInt(args[0]);
	int max = Integer.parseInt(args[1]);
	int n = Integer.parseInt(args[2]);
	Thread[] t;

	try {
	    Registry registry = LocateRegistry.getRegistry(dir);
	    WorkerFactory factory = (WorkerFactory) registry.lookup("WorkerFactory");
	    ArrayList<Worker> workers = factory.dameWorkers(n);
	    if (workers == null) {
		System.err.println("No hay tantos workers como ha pedido");
	    } else {
		int q = (max - min) / n;
		int r = (max - min) % n;

		if (r != 0) {
		    t = new Thread[n + 1];
		    global = new ArrayList<>(n + 1);

		} else {
		    t = new Thread[n];
		    global = new ArrayList<>(n);

		}
		for (int i = 0; i < n; i++) {
		    System.err.println(min + q * i);
		    System.err.println(min + q * i + q);
		    t[i] = new Thread(new PalThread(workers.get(i), min + q * i, min + q * i + q, i));
		}
		if (r != 0) {
		    System.err.println("resto " + r);
		    System.err.println(max - r);
		    System.err.println(max);
		    t[n] = new Thread(new PalThread(workers.get(0), max - r, max, n));
		}

		for (int i = 0; i < t.length; i++) {
		    t[i].run();
		}

		for (int i = 0; i < t.length; i++) {
		    try {
			t[i].join();
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }
		}
		mostrarPrimos(global);

	    }

	} catch (NotBoundException e) {
	    e.printStackTrace();
	} catch (NumberFormatException e) {
	    System.err.println("Los parametros deben de ser enteros.");
	    e.printStackTrace();
	} catch (RemoteException e) {
	    e.printStackTrace();
	}

    }

    private static void mostrarPrimos(ArrayList<ArrayList<Integer>> primos) {
	for (ArrayList<Integer> a : primos) {
	    for (Integer i : a) {
		System.out.println(i);
	    }
	}
    }

}
