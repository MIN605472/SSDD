package serverSelector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class TestingClient {

	public static void main(String[] args) throws IOException {
		SocketChannel socket = SocketChannel.open(new InetSocketAddress("localhost", 5555));
		byte[] msg = makeNotImplemented().getBytes();
		ByteBuffer req = ByteBuffer.allocate(msg.length).put(msg);
		req.flip();
		socket.write(req);
		ByteBuffer buf = ByteBuffer.allocate(200);
		socket.read(buf);
		buf.flip();
		System.err.println(new String(buf.array()));

	}

	public static String makeNotImplemented() {
		StringBuffer str = new StringBuffer();
		str.append("HEAD /foo.txt HTTP/1.1\r\n\r\n");
		return str.toString();
	}

}
