package serverSelector;

import java.nio.ByteBuffer;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
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

	private boolean isInWorkingDirectory(File fichero) {
		String p = fichero.toString();
		int numSep = p.length() - p.replace(File.separator, "").length();
		System.err.println(numSep + " num sep");
		return numSep == 1;
	}

	private void parseBodyPost(String str, String res[]) {
		System.err.println("parseboyd: " + str);
		String splitStr[] = str.split("&");
		res[0] = splitStr[0].substring(splitStr[0].indexOf("=") + 1);
		res[1] = splitStr[1].substring(splitStr[1].indexOf("=") + 1);
		System.err.println("res0: " + res[0]);
		System.err.println("res1: " + res[1]);

	}

	public void handle() throws IOException {
		System.err.println("handle");
		if (!pair.getFirst().failed()) {
			File fichero = new File(new File("").getAbsolutePath() + pair.getFirst().getPath());
			if (isInWorkingDirectory(new File(pair.getFirst().getPath()))) {
				if (pair.getFirst().getMethod().equals("GET")) {
					System.err
							.println("entra aqui : get   " + isInWorkingDirectory(new File(pair.getFirst().getPath())));
					fichero = new File(new File("").getAbsolutePath() + pair.getFirst().getPath());
					System.err.println("hola" + new File("").getAbsolutePath() + pair.getFirst().getPath());
					if (!fichero.exists()) {
						System.err.println("entra aqui : get : non found");
						fichero = new File(new File("").getAbsolutePath() + "/notfound.html");
						String header = new String("HTTP/1.1 404 Not Found\nContent-Type: text/html\nContent-Length: "
								+ fichero.length() + "\n\n");
						sendResponse(header, fichero);
					} else {
						System.err.println("entra aqui : get : found");
						String header = new String(
								"HTTP/1.1 200 OK\nContent-Type: " + Files.probeContentType(fichero.toPath())
										+ "\nContent-Length: " + fichero.length() + "\n\n");
						sendResponse(header, fichero);
					}
				} else if (pair.getFirst().getMethod().equals("POST")) {
					System.err.println("entra aqui : post");
					String body = URLDecoder.decode(new String(pair.getFirst().getBody().array()), "UTF-8");
					String res[] = new String[2];
					parseBodyPost(body, res);
					System.err.println("nombre fich: " + res[0]);
					System.err.println("contendio fich: " + res[1]);

					File f = new File(res[0]);
					f.createNewFile();
					writeRequest(f, res[1]);
					fichero = new File("success_post.html");
					String header = new String("HTTP/1.1 200 OK\nContent-Type: "
							+ Files.probeContentType(fichero.toPath()) + "\nContent-Length: " + fichero.length()
							+ "\n\n<html><head>\n<title>¡Éxito!</title>\n</head><body>\n"
							+ "<p>Se ha escrito lo siguiente en el fichero " + res[0] + ":</p>\n<pre>\n" + res[1]
							+ "\n</pre>\n</body></html>\n");
					ByteBuffer buf = ByteBuffer.allocate(header.length());
					buf.put(header.getBytes());
					buf.flip();
					socket.write(buf);
					socket.close();

				} else {
					fichero = new File(new File("").getAbsolutePath() + "notimplemented.html");
					String header = new String(
							"HTTP/1.1 501 Not Implemented\nContent-Type: " + Files.probeContentType(fichero.toPath())
									+ "\nContent-Length: " + fichero.length() + "\n\n");
					sendResponse(header, fichero);
				}
			} else {
				fichero = new File("forbidden.html");
				String header = new String("HTTP/1.1 403 Forbidden\nContent-Type: "
						+ Files.probeContentType(fichero.toPath()) + "\nContent-Length: " + fichero.length() + "\n\n");
				sendResponse(header, fichero);
			}
		} else {
			File fichero = new File(new File("").getAbsolutePath() + "badrequest.html");
			String header = new String("HTTP/1.1 400 Bad Request\nContent-Type: "
					+ Files.probeContentType(fichero.toPath()) + "\nContent-Length: " + fichero.length() + "\n\n");
			sendResponse(header, fichero);
		}
	}

	private void writeRequest(File fichero, String contenido) throws IOException {
		FileChannel fc = FileChannel.open(fichero.toPath(), StandardOpenOption.WRITE);
		ByteBuffer buf = ByteBuffer.allocate(contenido.length());
		buf.put(contenido.getBytes());
		buf.flip();
		fc.write(buf);
	}

	private void sendResponse(String header, File fichero) throws IOException {
		if (pair.getSecond() == null) {
			System.out.println(Files.probeContentType(fichero.toPath()));
			ByteBuffer h = ByteBuffer.allocate(bufSize);
			h.put(header.getBytes());
			h.flip();
			socket.write(h);
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
}
