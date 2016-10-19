package serverThread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.lang.Thread;

public class ServidorThreads {

	public static void main(String[] args){
		if (args.length < 1) {
			throw new IllegalArgumentException("Parametros(s): <Port> ...");
		}

		int servPort = Integer.parseInt(args[0]);
		ServerSocket servSock=null;
		try {
			// Create a server socket to accept client connection requests
			servSock = new ServerSocket(servPort);

			while (true) { // Run forever, accepting and servicing connections
				Socket clntSock = servSock.accept(); // Get client connection

				Thread t = new Thread(new GestorThreads(clntSock));
				t.run();

				SocketAddress clientAddress = clntSock.getRemoteSocketAddress();
				System.out.println("Handling client at " + clientAddress);

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
