package io.pivotal.demo.sko.util;

import java.io.Serializable;

public class MappedTransaction implements Serializable{

	private long id;
	private double value;
	private long timestamp;
	private String location;
	private boolean suspect;
	
	public MappedTransaction(long id, double value, String location, boolean suspect, 
			long timestamp) {
		this.id = id;
		this.value = value;
		this.location = location;
		this.timestamp = timestamp;
		this.suspect = suspect;

	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public boolean isSuspect() {
		return suspect;
	}
	public void setSuspect(boolean suspect) {
		this.suspect = suspect;
	}
	
	
}
