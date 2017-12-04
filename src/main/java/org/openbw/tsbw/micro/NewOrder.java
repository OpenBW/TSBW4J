package org.openbw.tsbw.micro;

public class NewOrder extends Message {

	public NewOrder() {
		
		super("");
	}
	
	@Override
	public String toString() {
		
		return "new order issued message";
	}
}
