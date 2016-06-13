package io.pivotal.demo.util;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@EnableAutoConfiguration 
@Configuration
public class RedisConfig extends AbstractCloudConfig{

	
	
	 @Bean
	 public RedisConnectionFactory redisFactory() {		
		return connectionFactory().redisConnectionFactory();
	 }	 
	 
	 
	 @Bean
	 RedisTemplate< String, String > redis() {
	  final RedisTemplate< String, String > template =  new RedisTemplate< String, String >();
	  template.setKeySerializer(new StringRedisSerializer() );
	  template.setValueSerializer(new StringRedisSerializer() );
	  template.setConnectionFactory( redisFactory() );
	  return template;
	 }	 
	 

}
