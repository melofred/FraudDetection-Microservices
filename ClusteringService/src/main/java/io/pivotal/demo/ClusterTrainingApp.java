package io.pivotal.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
@EnableAutoConfiguration
@SpringBootApplication
public class ClusterTrainingApp {

	public static void main(String[] args) throws Exception{
		SpringApplication.run(ClusterTrainingApp.class, args);
	}
	
	
	
}


