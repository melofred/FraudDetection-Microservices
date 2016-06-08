package io.pivotal.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties
public class CustomerPortifolioItem {

	private Long productId;
	private int bought; 
	
	public CustomerPortifolioItem() {}
	
	public CustomerPortifolioItem(Long productId, boolean bought){
		this.productId = productId;
		this.bought = (bought?1:0);
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public int getBought() {
		return bought;
	}

	public void setBought(int bought) {
		this.bought = bought;
	}	
	
	
	
}
