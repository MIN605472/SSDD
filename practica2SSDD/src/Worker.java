import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface Worker extends Remote {

	java.util.ArrayList<Integer> encuentraPrimos(int min, int max)
			throws RemoteException;
	
	
	
}
