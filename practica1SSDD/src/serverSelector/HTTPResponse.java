package serverSelector;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * Clase que representa una respuesta devuelta por el servidor HTTP al cliente
 *
 */
public class HTTPResponse {
	private static final String VERSION = "1.1";
	private ByteBuffer response;
	private String contentType;
	private Status code;
	private FileChannel fChnl;
	private long size;

	/**
	 * A la hora de constuir el objeto hay que decidir si el cuerpo de la
	 * respuesta proviene o no de un fichero. Si contenido del cuerpo esta en un
	 * fichero utilizar fChnl y dejar body a null. Si por el contrario el cuerpo
	 * no esta en un fichero usar el parametro body y dejar fChnl a null. En
	 * caso de que alguno de los dos parametros (body y fChnl) no este a null el
	 * comportamiento de los metodos del objeto no estan definidos
	 * 
	 * @param code
	 *            codigo del estado de la respuesta
	 * @param contentType
	 *            tipo MIME del contenido del cuerpo
	 * @param body
	 *            cuerpo de la respuesta en caso de que el cuerpo sea algo que
	 *            no es leido de un fichero
	 * @param fChnl
	 *            cuerpo de la respuesta en caso de que el cuerpo sea algo leido
	 *            de un fichero
	 * @throws IOException
	 */
	public HTTPResponse(Status code, String contentType, byte body[], FileChannel fChnl) throws IOException {
		this.code = code;
		this.contentType = contentType;
		this.fChnl = fChnl;
		if (body == null) {
			size = fChnl.size();
			response = ByteBuffer.allocate(4096);
			response.put(headers().getBytes());
		} else {
			size = body.length;
			byte[] hBytes = headers().getBytes();
			response = ByteBuffer.allocate(hBytes.length + body.length);
			response.put(hBytes);
			response.put(body);
		}

	}

	private String headers() {
		StringBuffer str = new StringBuffer("HTTP/");
		str.append(VERSION);
		str.append(" ");
		str.append(code.getCode());
		str.append(" ");
		str.append(code.getDescription());
		str.append("\nContent-Type: ");
		str.append(contentType);
		str.append("\nContent-Length: ");
		str.append(size);
		str.append("\n\n");
		return str.toString();
	}

	public void send(SocketChannel socket) throws IOException {
		if (fChnl != null) {
			fChnl.read(response);
		}
		response.flip();
		socket.write(response);
		response.compact();
	}

	public boolean hasRemaining() throws IOException {
		if (fChnl != null) {
			return fChnl.position() != fChnl.size() || response.position() != 0;
		} else {
			return response.position() != 0;
		}

	}

	public enum Status {
		NOT_FOUND(404, "Not Found"), OK(200, "OK"), FORBIDDEN(403, "403 Forbidden"), BAD_REQUEST(400,
				"Bad Request"), NOT_IMPLEMENTED(501, "Not Implemented");

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

}
