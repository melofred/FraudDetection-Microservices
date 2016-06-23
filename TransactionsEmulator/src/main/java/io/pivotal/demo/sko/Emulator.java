package io.pivotal.demo.sko;


import io.pivotal.demo.sko.entity.PoSDevice;
import io.pivotal.demo.sko.entity.Transaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PrimitiveIterator.OfLong;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

import org.mortbay.util.ajax.JSON;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@ComponentScan
@Configuration
public class Emulator implements CommandLineRunner {

	    @Value("${geodeUrl}")
		private String geodeURL;
		
	    @Value("${delayInMs}")
		private long delay;
	    
	    @Value("${skipSetup}")
	    private boolean skipSetup;

	    @Value("${numberOfAccounts}")
	    private int numberOfAccounts;
		
	    @Value("${numberOfTransactions}")
	    private int numberOfTransactions;
	    
	    private ArrayList<String> counties;

		private Map<Long,Long> accountToDeviceMap = new HashMap<Long,Long>();
	   
	    private RestTemplate restTemplate = new RestTemplate();
		
		Logger logger = Logger.getLogger(Emulator.class.getName());
		
		
	    private void getCloudEnvProperties(){
	    	String vcapServices = System.getenv("VCAP_SERVICES");
	    	if (vcapServices==null || vcapServices.isEmpty()) return;
	    	    	
			Object parsed = JSON.parse(vcapServices);
			logger.info("VCAP= "+parsed.toString());
			Object[] userProvided = (Object[])((Map)parsed).get("user-provided");
			Object gemService = userProvided[0];
			Map credentials=(Map)((Map)gemService).get("credentials");			
			geodeURL = credentials.get("RestAPI").toString();
			
	    }
		
		
		@Override
		public void run(String... args) throws Exception {
			
			getCloudEnvProperties();
			loadPoSCounties();
			
			if (!skipSetup){
				runSetup();
			}
			if (numberOfTransactions<0) numberOfTransactions = Integer.MAX_VALUE;
			
			logger.info(">>>>> RUNNING SIMULATION");		
			logger.info("--------------------------------------");
			logger.info(">>> Geode rest endpoint: "+geodeURL);
			logger.info("--------------------------------------");
			
			logger.info(">>> Posting "+numberOfTransactions+" transactions ...");

			int numberOfDevices = counties.size();
			
			OfLong deviceIDs = new Random().longs(0, numberOfDevices).iterator();
			OfLong accountIDs = new Random().longs(0, numberOfAccounts).iterator();
			
			Random random = new Random();
			long mean = 100; // mean value for transactions
			long variance = 40; // variance

			DecimalFormat df = new DecimalFormat();
			df.setMaximumFractionDigits(2);
			
			for (int i=0; i<numberOfTransactions; i++){
				//Map<String,Object> map = (Map)objects.get(i);
				Transaction t = new Transaction();
				t.setId(Math.abs(UUID.randomUUID().getLeastSignificantBits()));
				long accountId = accountIDs.next();
				t.setAccountId(accountId);		
				
				// 90% of times, we'll transact this account from a single "home location"
				if (Math.random()<0.9){
					t.setDeviceId(getHomePoS(accountId));
				}
				else {
					t.setDeviceId(deviceIDs.next());
				}
				
				
				
				t.setTimestamp(System.currentTimeMillis());
				
				double value = Double.parseDouble(df.format(Math.abs(mean+random.nextGaussian()*variance)));  
				t.setValue(value);
				
				try{					
					Transaction response = restTemplate.postForObject(geodeURL+RegionName.Transaction, t, Transaction.class);
				}
				catch(Exception e){
					logger.warning("Failed to connect to Geode using URL "+geodeURL);
					e.printStackTrace();
				}
				Thread.sleep(delay);
				

			}

			logger.info("done");
			
			
		}
		
		private Long getHomePoS(Long accountId){
			
			// Randomly pick a deviceId, in case there's not already one mapped to that account
			
			if (accountToDeviceMap.get(accountId)==null){
				Long deviceId = new Random().longs(0, counties.size()).iterator().next();
				accountToDeviceMap.put(accountId, deviceId);
			}			
			return accountToDeviceMap.get(accountId);
			
			
		}
		

		/*
		 * Load the counties data from file
		 */
		private void loadPoSCounties() throws IOException {
			counties = new ArrayList<String>();
			
			InputStream is = ClassLoader.getSystemResourceAsStream("counties.csv");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			
			String line = br.readLine();  //skip header
			while ( (line=br.readLine())!=null){
				String county = line.split("\"")[1];
				counties.add(county);
			}
		}

		private void runSetup() {

			int numberOfDevices = counties.size();
			
			logger.info(">>>>> RUNNING SETUP");		
			logger.info("--------------------------------------");
			logger.info(">>> Geode rest endpoint: "+geodeURL);
			logger.info("--------------------------------------");
			
			logger.info(">>> Adding "+numberOfDevices+" devices ...");

			// Add PoS'es
			for (int i=0; i<numberOfDevices; i++){
				PoSDevice device = new PoSDevice();
				device.setId(i+1);
				device.setLocation(counties.get(i));
				device.setMerchantName("Merchant "+i);
				PoSDevice response = restTemplate.postForObject(geodeURL+RegionName.PoS, device, PoSDevice.class);
			}
			
			
		}
}
