/*
 * AUTOR: Marius Nemtanu, Pablo Piedrafita
 * NIA: 605472, 691812
 * FICHERO: HTTPResponse.java
 * TIEMPO: 17 horas en común todo el programa
 * DESCRIPCION: el fichero contiene una clase que representa una respuesta devuelta por el servidor HTTP al cliente
 */
package serverThread;

public class HTTPResponse {
	private static final String VERSION = "1.1";
	private byte[] content;
	private String contentType;
	private Status code;

	/**
	 * 
	 * @param code
	 * @param contentType
	 * @param content
	 */
	public HTTPResponse(Status code, String contentType, byte[] content) {
		this.code = code;
		this.contentType = contentType;
		this.content = content;
	}

	/**
	 * Genera la cabecera HTTP
	 * 
	 * @return Devuelve la cabecera HTTP generada
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
		str.append(content.length);
		str.append("\r\n\r\n");
		return str.toString();
	}

	/**
	 * 
	 * @return Devuelve en forma de array de bytes la respuesta generada. Esto
	 *         permite pasarlo directamente al socket.
	 */
	public byte[] toBytes() {
		byte[] header = headers().getBytes();
		byte[] respuesta = new byte[header.length + content.length];
		System.arraycopy(header, 0, respuesta, 0, header.length);
		// Concatenamos cabecera y contenido
		System.arraycopy(content, 0, respuesta, header.length, content.length);
		return respuesta;
	}

	/**
	 * Enumeracion de los distintos codigos de estado de una respuesta HTTP
	 * junto con un pequeña descripicion de estos
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

		public String getDescription() {
			return description;
		}

		public int getCode() {
			return code;
		}
	}

}
