package com.jeffthefate.dmbquiz;

import java.io.Serializable;

public class SetInfo implements Serializable {

	private static final long serialVersionUID = 2188748545860423285L;
	
	private String setVenue;
	private String setCity;
	private String setDate;
	private String setStamp;
	private String setlist;
	private String key;
	private boolean isArchive = false;
	
	public SetInfo() {}
	
	public String getSetVenue() {
		return setVenue;
	}

	public void setSetVenue(String setVenue) {
		this.setVenue = setVenue;
	}

	public String getSetCity() {
		return setCity;
	}

	public void setSetCity(String setCity) {
		this.setCity = setCity;
	}

	public String getSetDate() {
		return setDate;
	}

	public void setSetDate(String setDate) {
		this.setDate = setDate;
	}

	public String getSetStamp() {
		return setStamp;
	}

	public void setSetStamp(String setStamp) {
		this.setStamp = setStamp;
	}

	public String getSetlist() {
		return setlist;
	}

	public void setSetlist(String setlist) {
		this.setlist = setlist;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public boolean isArchive() {
		return isArchive;
	}

	public void setArchive(boolean isArchive) {
		this.isArchive = isArchive;
	}
	
	@Override
	public String toString() {
		return "setVenue: " + getSetVenue() + ", setCity: " + getSetCity() +
				", setDate: " + getSetDate() + ", setStamp: " + getSetStamp() +
				", setlist: " + getSetlist() + ", key: " + getKey() +
				", isArchive: " + isArchive();
	}
	
}