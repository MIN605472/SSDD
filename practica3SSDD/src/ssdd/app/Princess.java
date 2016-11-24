/*
* AUTOR: Marius Nemtanu, Pablo Piedrafita
* NIA: 605472, 691812
* FICHERO: Princess.java
* TIEMPO: 5 horas
* DESCRIPCI’ON: Este fichero contiene la clase Princesa, de la aplicación implementada
*/
package ssdd.app;

import java.io.FileNotFoundException;

import ssdd.ms.Envelope;
import ssdd.ms.MessageSystem;
import ssdd.p3.MessageValue;

public class Princess {

	
	public static void main(String[] args) {
		boolean debug = false;
		String networkFile = "peers.txt";
		Envelope env = null;
		for (String arg : args) {
			if (arg.equals("-d")){
				debug = true;
			}
			else{
				networkFile = arg;
			}
		}
		MessageSystem ms = null;
		try {
			ms = new MessageSystem(1, networkFile, debug);
		} catch (FileNotFoundException e) {
			System.err.println("El fichero " + networkFile + " no existe.");
		}
		int num = ms.getNumAdd();
		String msg = "Anuncio boda.";
		Carta letter = new Carta("Princesa", "Caballeros", msg, false);
		// Anuncia boda
		for (int i = 2; i <= num; i++) {
			ms.send(i, letter);
		}
		// Recibe proposiciones de matrimonio
		for (int i = 2; i <= num; i++) {
			if (i == 2) {
				env = ms.receive();
			} else {
				ms.receive();
			}
		}
		letter = (Carta) env.getPayload();
		int rem = env.getSource();
		String caballero = letter.getRemitente();
		letter = new Carta("Princesa", "Caballeros", "Me casare con "
				+ caballero, false);
		// Informa de con quien se casara
		for (int i = 2; i <= num; i++) {
			if (i == rem) {
				letter.setMencion(true);
				ms.send(i, letter);
				letter.setMencion(false);
			} else {
				ms.send(i, letter);
			}
		}
		// Recibe los retos de los caballeros
		for (int i = 2; i < num; i++) {
			if (i == 2) {
				env = ms.receive();

			} else {
				ms.receive();
			}
		}
		// Informa de que caballeros lucharan
		letter = (Carta) env.getPayload();
		int rem1 = env.getSource();
		String caballero2 = letter.getRemitente();
		letter = new Carta("Princesa", "Caballeros",
				"Anuncio que se enfrentaran " + caballero + " y " + caballero2,
				false);
		for (int i = 2; i <= num; i++) {
			if (i == rem || i == rem1) {
				letter.setMencion(true);
				ms.send(i, letter);
				letter.setMencion(false);
			} else {
				ms.send(i, letter);
			}
		}

		ms.stopMailbox();
	}

}
