/*
 * AUTOR: Marius Nemtanu, Pablo Piedrafita
 * NIA: 605472, 691812
 * FICHERO: Startup.java
 * TIEMPO:
 * DESCRIPCION: Fichero que contiene una clase usada para el lanzamiento 
 * de otros main
 * 
 */

/**
 * Clase usada para lanzar otros main
 *
 */
public class Startup {

    private static final String MENSAJE_ERROR = "Los parametros son -c, -a o -u";

    public static void main(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException(MENSAJE_ERROR);
        }

        String newArgs[] = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length - 1);
        if (args[0].equals("-c")) {
            WorkerServer.main(newArgs);
        } else if (args[0].equals("-a")) {
            WorkerFactoryServer.main(newArgs);
        } else if (args[0].equals("-u")) {
            Cliente.main(newArgs);
        } else {
            throw new IllegalArgumentException(MENSAJE_ERROR);
        }
    }

}
