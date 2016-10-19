package serverSelector;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class SelectorHandler {

	private int bufSize;

	public SelectorHandler(int bufSize) {
		this.bufSize = bufSize;
	}

	public void handleAccept(SelectionKey key) throws IOException {
		SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
		clntChan.configureBlocking(false);
		clntChan.register(key.selector(), SelectionKey.OP_READ,
				new Pair<HTTPParser, HTTPResponse>(new HTTPParser(), null));
	}

	public void handleRead(SelectionKey key) throws IOException {
		SocketChannel clntChan = (SocketChannel) key.channel();
		@SuppressWarnings("unchecked")
		Pair<HTTPParser, HTTPResponse> pair = (Pair<HTTPParser, HTTPResponse>) key.attachment();
		ByteBuffer buf = ByteBuffer.allocate(bufSize);
		int bytesRead = clntChan.read(buf);
		buf.flip();
		HTTPParser parser = pair.getFirst();
		parser.parseRequest(buf);
		if (parser.isComplete() || parser.failed()) {
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
		@SuppressWarnings("unchecked")
		Pair<HTTPParser, HTTPResponse> pair = (Pair<HTTPParser, HTTPResponse>) key.attachment();
		SocketChannel clntChan = (SocketChannel) key.channel();
		if (pair.getSecond() == null) {
			pair.setSecond(HTTPRequestHandler.handle(pair.getFirst()));
		}
		if (pair.getSecond().hasRemaining()) {
			pair.getSecond().send(clntChan);
		} else {
			clntChan.close();
		}
	}
}
