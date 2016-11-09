import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;

public class CopyOfWorkerServer// implements Worker
{

	private static String dir = "";

	public static void main(String[] args) throws RemoteException {

		ArrayList<Integer> arr = encuentraPrimos(0, 11111);		
		System.err.println();
		ArrayList<Integer> arr2 = encuentraPrimos(11111, 11111*2);		
		System.err.println();
		ArrayList<Integer> arr3 = encuentraPrimos(11111*2, 11111*3);
		System.err.println();
		ArrayList<Integer> arr4 = encuentraPrimos(11111*3, 11111*4);
		System.err.println();
		ArrayList<Integer> arr5 = encuentraPrimos(11111*4, 11111*5);
		System.err.println();
		ArrayList<Integer> arr6 = encuentraPrimos(11111*5, 11111*6);
		System.err.println();
		ArrayList<Integer> arr7 = encuentraPrimos(11111*6, 11111*7);
		System.err.println();
		ArrayList<Integer> arr8 = encuentraPrimos(11111*7, 11111*8);
		System.err.println();
		ArrayList<Integer> arr9 = encuentraPrimos(11111*8, 11111*9);
		System.err.println();
		

		// arr = encuentraPrimos(0, 1000000000); limite de lo fisico, donde lo
		// irreal se junta con lo posible.

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
	public static ArrayList<Integer> encuentraPrimos(int min, int max)
			throws RemoteException {
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
		long t2 = System.nanoTime();
		if (min <= 2) {
			listaConPrimos.add(2);
		}
		min = min % 2 == 0 ? min + 1 : min;

		for (int i = min; i < max; i += 2) {
			if (!criba[i]) {
				listaConPrimos.add(i);
			}
		}
		long t3 = System.nanoTime();
		System.err.println(t2 - t1);
		System.err.println(t3 - t2);
		return listaConPrimos;
	}

	public static synchronized void print(String str) {
		System.err.println(str);
	}

}
