package io.pivotal.demo.sko;
import io.pivotal.demo.sko.util.GeodeClient;
import io.pivotal.demo.sko.util.TransactionsMap;

import java.util.Properties;

import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;
import com.gemstone.gemfire.pdx.PdxInstance;


public class TransactionListener extends CacheListenerAdapter
		implements Declarable {

	@Override
	public void init(Properties arg0) {
		System.out.println("starting listener");
	}

	@Override
	public void afterCreate(EntryEvent event) {
		transactionArrived(event);
	}

	@Override
	public void afterUpdate(EntryEvent event) {
		transactionArrived(event);
	}
	
	

	
	public void transactionArrived(EntryEvent e){
	
		Object obj = e.getNewValue();

		long transactionId;
		long deviceId;
		double value;
		long timestamp;
		if (obj instanceof PdxInstance){		
			
			transactionId = ((Number)((PdxInstance)obj).getField("id")).longValue();
			deviceId = ((Number)((PdxInstance)obj).getField("deviceId")).longValue();
			value = ((Number)((PdxInstance)obj).getField("value")).longValue();			
			timestamp = ((Number)((PdxInstance)obj).getField("timestamp")).longValue();
			
			String location = GeodeClient.getInstance().getPoSLocation(deviceId);
			
			TransactionsMap.latestTransactions.addTransaction(transactionId, value, location, timestamp);
			
		}
		else throw new RuntimeException("new object is not PDX Instance.. it came as "+obj.getClass());
		
		
		
		
	}
	
	

}
