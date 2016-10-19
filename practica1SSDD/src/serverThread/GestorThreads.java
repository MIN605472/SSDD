package serverThread;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.file.Files;

public class GestorThreads implements Runnable {

	public enum Status {
		NOT_FOUND(404, "Not Found"), OK(200, "OK"), FORBIDDEN(403,
				"403 Forbidden"), BAD_REQUEST(400, "Bad Request"), NOT_IMPLEMENTED(
				501, "Not Implemented");

		private int code;
		private String description;

		Status(int code, String description) {
			this.code = code;
			this.description = description;
		}

		public String getDescription() {
			return description;
		}

		public int getCode() {
			return code;
		}
	}

	private Socket client;

	public GestorThreads(Socket clntsock) {
		client = clntsock;
	}

	public void run() {

		InputStream in = null;
		try {
			in = client.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}

		BlockingHTTPParser parser = new BlockingHTTPParser();
		parser.parseRequest(in);

		if (!parser.failed()) {
			if (isInWorkingDirectory(new File(parser.getPath()))) {
				if (parser.getMethod().equals("GET")) {
					File fichero = new File(parser.getPath().substring(1));
					if (!fichero.exists()) {
						handleGetReq(new File("notfound.html"),	Status.NOT_FOUND);
					} else {
						handleGetReq(fichero, Status.OK);
					}
				} else if (parser.getMethod().equals("POST")) {
					String str = new String(parser.getBody().array());
					String res[] = new String[2];
					try {
						parseBodyPost(str, res);
						handlePostReq(res[0], res[1]);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (IOException e){
						e.printStackTrace();
					}
					
				}

			} else {
				handleGetReq(new File("forbidden.html"), Status.FORBIDDEN);
			}

		} else {
			handleGetReq(new File("badrequest.html"), Status.BAD_REQUEST);
		}

		// OutputStream out = clntSock.getOutputStream();
	}

	private byte[] handlePostReq(String path, String content) throws IOException {
		File fichero = new File(path);
		boolean append= fichero.exists();
		BufferedOutputStream buff = new BufferedOutputStream(new FileOutputStream(fichero,append));
		buff.write(content.getBytes());
		buff.close();
		String response = new String("HTTP/1.1 200 OK\nContent-Type: "
				+ Files.probeContentType(fichero.toPath()) + "\nContent-Length: " + fichero.length()
				+ "\n\n<html><head>\n<title>¡Éxito!</title>\n</head><body>\n"
				+ "<p>Se ha escrito lo siguiente en el fichero " + path + ":</p>\n<pre>\n" + content
				+ "\n</pre>\n</body></html>\n");
		return response.getBytes();
	}

	private byte[] handleGetReq(File file, Status stat) {
		String header = 
		
	}

	private boolean isInWorkingDirectory(File file) {
		// TODO Auto-generated method stub
		return false;
	}

	private static void parseBodyPost(String str, String res[]) throws UnsupportedEncodingException {
		  str = URLDecoder.decode(str, "UTF-8");
		  String splitStr[] = str.split("&");
		  res[0] = splitStr[0].substring(splitStr[0].indexOf("=") + 1);
		  res[1] = splitStr[1].substring(splitStr[1].indexOf("=") + 1);
		 }
}
