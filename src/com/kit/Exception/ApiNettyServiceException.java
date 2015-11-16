package com.kit.Exception;

public class ApiNettyServiceException extends Exception {

	private static final long serialVersionUID = 1L;

	public ApiNettyServiceException() {
		super();
	}
	
	public ApiNettyServiceException(String message) {
		super(message);
	}
	
	public ApiNettyServiceException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ApiNettyServiceException(Throwable cause) {
		super(cause);
	}
	
	protected ApiNettyServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
