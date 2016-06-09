package io.pivotal.demo;

import io.pivotal.demo.util.Util;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.config.SpelExpressionConverterConfiguration;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.support.MutableMessage;
import org.springframework.messaging.Message;
import org.springframework.tuple.MutableTuple;
import org.springframework.tuple.Tuple;
import org.springframework.tuple.TupleBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

@EnableBinding(Processor.class)
@EnableConfigurationProperties(EnrichProcessorProperties.class)
@Import(SpelExpressionConverterConfiguration.class)
public class EnrichProcessor {

	@Autowired
	private StringRedisTemplate redis;
	
    @Autowired
    private EnrichProcessorProperties properties;

    @Autowired
    @Qualifier(IntegrationContextUtils.INTEGRATION_EVALUATION_CONTEXT_BEAN_NAME)
    private EvaluationContext evaluationContext;    
	
	@ServiceActivator(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
	public Object process(Message<?> message) throws Exception {
		

		SpelExpressionParser spel = new SpelExpressionParser();
		
		String account = spel.parseExpression("payload.accountId").getValue(evaluationContext, message).toString();
		String device = spel.parseExpression("payload.deviceId").getValue(evaluationContext, message).toString();
		
		String location = redis.opsForValue().get("device::"+device);		
		String home = redis.opsForValue().get("home::"+account);
		
		double 	homeLat=0 , 
				homeLong=0, 
				locLat=0, 
				locLong=0, 
				distance=0;
		
		if (home!=null && location!=null) {
		
					
			String[] homeSplit = home.split(":");
			String[] locSplit = location.split(":");
			
			DecimalFormat df = new DecimalFormat();
			df.setMaximumFractionDigits(2);
			
			homeLat = Double.parseDouble(homeSplit[0].trim());
			homeLong = Double.parseDouble(homeSplit[1].trim());			
			locLat = Double.parseDouble(locSplit[0].trim());
			locLong = Double.parseDouble(locSplit[1].trim());
			
			distance = Double.parseDouble(df.format(Util.calculateDistanceInKm(homeLat, homeLong, locLat, locLong)));
		}
		
		MutableMessage<?> result = convertToMutable(message);
		spel.parseExpression("payload.homeLocation").setValue(evaluationContext, result, home);
		spel.parseExpression("payload.homeLatitude").setValue(evaluationContext, result, homeLat);
		spel.parseExpression("payload.homeLongitude").setValue(evaluationContext, result, homeLong);
		spel.parseExpression("payload.distance").setValue(evaluationContext, result, distance);
	
		return result;
	}

    private MutableMessage<?> convertToMutable(Message<?> input) throws Exception{
        Object payload = input.getPayload();
        if (payload instanceof Tuple && !(payload instanceof MutableTuple)) {
                payload = TupleBuilder.mutableTuple().putAll((Tuple) payload).build();
        }
        else if (payload instanceof String){
    		String strPayload = input.getPayload().toString();
    		Iterator<Entry<String, Object>> objects = new ObjectMapper().readValue(strPayload, Map.class).entrySet().iterator();
    		TupleBuilder tuples = TupleBuilder.mutableTuple();
    		while (objects.hasNext()){
    			Entry<String,Object> entry = objects.next();
    			tuples.put(entry.getKey(), entry.getValue());
    		}        	
    		payload = tuples.build();
        }
        return new MutableMessage<>(payload, input.getHeaders());
    }
}	

