package com.xgs.healthupdate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

//SpringBoot启动类
@SpringBootApplication
@EnableScheduling
public class HealthUpdateApplication {

  public static void main(String[] args) {
    SpringApplication.run(HealthUpdateApplication.class, args);

  }


}
