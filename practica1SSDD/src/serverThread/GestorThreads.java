package serverThread;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.file.Files;

public class GestorThreads implements Runnable {

	private Socket client;

	public GestorThreads(Socket clntsock) {
		client = clntsock;
	}

	public void run() {

		InputStream in = null;
		try {
			in = client.getInputStream();

			BlockingHTTPParser parser = new BlockingHTTPParser();
			parser.parseRequest(in);

			if (!parser.failed()) {
				if (isInWorkingDirectory(new File(parser.getPath()))) {
					if (parser.getMethod().equals("GET")) {
						File fichero = new File(parser.getPath().substring(1));
						if (!fichero.exists()) {
							try {
								client.getOutputStream().write(
										handleGetReq(new File("notfound.html"),
												HTTPResponse.Status.NOT_FOUND));
							} catch (IOException e) {
								e.printStackTrace();
							}
						} else {
							client.getOutputStream().write(
									handleGetReq(fichero,
											HTTPResponse.Status.OK));
						}
					} else if (parser.getMethod().equals("POST")) {
						String str = new String(parser.getBody().array());
						String res[] = new String[2];

						parseBodyPost(str, res);
						client.getOutputStream().write(
								handlePostReq(res[0], res[1]));

					}

				} else {
					client.getOutputStream().write(
							handleGetReq(new File("forbidden.html"),
									HTTPResponse.Status.FORBIDDEN));
				}

			} else {
				client.getOutputStream().write(
						handleGetReq(new File("badrequest.html"),
								HTTPResponse.Status.BAD_REQUEST));
			}
			client.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private byte[] handlePostReq(String path, String content) {
		String probe = "text/html";
		File fichero = null;
		boolean append = false;
		try {
			fichero = new File(path);
			append = fichero.exists();
			BufferedOutputStream buff = new BufferedOutputStream(
					new FileOutputStream(fichero, append));
			buff.write(content.getBytes());
			buff.close();
		} catch (IOException e) {
			e.getStackTrace();
		}
		// System.err.println(new String(content));
		return new HTTPResponse(HTTPResponse.Status.OK, probe, content.getBytes())
				.toBytes();
	}

	private byte[] handleGetReq(File file, HTTPResponse.Status stat) {
		System.err.println("GET");
		byte[] content = null;
		String probe = "";
		try {
			probe = Files.probeContentType(file.toPath());
			BufferedInputStream buff = new BufferedInputStream(
					new FileInputStream(file));
			content = new byte[(int) file.length()];
			buff.read(content);
			buff.close();
		} catch (IOException e) {
			e.getStackTrace();
		}
		// System.err.println(new String(content));
		return new HTTPResponse(stat, probe, content).toBytes();

	}

	private static boolean isInWorkingDirectory(File fichero) {
		String p = fichero.toString();
		int numSep = p.length() - p.replace(File.separator, "").length();
		return numSep == 1;
	}

	private static void parseBodyPost(String str, String res[])
			throws UnsupportedEncodingException {
		str = URLDecoder.decode(str, "UTF-8");
		String splitStr[] = str.split("&");
		res[0] = splitStr[0].substring(splitStr[0].indexOf("=") + 1);
		res[1] = splitStr[1].substring(splitStr[1].indexOf("=") + 1);
	}
}
