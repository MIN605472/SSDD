import serverSelector.ServidorSelect;
import serverThread.ServidorThreads;

public class ServidorHTTP {

	public static void main(String[] args) {
		if (args.length != 2 || !((args[0].equals("-s")) || args[0].equals("-t"))) {
			throw new IllegalArgumentException("Parametros: -t|-s puerto ");
		}
		if (args[0].equals("-t")) {
			ServidorThreads.main(new String[] { args[1] });
		} else if (args[0].equals("-s")) {
			ServidorSelect.main(new String[] { args[1] });
		}
	}

}
