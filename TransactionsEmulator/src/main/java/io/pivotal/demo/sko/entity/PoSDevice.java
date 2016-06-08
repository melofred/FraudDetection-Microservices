package io.pivotal.demo.sko.entity;

import org.springframework.context.annotation.ComponentScan;

@ComponentScan
public class PoSDevice {

	private long id;
	private String location; 
	/* location can be a ZIP code or county name, that would be mapped on D3 US MAP.
	 * Good transactions would be green, possible frauds would be red. 
	 * Also a box in a side showing stats about frauds:  number total, per state, etc.
	 * http://bl.ocks.org/mbostock/4965422
	 * http://bl.ocks.org/mbostock/9943478
	 * http://www.scriptscoop.net/t/ec57d05b18cd/d3.js-dynamically-adjust-bubble-radius-of-counties.html
	 */
	private String merchantName;
	
	
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	
	
	
}
