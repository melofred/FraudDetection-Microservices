package io.pivotal.demo.sko.util;

import io.pivotal.demo.sko.RegionName;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.mortbay.util.ajax.JSON;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.execute.Execution;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.cache.query.FunctionDomainException;
import com.gemstone.gemfire.cache.query.NameResolutionException;
import com.gemstone.gemfire.cache.query.Query;
import com.gemstone.gemfire.cache.query.QueryInvocationTargetException;
import com.gemstone.gemfire.cache.query.QueryService;
import com.gemstone.gemfire.cache.query.TypeMismatchException;
import com.gemstone.gemfire.pdx.PdxInstance;

public class GeodeClient {

    private  ClientCache cache;
    
    private static GeodeClient instance;

	private static String locatorHost = System.getProperty("locatorHost", "geode-server");
	private static int locatorPort = Integer.getInteger("locatorPort", 10334);
	private static String username = "";
	private static String password = "";
	
	private QueryService queryService;
	
    Region transactions;
    Region suspect;

	static Logger logger = Logger.getLogger(GeodeClient.class.getCanonicalName());

    private GeodeClient() {
 
    	if (System.getenv("VCAP_SERVICES")!=null){
			logger.info("Configuring locator information from service binding");
    		getCloudEnvProperties();    	
    	}
    
		logger.info(String.format("Geode Locator Information: %s[ %d ]",locatorHost, locatorPort));

    }
    
    /*
     * Parse the environment variables for services.
     */
    private void getCloudEnvProperties(){
    	String vcapServices = System.getenv("VCAP_SERVICES");
    	if (vcapServices==null || vcapServices.isEmpty()) return;
    	    	
		Object parsed = JSON.parse(vcapServices);
		logger.info("VCAP= "+parsed.toString());
		Object[] userProvided = (Object[])((Map)parsed).get("user-provided");
		Object gemService = userProvided[0];
		Map credentials=(Map)((Map)gemService).get("credentials");
		locatorHost = credentials.get("locatorHost").toString();
		locatorPort = Integer.parseInt(credentials.get("locatorPort").toString());
    }
    
    
    public static synchronized GeodeClient getInstance(){
    	if (instance==null) instance = new GeodeClient();
    	return instance;
    }


    public void setup(){
    	
		Properties props = new Properties();
		if (!username.isEmpty()){
			/*
			props.put("security-client-auth-init","templates.security.UserPasswordAuthInit.create");*/
			props.put("security-username", username);
			props.put("security-password", password);
		}
		    	
        cache = new ClientCacheFactory(props)
								.addPoolLocator(locatorHost, locatorPort)
								.setPoolSubscriptionEnabled(true)
								.set("name", "GeodeClient")
								.set("cache-xml-file", "client.xml")
								.set("mcast-port", "0")
								.create();

        queryService = cache.getQueryService();
        
        transactions = cache.getRegion(RegionName.Transaction.name());
        suspect = cache.getRegion(RegionName.Suspect.name());

        transactions.registerInterest("ALL_KEYS");
        suspect.registerInterest("ALL_KEYS");
        
        
    }
    
    public String getPoSLocation(Long deviceId){
    	
    	Query query = queryService.newQuery("select d.location from /PoS d where d.id=$1");
    	
   		try {
   			
   			Collection result = (Collection)query.execute(new Object[]{deviceId});
   			if (result.size()==0) return "";
   			String location = (String)result.iterator().next();
   			
			return location;
			
		} catch (FunctionDomainException | TypeMismatchException
				| NameResolutionException | QueryInvocationTargetException e) {
			throw new RuntimeException(e);
		}


    	
    }

    public PdxInstance getTransaction(long id){
    	
    	Query query = queryService.newQuery("select * from /Transaction t where t.id=$1");
    	
   		try {
   			
   			Collection result = (Collection)query.execute(new Object[]{id});
   			if (result.size()==0) throw new IllegalArgumentException("Couldn't find the transaction #"+id);
   			return (PdxInstance)result.iterator().next();
			
		} catch (FunctionDomainException | TypeMismatchException
				| NameResolutionException | QueryInvocationTargetException e) {
			throw new RuntimeException(e);
		}
    	
    }


}
