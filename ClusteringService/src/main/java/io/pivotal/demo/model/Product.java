package io.pivotal.demo.model;

public enum Product {

	BABY_DIAPERS(0),
	BABY_SHAMPOO(1),
	BABY_WIPES(2),
	BABY_CRIB(3),
	
	TV_SERIES_BOX (4),
	TV(5),
	IPHONE(6),
	IPHONE_CASE(7),
	APPLE_WATCH (8),
	MACBOOK(9),
	
	CAT_DRY_FOOD(10),
	CAT_WET_FOOD(11),
	CAT_LITTER(12);
	
	
	private final long productId;
	
	Product(long productId){
		this.productId = productId;
	}
	
	public long getProductId(){
		return productId;
	}
	

	
}
