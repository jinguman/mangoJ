package app.kit.exception;

public class RequestParamException extends Exception {

	public RequestParamException() {
		super();
	}
	
	public RequestParamException(String message) {
		super(message);
	}
	
	public RequestParamException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public RequestParamException(Throwable cause) {
		super(cause);
	}
	
	protected RequestParamException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
