package org.regenstrief.linkage.entropy;

public class EntropyProviderException extends Exception {
	public EntropyProviderException(){
		super();
	}
	
	public EntropyProviderException(String message){
		super(message);
	}
	
	public EntropyProviderException(String message, Throwable cause){
		super(message, cause);
	}
	
	public EntropyProviderException(Throwable cause){
		super(cause);
	}
	
}
