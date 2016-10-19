package serverSelector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

public class ServidorSelect {
	private static final int BUFSIZE = 4096;

	public static void main(String[] args) {
		System.out.println(args[0]);
		if (args.length < 1) {
			throw new IllegalArgumentException("Parametros(s): <Port> ...");
		}

		ServerSocketChannel serverSocket;
		Selector selector = null;
		SelectorHandler protocol = new SelectorHandler(BUFSIZE);

		try {
			serverSocket = ServerSocketChannel.open();
			serverSocket.bind(new InetSocketAddress("localhost", Integer.parseInt(args[0])));
			serverSocket.configureBlocking(false);
			selector = Selector.open();
			serverSocket.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			e.printStackTrace();
		}

		while (true) {
			SelectionKey key = null;
			try {
				selector.select();
				Iterator<SelectionKey> it = selector.selectedKeys().iterator();
				while (it.hasNext()) {
					key = it.next();
					if (key.isAcceptable()) {
						protocol.handleAccept(key);
					}
					if (key.isReadable()) {
						protocol.handleRead(key);
					}

					if (key.isValid() && key.isWritable()) {
						protocol.handleWrite(key);

					}
					it.remove();
				}
			} catch (IOException e) {
				key.cancel();
				e.printStackTrace();
			}
		}
	}

}
