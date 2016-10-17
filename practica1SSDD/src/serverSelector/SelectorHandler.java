package serverSelector;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class SelectorHandler {

	private int bufSize; // Size of I/O buffer

	public SelectorHandler(int bufSize) {
		this.bufSize = bufSize;
	}

	public void handleAccept(SelectionKey key) throws IOException {
		System.err.println("handleAccept");
		SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
		clntChan.configureBlocking(false); 
		clntChan.register(key.selector(), SelectionKey.OP_READ,
				new Pair<HTTPParser, FileChannel>(new HTTPParser(), null));
	}

	public void handleRead(SelectionKey key) throws IOException {
		System.err.println("handleRead");
		SocketChannel clntChan = (SocketChannel) key.channel();
		@SuppressWarnings("unchecked")
		Pair<HTTPParser, FileChannel> pair = (Pair<HTTPParser, FileChannel>) key.attachment();
		ByteBuffer buf = ByteBuffer.allocate(bufSize);
		int bytesRead = clntChan.read(buf);
		buf.flip();
		System.err.println("Leidos: " + bytesRead);

		String s = new String(buf.array(), Charset.forName("UTF-8"));
		System.err.println("hola " + s);
		HTTPParser parser = pair.getFirst();
		parser.parseRequest(buf);
		System.err.println("Metodo: " + parser.getMethod());
		System.err.println("Path: " + parser.getPath());
		System.err.println("Body: " + parser.getBody());

		if (parser.isComplete() || parser.failed()) {
			System.err.println("iscoplmete o failed");
			key.interestOps(SelectionKey.OP_WRITE);
		} else {
			if (bytesRead == -1) {
				clntChan.close();
			} else if (bytesRead > 0) {
				key.interestOps(SelectionKey.OP_READ);
			}
		}
	}

	public void handleWrite(SelectionKey key) throws IOException {
		System.err.println("handleWrite");
		@SuppressWarnings("unchecked")
		Pair<HTTPParser, FileChannel> pair = (Pair<HTTPParser, FileChannel>) key.attachment();
		SocketChannel clntChan = (SocketChannel) key.channel();
		RequestHandler handler = new RequestHandler(pair, bufSize, clntChan);
		handler.handle();
	}
}
