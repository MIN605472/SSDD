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

public class HTTPRequestHandler {

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

	private static HTTPResponse handleGetReq(File f, HTTPResponse.Status s) throws IOException {
		FileChannel fChnl = FileChannel.open(f.toPath(), StandardOpenOption.READ);
		HTTPResponse res = new HTTPResponse(s, Files.probeContentType(f.toPath()), null, fChnl);
		return res;
	}

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

	private static String makePostOkMessage(String nombre, String contenido) {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(
				"<html><head>\n<title>��xito!</title>\n</head><body>\n<h1>��xito!</h1>\n<p>Se ha escrito lo siguiente en el fichero ");
		strBuf.append(nombre);
		strBuf.append(":</p>\n<pre>");
		strBuf.append(contenido);
		strBuf.append("</pre>\n</body></html>");
		return strBuf.toString();
	}

	private static boolean isInWorkingDirectory(File fichero) {
		String p = fichero.toString();
		int numSep = p.length() - p.replace(File.separator, "").length();
		return numSep == 1;
	}

	private static void parseBodyPost(String str, String res[]) throws UnsupportedEncodingException {
		String splitStr[] = str.split("&");
		res[0] = URLDecoder.decode(splitStr[0].substring(splitStr[0].indexOf("=") + 1), "UTF-8");
		res[1] = URLDecoder.decode(splitStr[1].substring(splitStr[1].indexOf("=") + 1), "UTF-8");
	}
}
