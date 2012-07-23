package org.fer.urgence;

public class Options {
	private boolean sendSmsOnTry;

	public Options() {
		super();
	}
	
	public boolean isSendSmsOnTry() {
		return sendSmsOnTry;
	}

	public void setSendSmsOnTry(boolean sendSmsOnTry) {
		this.sendSmsOnTry = sendSmsOnTry;
	}
	
}
