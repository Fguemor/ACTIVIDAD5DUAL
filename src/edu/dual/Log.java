package edu.dual;

/**
 * Clase con los atributos de un logger en xml
 * @author Fátima Guerrero
 * @version 1.0
 */
public class Log {
	//declaración de variables de la clase Log
	private String date,logger,level,message;

	/**
	 * Constuctor con los parámetros de registros
	 * @param date fecha del registro
	 * @param logger objeto logger que creó el registro
	 * @param level nivel del log
	 * @param mensaje mensaje del log
	 */
	public Log(String date, String logger, String level, String mensaje) {
		super();
		this.date = date;
		this.logger = logger;
		this.level = level;
		this.message = mensaje;
	}
	//GETTERS Y SETTERS

	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getLogger() {
		return logger;
	}
	public void setLogger(String logger) {
		this.logger = logger;
	}
	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getMensaje() {
		return message;
	}

	public void setMensaje(String mensaje) {
		this.message = mensaje;
	}
	/**
	 * Método sobreescrito to string
	 */
	@Override
	public String toString() {
		return "Log [date=" + date + ", logger=" + logger + ", level=" + level + ", mensaje=" + message + "]";
	}



}
