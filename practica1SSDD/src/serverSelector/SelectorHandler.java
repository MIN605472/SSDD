/*
 * AUTOR: Marius Nemtanu, Pablo Piedrafita
 * NIA: 605472, 691812
 * FICHERO: SelectorHandler.java
 * TIEMPO: 17 horas en comun todo el programa
 * DESCRIPCION: el fichero contiene una clase que tiene metodos para tratar las operaciones de 
 * lectura, escritura y aceptacion de un socketchannel
 * 
 */
package serverSelector;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Clase que tiene metodos para tratar las operacioens de lectura, escritura y
 * aceptacion de un socketchannel
 *
 */
public class SelectorHandler {

	private int bufSize;

	public SelectorHandler(int bufSize) {
		this.bufSize = bufSize;
	}

	/**
	 * Metodo que trata un accept
	 * 
	 * @param key
	 *            key que esta lista para aceptar conexion
	 * @throws IOException
	 */
	public void handleAccept(SelectionKey key) throws IOException {
		SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
		clntChan.configureBlocking(false);
		clntChan.register(key.selector(), SelectionKey.OP_READ,
				new Pair<HTTPParser, HTTPResponse>(new HTTPParser(), null));
	}

	/**
	 * Metodo que trata una lectura
	 * 
	 * @param key
	 *            key que esta lista para leer
	 * @throws IOException
	 */
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

	/**
	 * Metodo que trata una escritura
	 * 
	 * @param key
	 *            key que esta lista para escribir
	 * @throws IOException
	 */
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
