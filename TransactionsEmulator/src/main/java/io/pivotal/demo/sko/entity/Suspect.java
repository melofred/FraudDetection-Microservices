package io.pivotal.demo.sko.entity;

import org.springframework.context.annotation.ComponentScan;

@ComponentScan
public class Suspect {

	private long transactionId;
	private long deviceId;
	private long markedSuspectMillis;
	private String reason;

	
	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(long transactionId) {
		this.transactionId = transactionId;
	}

	public long getMarkedSuspectMillis() {
		return markedSuspectMillis;
	}

	public void setMarkedSuspectMillis(long markedSuspectMillis) {
		this.markedSuspectMillis = markedSuspectMillis;
	}

	public long getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(long deviceId) {
		this.deviceId = deviceId;
	}
	
	
	
	
}
