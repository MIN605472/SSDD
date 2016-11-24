package ssdd.app;

import java.io.FileNotFoundException;

import ssdd.ms.Envelope;
import ssdd.ms.MessageSystem;

public class Knight {
	private static final String nombre = nombreRandom();

	public static void main(String[] args) {
		boolean debug = false;
		String networkFile = "peers.txt";
		int pid = Integer.parseInt(args[0]);
		String nombre = args[1];
		
		for (String arg : args) {
			if (arg.equals("-d")) {
				debug = true;
			}
		}

		MessageSystem ms = null;
		try {
			ms = new MessageSystem(pid, networkFile, debug);
		} catch (FileNotFoundException e) {
			System.err.println("El fichero " + networkFile + " no existe.");
		}
		// recibe el anuncio
		Envelope env = ms.receive();
		// envia peticion
		Carta letter = new Carta(nombre, "princesa", "Yo " + nombre
				+ ", me propongo para el matrimonio.", false);
		ms.send(1, letter);
		// recibe a quien a elegido
		env = ms.receive();
		letter = (Carta) env.getPayload();
		if (!letter.isMencion()) {
			letter.setCuerpo("Yo " + nombre
					+ ", reto al pretendiente a un duelo.");
			ms.send(1, letter);
		}

	}

	private static String nombreRandom() {
		// TODO Auto-generated method stub
		return null;
	}
}
