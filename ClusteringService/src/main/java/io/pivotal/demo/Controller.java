package io.pivotal.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Component
@RestController
@RequestMapping(value = "/clustering")
public class Controller {
	
	@Autowired
	private RedisTemplate<String,String> redis;
	
	private static final String key = "PMML";
	
    public Controller() {
    }
    
    
          
    /**
     * Gets the clustering model, as per last training, in PMML format.
     * @return trained model in PMML format.
     */
    @RequestMapping(value="/model.pmml.xml", method=RequestMethod.GET)    
	public String getPMMLModel(){    	
    	String model = redis.boundValueOps(key).get();
    	return model==null?"":model;
    } 
    

    /**
     * Trains the clustering algorithm
     */
    @RequestMapping(value="/train", method=RequestMethod.GET)    
	public String trainAndSave(){    	
    	String model = ClusteringService.train();
    	redis.opsForValue().set(key, model);
    	return model;
    	
    }    
	
    
	 
}
