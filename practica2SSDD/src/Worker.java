import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Worker extends Remote {

    java.util.ArrayList<Integer> encuentraPrimos(int min, int max) throws RemoteException;

}
