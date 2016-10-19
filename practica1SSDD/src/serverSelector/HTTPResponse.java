package serverSelector;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class HTTPResponse {
	private static final String VERSION = "1.1";
	private ByteBuffer response;
	private String contentType;
	private Status code;
	private FileChannel fChnl;

	public HTTPResponse(Status code, String contentType, ByteBuffer body, FileChannel fChnl) throws IOException {
		this.code = code;
		this.contentType = contentType;
		this.fChnl = fChnl;
		if (body == null) {
			response = ByteBuffer.allocate(4096);
			response.put(headers().getBytes());
		} else {
			response = ByteBuffer	.allocate(headers().getBytes().length + body.capacity());
			response.put(headers().getBytes());
			response.put(body);
			response.flip();
		}
	}

	private String headers() throws IOException {
		StringBuffer str = new StringBuffer("HTTP/");
		str.append(VERSION);
		str.append(" ");
		str.append(code.getCode());
		str.append(" ");
		str.append(code.getDescription());
		str.append("\nContent-Type: ");
		str.append(contentType);
		str.append("\nContent-Length: ");
		str.append(fChnl.size());
		str.append("\n\n");
		return str.toString();
	}

	public void send(SocketChannel socket) throws IOException {
		fChnl.read(response);
		response.flip();
		socket.write(response);
		response.compact();
	}

	public boolean hasRemaining() throws IOException {
		return fChnl.position() != fChnl.size() || response.position() != 0;
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
