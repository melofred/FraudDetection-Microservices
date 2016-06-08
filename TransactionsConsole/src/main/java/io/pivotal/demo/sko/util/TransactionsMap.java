package io.pivotal.demo.sko.util;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TransactionsMap implements Serializable{

	public static TransactionsMap latestTransactions = new TransactionsMap();
	public static TransactionsMap suspiciousTransactions= new TransactionsMap();
		
	
	private List<MappedTransaction> transactions = new ArrayList<MappedTransaction>();

	public TransactionsMap(){}
	
	public TransactionsMap(Collection<MappedTransaction> t){
		transactions.addAll(t);
	}
	
	public Collection<MappedTransaction> getTransactions() {
		return transactions;
	}

	
	public void addTransaction(long id, double value, String location, boolean suspect, long timestamp){
		transactions.add(new MappedTransaction(id, value, location, suspect, timestamp));
	}
	
	public void clearAll(){
		transactions.clear();
	}


	public void addTransaction(MappedTransaction t) {
		transactions.add(t);
		
	}

	public void addTransaction(long id, double value,
			String location, long timestamp) {
		addTransaction(id, value, location, false, timestamp);		
	}
	
	
	
}
