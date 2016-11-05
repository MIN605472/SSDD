import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;

public class WorkerFactoryServer implements WorkerFactory {

	private static String dir = "";
	private static Registry registry;

	public static void main(String[] args) {
		if (args.length > 1)
			throw new IllegalArgumentException("El parametro es [IP_registro]");
		if (args.length == 1)
			dir = args[0];
		else
			dir = "localhost";
		try {

			WorkerFactoryServer server = new WorkerFactoryServer();
			WorkerServer stub = (WorkerServer) UnicastRemoteObject
					.exportObject(server, 0);

			registry = LocateRegistry.getRegistry(dir);
			registry.bind("WorkerFactory", stub);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<Worker> dameWorkers(int n) throws RemoteException {
		String[] listaWorkers = registry.list();
		if (listaWorkers.length < n) return null;
		
		ArrayList<Worker> workers = new ArrayList<>();
		Random ran = new Random();
		int idx;
		
		for (int i = 0; i < n; i++) {
			idx = ran.nextInt(listaWorkers.length);
			try {
				Worker worker = (Worker) registry.lookup(listaWorkers[idx]);
				workers.add(worker);
			} catch (NotBoundException e) {
				e.printStackTrace();
			}
		}

		return workers;
	}

}
