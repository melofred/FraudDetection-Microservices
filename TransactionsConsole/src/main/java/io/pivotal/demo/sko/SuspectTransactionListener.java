package io.pivotal.demo.sko;
import io.pivotal.demo.sko.util.GeodeClient;
import io.pivotal.demo.sko.util.TransactionsMap;

import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;
import com.gemstone.gemfire.pdx.PdxInstance;


public class SuspectTransactionListener extends CacheListenerAdapter
		implements Declarable {

	@Override
	public void init(Properties arg0) {
	}

	@Override
	public void afterCreate(EntryEvent event) {
		suspectTransactionFound(event);
	}

	@Override
	public void afterUpdate(EntryEvent event) {
		suspectTransactionFound(event);
	}

	
	public void suspectTransactionFound(EntryEvent e){
	
		String value = e.getNewValue().toString();

		// parse 
		
		ObjectMapper mapper = new ObjectMapper();
		Map<String,Object> objects =  null;
		
		try{
			objects = mapper.readValue(value, Map.class);
			
			
		}
		catch(Exception ex){
			throw new RuntimeException(ex);
		}
		
		long transactionId;
		long deviceId;
		double transactionValue;
		long timestamp;
		
		transactionId = Long.parseLong(objects.get("id").toString());
		try{
			deviceId = Long.parseLong(objects.get("deviceId").toString());
			transactionValue = Double.parseDouble(objects.get("value").toString());			
			timestamp = Long.parseLong(objects.get("timestamp").toString());
			String location = GeodeClient.getInstance().getPoSLocation(deviceId).trim();
			
			TransactionsMap.suspiciousTransactions.addTransaction(transactionId, transactionValue, location, true, timestamp);
		}
		catch(IllegalArgumentException ie){
			// This usually means a suspect based on a transaction row not available anymore in Gem (for example, expired)
			// ignore.				
		}
			

		
	}
	
	

}
