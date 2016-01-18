package app.kit.exception;

public class HttpServiceException extends Exception {

	private static final long serialVersionUID = 1L;

	public HttpServiceException() {
		super();
	}
	
	public HttpServiceException(String message) {
		super(message);
	}
	
	public HttpServiceException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public HttpServiceException(Throwable cause) {
		super(cause);
	}
	
	protected HttpServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
