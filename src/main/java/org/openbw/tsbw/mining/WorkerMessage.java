package org.openbw.tsbw.mining;

public abstract class WorkerMessage {

	protected String senderID;
	
	public WorkerMessage(String senderID) {
	
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
