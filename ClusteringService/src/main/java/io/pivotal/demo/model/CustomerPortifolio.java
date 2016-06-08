package io.pivotal.demo.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties
public class CustomerPortifolio {

	private Long customerId;
	private List<CustomerPortifolioItem> products = new ArrayList<CustomerPortifolioItem>();
	
	public CustomerPortifolio(){		
	}
	
	public CustomerPortifolio(Long customerId){
		this.customerId = customerId;
	}
	
	
	public void add(Long productId, boolean bought) {
		products.add(new CustomerPortifolioItem(productId, bought));
	}	
	
	
	public Long getCustomerId() {
		return customerId;
	}
	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}
	public List<CustomerPortifolioItem> getProducts() {
		return products;
	}
	public void setProducts(List<CustomerPortifolioItem> products) {
		this.products = products;
	}
	
	
}
