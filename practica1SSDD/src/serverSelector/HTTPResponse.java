/*
 * AUTOR: Marius Nemtanu, Pablo Piedrafita
 * NIA: 605472, 691812
 * FICHERO: HTTPResponse.java
 * TIEMPO: 17 horas en comun todo el programa
 * DESCRIPCION: el fichero contiene una clase que representa una respuesta devuelta 
 * por el servidor HTTP al cliente
 * 
 */

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
	 * A la hora de construir el objeto hay que decidir si el cuerpo de la
	 * respuesta proviene o no de un fichero. Si contenido del cuerpo esta en un
	 * fichero utilizar fChnl y dejar body a null. Si por el contrario el cuerpo
	 * no esta en un fichero usar el parametro body y dejar fChnl a null. En
	 * caso de que alguno de los dos parametros (body y fChnl) no este a null el
	 * comportamiento de los metodos del objeto no estan definido
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

	/**
	 * Metodo que devuelve los headers de esta respuesta
	 * 
	 * @return string con los headers de la respuesta
	 */
	private String headers() {
		StringBuffer str = new StringBuffer("HTTP/");
		str.append(VERSION);
		str.append(" ");
		str.append(code.getCode());
		str.append(" ");
		str.append(code.getDescription());
		str.append("\r\nContent-Type: ");
		str.append(contentType);
		str.append("\r\nContent-Length: ");
		str.append(size);
		str.append("\r\n\r\n");
		return str.toString();
	}

	/**
	 * Metodo que escribe esta respuesta en el socketchannel dados
	 * 
	 * @param socket
	 *            por donde enviar la respuesta
	 * @throws IOException
	 */
	public void send(SocketChannel socket) throws IOException {
		if (fChnl != null) {
			fChnl.read(response);
		}
		response.flip();
		socket.write(response);
		response.compact();
	}

	/**
	 * Metodo que dice si hay datos de esta respuesta por enviar
	 * 
	 * @return true en caso de que aun haya datos por enviar, false en caso
	 *         contrario
	 * @throws IOException
	 */
	public boolean hasRemaining() throws IOException {
		if (fChnl != null) {
			return fChnl.position() != fChnl.size() || response.position() != 0;
		} else {
			return response.position() != 0;
		}

	}

	/**
	 * Enumeracion de los distintos codigos de estado de una respuesta HTTP
	 * junto con un pequeña descripicion de estos
	 *
	 */
	public enum Status {
		NOT_FOUND(404, "Not Found"), OK(200, "OK"), FORBIDDEN(403, "403 Forbidden"), BAD_REQUEST(400,
				"Bad Request"), NOT_IMPLEMENTED(501, "Not Implemented");

		private int code;
		private String description;

		Status(int code, String description) {
			this.code = code;
			this.description = description;
		}

		/**
		 * Devuelve la descripcion del codigo de estado
		 * 
		 * @return una cadena con la descripcion del codigo de estado
		 */
		public String getDescription() {
			return description;
		}

		/**
		 * Devuelve el codigo de estado
		 * 
		 * @return entero con el codigo de estado
		 */
		public int getCode() {
			return code;
		}
	}

}
