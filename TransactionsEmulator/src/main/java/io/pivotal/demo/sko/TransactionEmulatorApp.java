package io.pivotal.demo.sko;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class TransactionEmulatorApp {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(TransactionEmulatorApp.class, args);
    }
}
