/*
* AUTOR: Marius Nemtanu, Pablo Piedrafita
* NIA: 605472, 691812
* FICHERO: Carta.java
* TIEMPO: 5 horas
* DESCRIPCI’ON: Este fichero contiene la clase Carta, que representa una carta
* nos permite poner el remitente y el destinatario en formato de texto.
*/
package ssdd.app;

import java.io.Serializable;

public class Carta implements Serializable{
	private static final long serialVersionUID = 1L;
	private String remitente;
	private String destinatario;
	private boolean mencion;
	private String cuerpo;	
	
	public Carta(String rem, String dest, String cuerpo, boolean mencion){
		this.remitente = rem;
		this.destinatario = dest;
		this.cuerpo = cuerpo;
		this.mencion = mencion;
	}
	
	public String getRemitente() {
		return remitente;
	}

	public void setRemitente(String remitente) {
		this.remitente = remitente;
	}

	public String getDestinatario() {
		return destinatario;
	}

	public void setDestinatario(String destinatario) {
		this.destinatario = destinatario;
	}

	public String getCuerpo() {
		return cuerpo;
	}

	public void setCuerpo(String cuerpo) {
		this.cuerpo = cuerpo;
	}

	public boolean isMencion() {
		return mencion;
	}

	public void setMencion(boolean mencion) {
		this.mencion = mencion;
	}

	public String toString(){
		String carta = "Remitente: "+this.remitente+".\nDestinatario: "+this.destinatario+".\nMensaje: "+this.cuerpo;
		return carta;
		
	}
	
}
