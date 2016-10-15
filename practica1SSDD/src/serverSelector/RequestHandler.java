package serverSelector;

import java.nio.ByteBuffer;
import java.io.File;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public class RequestHandler {

	private ByteBuffer body;
	private String method = "";
	private String path = "";
	private int bufSize;
	private SocketChannel socket;

	public RequestHandler(String path, String method, ByteBuffer body, int bufSize, SocketChannel socket) {
		this.body = body;
		this.method = method;
		this.path = path;
		this.bufSize = bufSize;
		this.socket=socket;
	}

	public void handle() throws IOException {
		if (method.equals("GET")) {
			File fichero = new File(path);
			if (!fichero.exists())
				notFound();
			else
				leerFichero(fichero);
			//devolver fichero
		}

	}

	private void leerFichero(File fichero) {
		
		
	}

	private void notFound() throws IOException {
		String header=new String("HTTP/1.1 404 Not Found\nContent-Type: text/html\nContent-Length: 90\n");
		String body=new String("<html><head>\n<title>404 Not Found</title>\n</head><body>\n<h1>Not Found</h1></body></html>");
		ByteBuffer src = ByteBuffer.allocate(bufSize);
		String msg=header+body;
		src.put(msg.getBytes());
		socket.write(src);
	}
}
