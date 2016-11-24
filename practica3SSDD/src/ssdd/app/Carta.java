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
