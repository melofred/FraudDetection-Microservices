package io.pivotal.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties
public class CartItem {

	private long customerId;
	private long productId;

	public CartItem(){
		   
	}

	
	public CartItem(long customerId, long productId) {
		super();
		this.customerId = customerId;
		this.productId = productId;
	}


	public long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(long customerId) {
		this.customerId = customerId;
	}

	public long getProductId() {
		return productId;
	}

	public void setProductId(long productId) {
		this.productId = productId;
	}
	
    @Override
    public String toString() {
        return "CustomerProductOrder{" +
                "customer='" + customerId + '\'' +
                ", product=" + productId +
                '}';
    }	   
	
}
