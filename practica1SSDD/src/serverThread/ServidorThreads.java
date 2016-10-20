/*
 * AUTOR: Marius Nemtanu, Pablo Piedrafita
 * NIA: 605472, 691812
 * FICHERO: ServidorThreads.java
 * TIEMPO: 17 horas en común todo el programa
 * DESCRIPCION: clase que contiene el método principal, encargado de recibir las peticiones 
 * y crear threads que se encarguen de su gestión
 */
package serverThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.Thread;

public class ServidorThreads {

	public static void main(String[] args) {
		if (args.length != 1) {
			throw new IllegalArgumentException("Parametros: <Port> ");
		}
		int servPort = Integer.parseInt(args[0]);
		ServerSocket servSock = null;
		try {
			// Create a server socket to accept client connection requests
			servSock = new ServerSocket(servPort);
			// Bucle infinito que acepta y da servicio a conexiones entrantes
			while (true) {
				// Aceptar conexion con cliente
				Socket clntSock = servSock.accept();
				// Crea thread para atender al cliente
				Thread t = new Thread(new GestorThreads(clntSock));
				t.run();

			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				servSock.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
