package com.smarttech.request.json.exception;

/**
 * Network check
 */
public class  SmartTechNetworkException extends Exception{
	private String detailMessage ;
	public SmartTechNetworkException(String detailMessage, Throwable throwable) {
	    super(detailMessage, throwable);
	    this.detailMessage = detailMessage;
	}
	    
	public SmartTechNetworkException(String detailMessage) {
	    super(detailMessage);
	    this.detailMessage = detailMessage;
	}
	  
	  
	@Override
	public String getMessage() {
	     return detailMessage;
	}
}