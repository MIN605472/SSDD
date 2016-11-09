import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
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
	    System.setProperty("java.rmi.server.hostname", dir);
	    WorkerFactoryServer server = new WorkerFactoryServer();
	    WorkerFactory stub = (WorkerFactory) UnicastRemoteObject.exportObject(server, 0);
	    registry = LocateRegistry.getRegistry(dir);
	    registry.bind("WorkerFactory", stub);
	} catch (RemoteException e) {
	    e.printStackTrace();
	} catch (AlreadyBoundException e) {
	    e.printStackTrace();
	}
    }

    /**
     * Metodo que, dada una lista de nombres de los objetos registrados en el
     * registro, devuelve una lista con los nombres de los Workers que hay
     * dentro
     * 
     * @param lista
     *            array con los nombres de los ojetos registrados
     * @return lista con los nombres de los workers
     */
    private List<String> getWorkersNames(String[] lista) {
	List<String> listaNombresWorkers = new ArrayList<>();
	for (int i = 0; i < lista.length; i++) {
	    if (lista[i].matches("^(Worker)[0-9]+")) {
		listaNombresWorkers.add(lista[i]);
	    }
	}
	return listaNombresWorkers;
    }

    public ArrayList<Worker> dameWorkers(int n) throws RemoteException {
	List<String> listaNombresWorkers = getWorkersNames(registry.list());
	if (listaNombresWorkers.size() < n)
	    return null;

	ArrayList<Worker> workers = new ArrayList<>();
	Random ran = new Random();
	int idx;
	for (int i = 0; i < n; i++) {
	    idx = ran.nextInt(listaNombresWorkers.size());
	    try {
		Worker worker = (Worker) registry.lookup(listaNombresWorkers.get(idx));
		listaNombresWorkers.remove(idx);
		workers.add(worker);
	    } catch (NotBoundException e) {
		e.printStackTrace();
	    }
	}

	return workers;
    }

}
