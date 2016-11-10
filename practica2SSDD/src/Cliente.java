
/*
 * AUTOR: Marius Nemtanu, Pablo Piedrafita
 * NIA: 605472, 691812
 * FICHERO: Cliente.java
 * TIEMPO: 16 comunes horas todo el proyecto
 * DESCRIPCION: el fichero contiene la clase que representa un cliente que 
 * va a pedir a un objeto remoto (WorkerFactory) otros objetos remotos (Worker) 
 * que calculan numeros primos en un intervalo
 * 
 */

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Clase que representa un cliente que pide al objeto remoto WorkerFactory
 * otros objetos remotos, Worker, que calculan numeros primos en un intervalo
 * dado
 *
 */
public class Cliente {

    private static String dir;
    private static Map<Integer, ArrayList<Integer>> global;

    public static synchronized void print(String str) {
        System.err.println(str);
    }

    public static void main(String[] args) {
        if (args.length > 4 || args.length < 3) {
            throw new IllegalArgumentException(
                    "Los parametros del Cliente son: min max n [IP_registro]");
        }
        if (args.length == 3) {
            dir = "localhost";
        } else {
            dir = args[3];
        }

        Thread[] t;

        try {
            int min = Integer.parseInt(args[0]);
            int max = Integer.parseInt(args[1]);
            int n = Integer.parseInt(args[2]);
            Registry registry = LocateRegistry.getRegistry(dir);
            WorkerFactory factory = (WorkerFactory) registry
                    .lookup("WorkerFactory");
            ArrayList<Worker> workers = factory.dameWorkers(n);
            if (workers == null) {
                System.err.println("No hay tantos workers como ha pedido");
            } else {
                int q = (max - min) / n;
                int r = (max - min) % n;
                if (r != 0) {
                    t = new Thread[n + 1];
                    global = new ConcurrentHashMap<>(n + 1);
                } else {
                    t = new Thread[n];
                    global = new ConcurrentHashMap<>(n);

                }
                for (int i = 0; i < n; i++) {
                    t[i] = new Thread(new ThreadCalculo(workers.get(i),
                            min + q * i, min + q * i + q, i));
                }
                if (r != 0) {
                    t[n] = new Thread(new ThreadCalculo(workers.get(0),
                            max - r, max, n));
                }

                for (int i = 0; i < t.length; i++) {
                    t[i].start();
                }

                for (int i = 0; i < t.length; i++) {
                    try {
                        t[i].join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                mostrarPrimos(global);
            }

        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Los parametros deben de ser enteros.");
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    /**
     * Metodo que muestra por pantalla los numeros que haya en primos
     * 
     * @param primos
     *            una lista de listas que contienen enteros
     */
    private static void mostrarPrimos(
            Map<Integer, ArrayList<Integer>> primos) {
        for (int i = 0; i < primos.size(); i++) {
            ArrayList<Integer> aux = primos.get(i);
            for (Integer numPrimo : aux) {
                System.out.println(numPrimo);
            }
        }
    }

    /**
     * Clase en la que se calcula los numeros primos en un intervalo
     * 
     */
    private static class ThreadCalculo implements Runnable {

        private int min;
        private int max;
        private int i;
        private Worker worker;

        /**
         * Constructor de la clase
         * 
         * @param worker
         *            Worker asignado para calcular los numeros primos en un
         *            intervalo
         * @param min
         *            limite inferior del intervalo
         * @param max
         *            limite superior del intervalo
         * @param i
         *            posicion del intervalo dentro los distintos intervalos
         *            que se han creado, es decir, si tenemos el intervalo
         *            [0,4] y lo dividimos en [0,2] y [3,4], el primer
         *            intervalo ocupa la posicion 0 y el segundo la posicion 1
         */
        public ThreadCalculo(Worker worker, int min, int max, int i) {
            this.worker = worker;
            this.min = min;
            this.max = max;
            this.i = i;
        }

        public void run() {
            try {
                global.put(i, worker.encuentraPrimos(min, max));
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
    }

}
