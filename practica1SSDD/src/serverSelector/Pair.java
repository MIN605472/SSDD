/*
 * AUTOR: Marius Nemtanu, Pablo Piedrafita
 * NIA: 605472, 691812
 * FICHERO: Pair.java
 * TIEMPO: 17 horas en comun todo el programa
 * DESCRIPCION: el fichero contiene una clase que representa un contenedor de un pares de objetos
 * 
 */

package serverSelector;

/**
 * Clase contenedora de un par de objetos
 *
 * @param <X>
 *            tipo del primer objeto
 * @param <Y>
 *            tipo del segundo objeto
 */
public class Pair<X, Y> {

	private X first;
	private Y second;

	public Pair(X first, Y second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * Devuelve el primer objeto del par
	 * 
	 * @return primer objeto del par
	 */
	public X getFirst() {
		return first;
	}

	/**
	 * Establece el primero objeto del par
	 * 
	 * @param first
	 *            ojeto que se quiere establecer
	 */
	public void setFirst(X first) {
		this.first = first;
	}

	/**
	 * Devuelve el segundo objeto del par
	 * 
	 * @return segundo objeto del par
	 */
	public Y getSecond() {
		return second;
	}

	/**
	 * Establece el segundo objeto del par
	 * 
	 * @param second
	 *            objeto que se quiere establecer
	 */
	public void setSecond(Y second) {
		this.second = second;
	}

}
