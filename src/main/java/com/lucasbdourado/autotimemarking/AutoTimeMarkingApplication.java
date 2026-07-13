package com.lucasbdourado.autotimemarking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AutoTimeMarkingApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoTimeMarkingApplication.class, args);
    }
}
