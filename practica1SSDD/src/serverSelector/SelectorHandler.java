package serverSelector;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class SelectorHandler {

	private int bufSize; // Size of I/O buffer

	public SelectorHandler(int bufSize) {
		this.bufSize = bufSize;
	}

	public void handleAccept(SelectionKey key) throws IOException {
		SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
		clntChan.configureBlocking(false); // Must be nonblocking to register
		// Register the selector with new channel for read and attach byte
		// buffer
		/** Se ha sustituido el buffer por HttpParser **/
		clntChan.register(key.selector(), SelectionKey.OP_READ, new HTTPParser());
	}

	public void handleRead(SelectionKey key) throws IOException {
		// Client socket channel has pending data
		SocketChannel clntChan = (SocketChannel) key.channel();
		ByteBuffer buf = ByteBuffer.allocate(bufSize);
		long bytesRead = clntChan.read(buf);
		HTTPParser parser = (HTTPParser) key.attachment();
		parser.parseRequest(buf);
		if (parser.isComplete() || parser.failed())
			key.interestOps(SelectionKey.OP_WRITE);
		if (bytesRead == -1) { // Did the other end close?
			clntChan.close();
		} else if (bytesRead > 0) {
			// Indicate via key that reading/writing are both of interest now.
			key.interestOps(SelectionKey.OP_READ);
		}
	}

	public void handleWrite(SelectionKey key) throws IOException {
		/*
		 * Channel is available for writing, and key is valid (i.e., client
		 * channel not closed).
		 */
		// Retrieve data read earlier
		HTTPParser parser = (HTTPParser) key.attachment();		
		SocketChannel clntChan = (SocketChannel) key.channel();
		RequestHandler handler = new RequestHandler(parser.getPath(),parser.getMethod(),parser.getBody(),bufSize,clntChan);
		clntChan.write(buf);
		if (!buf.hasRemaining()) { // Buffer completely written?
			// Nothing left, so no longer interested in writes
			key.interestOps(SelectionKey.OP_READ);
		}
		buf.compact(); // Make room for more data to be read in
	}
}
