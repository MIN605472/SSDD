package serverSelector;

import java.nio.ByteBuffer;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class RequestHandler {

	private Pair<HTTPParser, FileChannel> pair;
	private int bufSize;
	private SocketChannel socket;

	public RequestHandler(Pair<HTTPParser, FileChannel> pair, int bufSize, SocketChannel socket) {
		this.pair = pair;
		this.bufSize = bufSize;
		this.socket = socket;
	}

	public void handle() throws IOException {
		System.err.println("handle");

		if (pair.getFirst().getMethod().equals("GET")) {
			System.err.println("entra aqui : get");
			File fichero = new File(new File("").getAbsolutePath() + "/resources" + pair.getFirst().getPath());
			System.err.println("hola" + new File("").getAbsolutePath() + "/resources" + pair.getFirst().getPath());
			if (!fichero.exists()) {
				System.err.println("entra aqui : get : found");
				notFound();
			} else {
				System.err.println("entra aqui : get : not found");

				found(fichero);
			}
		} else if (pair.getFirst().getMethod().equals("POST")) {
			System.err.println("entra aqui : post");
		} else {
			System.err.println("entra aqui : bad");
			badRequest();
		}

	}

	private void found(File fichero) throws IOException  {
		if (pair.getSecond() == null) {
			System.out.println(Files.probeContentType(fichero.toPath()));
			ByteBuffer header = ByteBuffer.allocate(bufSize);
			String h = new String("HTTP/1.1 200 OK\nContent-Type: " + Files.probeContentType(fichero.toPath())
					+ "\nContent-Length: " + fichero.length() + "\n\n");
			header.put(h.getBytes());
			header.flip();
			socket.write(header);
			System.err.println("null found");
			pair.setSecond(FileChannel.open(fichero.toPath(), StandardOpenOption.READ));

		}

		long bytesTranfered = pair.getSecond().transferTo(pair.getSecond().position(), bufSize, socket);
		System.err.println("transfered" + bytesTranfered);
		System.err.println("position " + pair.getSecond().position());
		System.err.println("size " + pair.getSecond().size());

		pair.getSecond().position(pair.getSecond().position() + bytesTranfered);
		if (pair.getSecond().size() - pair.getSecond().position() == 0) {
			System.err.println("cerrraaaooooo");
			socket.close();
		}
		System.err.println("position " + pair.getSecond().position());

	}

	private void notFound() throws IOException {
		if (pair.getSecond() == null) {
			System.err.println("fchnl null");
			File file = new File(new File("").getAbsolutePath() + "/resources/notfound.html");
			pair.setSecond(FileChannel.open(file.toPath(), StandardOpenOption.READ));
		}

		long bytesTranfered = pair.getSecond().transferTo(pair.getSecond().position(), bufSize, socket);
		System.err.println("transfered" + bytesTranfered);
		pair.getSecond().position(pair.getSecond().position() + bytesTranfered);
		if (pair.getSecond().size() - pair.getSecond().position() == 0) {
			socket.close();
		}

	}

	private void badRequest() throws IOException {
		System.err.println("bad request");
		if (pair.getSecond() == null) {
			File file = new File("badrequest.html");
			pair.setSecond(FileChannel.open(file.toPath(), StandardOpenOption.READ));
		}
		long bytesTranfered = pair.getSecond().transferTo(pair.getSecond().position(), bufSize, socket);
		pair.getSecond().position(pair.getSecond().position() + bytesTranfered);
		if (pair.getSecond().size() - pair.getSecond().position() == 0) {
			socket.close();
		}
	}
}
