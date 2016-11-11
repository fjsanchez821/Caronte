package es.carm.figesper.caronte;

public class ContextException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ContextException(String message) {
		super(message);
	}
	
	public ContextException(String message, Throwable cause) {
		super(message, cause);
	}	
}
