package org.openbw.tsbw.micro;

public abstract class Message {

	protected String senderID;
	
	public Message(String senderID) {
	
		this.senderID = senderID;
	}
	
	public String getSenderID() {
		
		return this.senderID;
	}

	@Override
	public String toString() {
		
		return "message from " + senderID;
	}
}
