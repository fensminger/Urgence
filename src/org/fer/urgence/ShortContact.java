package org.fer.urgence;

public class ShortContact {
	private CharSequence contactName;
	private CharSequence phoneNumber;
	private Boolean sms;
	
	public ShortContact() {
		super();
	}
	
	public ShortContact(CharSequence contactName, CharSequence phoneNumber, Boolean sms) {
		super();
		this.phoneNumber = phoneNumber;
		this.contactName = contactName;
		this.sms = sms;
	}
	
	public CharSequence getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(CharSequence phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public CharSequence getContactName() {
		return contactName;
	}
	public void setContactName(CharSequence contactName) {
		this.contactName = contactName;
	}
	public Boolean isSms() {
		return sms;
	}
	public void setSms(Boolean sms) {
		this.sms = sms;
	}
	
}
