package serverThread;

public class HTTPResponse {
	private static final String VERSION = "1.1";
	private byte[] content;
	private String contentType;
	private Status code;

	public HTTPResponse(Status code, String contentType, byte[] content){
		this.code = code;
		this.contentType = contentType;
		this.content=content;
	}

	private String headers(){
		StringBuffer str = new StringBuffer("HTTP/");
		str.append(VERSION);
		str.append(" ");
		str.append(code.getCode());
		str.append(" ");
		str.append(code.getDescription());
		str.append("\nContent-Type: ");
		str.append(contentType);
		str.append("\nContent-Length: ");
		str.append(content.length);
		str.append("\n\n");
		return str.toString();
	}
	
	public byte[] toBytes(){
		byte[] header = headers().getBytes();
		byte[] respuesta = new byte[header.length+content.length];
		System.arraycopy(header, 0, respuesta, 0, header.length);
		System.arraycopy(content, 0, respuesta, header.length, content.length);
		return respuesta;
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
