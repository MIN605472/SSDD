/*
 * AUTOR: Marius Nemtanu, Pablo Piedrafita 
 * NIA: 605472, 691812
 * FICHERO: GestorThreads.java
 * TIEMPO: 17 horas en común todo el programa
 * DESCRIPCION: Contiene una clase que se encarga de gestionar la petición de un cliente
 */
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

	/**
	 * Constructor, recibe como parámetro el socket del cliente
	 * 
	 * @param clntsock
	 */
	public GestorThreads(Socket clntsock) {
		client = clntsock;
	}

	public void run() {

		InputStream in = null;
		try {
			in = client.getInputStream();

			BlockingHTTPParser parser = new BlockingHTTPParser();
			parser.parseRequest(in);
			System.err.println(new String(parser.getBody().array()));

			if (!parser.failed()) {
				// Solo permitimos el acceso al fichero si se encuentra en el
				// directorio raíz del WS
				if (isInWorkingDirectory(new File(parser.getPath()))) {
					// En caso de "GET"
					if (parser.getMethod().equals("GET")) {
						File fichero = new File(parser.getPath().substring(1));
						if (!fichero.exists()) {
							try {
								client.getOutputStream()
										.write(handleGetReq(new File("notfound.html"), HTTPResponse.Status.NOT_FOUND));
							} catch (IOException e) {
								e.printStackTrace();
							}
						} else {
							client.getOutputStream().write(handleGetReq(fichero, HTTPResponse.Status.OK));
						}
						// En caso de "POST"
					} else if (parser.getMethod().equals("POST")) {
						String str = new String(parser.getBody().array());
						String res[] = new String[2];

						parseBodyPost(str, res);
						client.getOutputStream().write(handlePostReq(res[0], res[1]));

					}

				} else {
					client.getOutputStream()
							.write(handleGetReq(new File("forbidden.html"), HTTPResponse.Status.FORBIDDEN));
				}

			} else {
				client.getOutputStream()
						.write(handleGetReq(new File("badrequest.html"), HTTPResponse.Status.BAD_REQUEST));
			}
			client.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Este método se encarga de la escritura del contenido de las peticiones
	 * POST. Si el fichero ya existe, concatena el texto nuevo al final.
	 * 
	 * @param path
	 * @param content
	 * @return Respuesta HTTP generada gracias a la clase HTTPResponse
	 */
	private byte[] handlePostReq(String path, String content) {
		String probe = "text/html";
		File fichero = null;
		boolean append = false;
		try {
			fichero = new File(path);
			append = fichero.exists();
			BufferedOutputStream buff = new BufferedOutputStream(new FileOutputStream(fichero, append));
			buff.write(content.getBytes());
			buff.flush();
			buff.close();
		} catch (IOException e) {
			e.getStackTrace();
		}
		return new HTTPResponse(HTTPResponse.Status.OK, probe, makePostOkMessage(path, content).getBytes()).toBytes();
	}

	/**
	 * Este método se encarga de la lectura del contenido del fichero pedido con
	 * GET.
	 * 
	 * @param file
	 * @param stat
	 * @return Devuelve la respuesta generada gracias a la clase HTTPResponse
	 */
	private byte[] handleGetReq(File file, HTTPResponse.Status stat) {
		byte[] content = null;
		String probe = "";
		try {
			probe = Files.probeContentType(file.toPath());
			BufferedInputStream buff = new BufferedInputStream(new FileInputStream(file));
			content = new byte[(int) file.length()];
			buff.read(content);
			buff.close();
		} catch (IOException e) {
			e.getStackTrace();
		}
		return new HTTPResponse(stat, probe, content).toBytes();

	}

	/**
	 * Verifica que el fichero pasado como parámetro se encuentra en el
	 * directorio raíz del workspace
	 * 
	 * @param fichero
	 * @return TRUE si el fichero esta en el directorio raíz del workspace,
	 *         FALSE en cualquier otro caso
	 */
	private static boolean isInWorkingDirectory(File fichero) {
		String p = fichero.toString();
		int numSep = p.length() - p.replace(File.separator, "").length();
		return numSep == 1;
	}

	/**
	 * Este método devuelve en la array res el resultado de separar en path y en
	 * contenido la URL
	 * 
	 * @param str
	 * @param res
	 * @throws UnsupportedEncodingException
	 */
	private static void parseBodyPost(String str, String res[]) throws UnsupportedEncodingException {
		String splitStr[] = str.split("&");
		res[0] = URLDecoder.decode(splitStr[0].substring(splitStr[0].indexOf("=") + 1), "UTF-8");
		res[1] = URLDecoder.decode(splitStr[1].substring(splitStr[1].indexOf("=") + 1), "UTF-8");
	}

	/**
	 * Este método genera la respuesta en HTML que se devuelve tras recibir y
	 * escribir exitosamente una petición POST
	 * 
	 * @param nombre
	 * @param contenido
	 * @return String correspondiente al código HTML de la página de
	 *         confirmación de escritura tras una petición POST
	 */
	private static String makePostOkMessage(String nombre, String contenido) {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(
				"<html><head>\n<title>¡Éxito!</title>\n</head><body>\n<h1>¡Éxito!</h1>\n<p>Se ha escrito lo siguiente en el fichero ");
		strBuf.append(nombre);
		strBuf.append(":</p>\n<pre>");
		strBuf.append(contenido);
		strBuf.append("</pre>\n</body></html>");
		return strBuf.toString();
	}
}