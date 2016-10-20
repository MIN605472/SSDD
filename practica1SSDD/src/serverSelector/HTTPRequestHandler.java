/*
 * AUTOR: Marius Nemtanu, Pablo Piedrafita
 * NIA: 605472, 691812
 * FICHERO: HTTPRequestHandler.java
 * TIEMPO: 17 horas en comun todo el programa
 * DESCRIPCION: el fichero contiene una clase que tiene metodos para tratar las peticiones
 * HTTP hechas por el cliente
 * 
 */

package serverSelector;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/**
 * Clase que contiene metodos para tratar las peticiones HTTP
 *
 */
public class HTTPRequestHandler {

	/**
	 * Metodo que trata un peticion HTTP dada
	 * 
	 * @param parser
	 *            peticion HTTP a tratar
	 * @return respuesta a esa peticion
	 * @throws IOException
	 */
	public static HTTPResponse handle(HTTPParser parser) throws IOException {
		if (!parser.failed()) {
			if (isInWorkingDirectory(new File(parser.getPath()))) {
				if (parser.getMethod().equals("GET")) {
					File fichero = new File(parser.getPath().substring(1));
					if (!fichero.exists()) {
						return handleGetReq(new File("notfound.html"), HTTPResponse.Status.NOT_FOUND);
					} else {
						return handleGetReq(fichero, HTTPResponse.Status.OK);
					}
				} else if (parser.getMethod().equals("POST")) {
					System.err.println("post");
					String str = new String(parser.getBody().array());
					String res[] = new String[2];
					parseBodyPost(str, res);
					return handlePostReq(res[0], res[1]);
				} else {
					return handleGetReq(new File("notimplemented.html"), HTTPResponse.Status.NOT_IMPLEMENTED);
				}
			} else {
				return handleGetReq(new File("forbidden.html"), HTTPResponse.Status.FORBIDDEN);
			}
		} else {
			return handleGetReq(new File("badrequest.html"), HTTPResponse.Status.BAD_REQUEST);
		}
	}

	/**
	 * Metodo que trata un peticion HTTP GET
	 * 
	 * @param f
	 *            fichero cuyo contenido va a ser introducido como cuerpo a de
	 *            la respuesta
	 * @param s
	 *            el codigo de estado de la peticion
	 * @return respuesta HTTP con el codigo de estado dado y con el cuerpo igual
	 *         al contenido del fichero dado
	 * @throws IOException
	 */
	private static HTTPResponse handleGetReq(File f, HTTPResponse.Status s) throws IOException {
		FileChannel fChnl = FileChannel.open(f.toPath(), StandardOpenOption.READ);
		HTTPResponse res = new HTTPResponse(s, Files.probeContentType(f.toPath()), null, fChnl);
		return res;
	}

	/**
	 * Metodo que trata una peticion HTTP POST
	 * 
	 * @param nombre
	 *            nombre del fichero a escribir
	 * @param contenido
	 *            contenido que se quiere escribir en el fichero
	 * @return respuesta a enviar al cliente, para decirle que se ha escrito
	 *         correctamente
	 * @throws IOException
	 */
	private static HTTPResponse handlePostReq(String nombre, String contenido) throws IOException {
		File f = new File(nombre);
		f.createNewFile();
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));
		byte bytesC[] = contenido.getBytes();
		out.write(bytesC);
		out.close();
		String msg = makePostOkMessage(nombre, contenido);
		return new HTTPResponse(HTTPResponse.Status.OK, "text/html", msg.getBytes(), null);

	}

	/**
	 * Metodo que devuelve una cadena de texto que representa el cuerpo de la
	 * respuesta a enviar al cliente cuando este hace una peticion post
	 * 
	 * @param nombre
	 *            nombre del fichero en el que se ha escrito
	 * @param contenido
	 *            contenido que se ha escrito en el fichero
	 * @return cadena de texto
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

	/**
	 * Metodo que devuelve si el fichero dado esta en el directorio de trabajo
	 * actual
	 * 
	 * @param fichero
	 *            representacion abstracta del fichero a comprobar
	 * @return true en caso de que el fichero este en el directorio del trabajo
	 *         actual, false en caso contrario
	 */
	private static boolean isInWorkingDirectory(File fichero) {
		String p = fichero.toString();
		int numSep = p.length() - p.replace(File.separator, "").length();
		return numSep == 1;
	}

	/**
	 * Metodo que parsea el cuerpo de la peticion POST
	 * 
	 * @param str
	 *            cadena en formato application/x-www-form-urlencoded que hay
	 *            que parsear
	 * @param res
	 *            array donde se mete en la posicion 0 el nombre del fichero a
	 *            escribir y en la posicion 1 el contenido a escribir en el
	 *            fichero
	 * 
	 * @throws UnsupportedEncodingException
	 */
	private static void parseBodyPost(String str, String res[]) throws UnsupportedEncodingException {
		String splitStr[] = str.split("&");
		res[0] = URLDecoder.decode(splitStr[0].substring(splitStr[0].indexOf("=") + 1), "UTF-8");
		res[1] = URLDecoder.decode(splitStr[1].substring(splitStr[1].indexOf("=") + 1), "UTF-8");
	}
}
