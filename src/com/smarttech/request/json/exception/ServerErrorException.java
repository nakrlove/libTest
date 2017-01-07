package com.smarttech.request.json.exception;


/**
 */
public class ServerErrorException extends Exception {
	
	
	private String detailMessage ;
	public ServerErrorException(String detailMessage, Throwable throwable) {
	    super(detailMessage, throwable);
	    this.detailMessage = detailMessage;
	}
	    
	public ServerErrorException(String detailMessage) {
	    super(detailMessage);
	    this.detailMessage = detailMessage;
	}
	  
	  
	@Override
	public String getMessage() {
	     return detailMessage;
	}
}
