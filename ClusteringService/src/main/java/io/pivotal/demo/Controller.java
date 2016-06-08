package io.pivotal.demo;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Component
@RestController
@RequestMapping(value = "/clustering")
public class Controller {
	
	final private double RECOMMENDATION_THRESHOLD = 0.5;
	static private String pmmlModel; 
	
    public Controller() {
    }
    
    
          
    /**
     * Gets the clustering model, as per last training, in PMML format.
     * @return trained model in PMML format.
     */
    @RequestMapping(value="/model.pmml.xml", method=RequestMethod.GET)    
	public String getPMMLModel(){    	
    	return pmmlModel;
    } 
    

    /**
     * Trains the clustering algorithm
     */
    @RequestMapping(value="/train", method=RequestMethod.GET)    
	public String trainAndSave(){    	
    	pmmlModel = ClusteringService.train();
    	return pmmlModel;
    	
    }    
	
    
	 
}
