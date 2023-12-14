package com.stc.fugitive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan(value = "com.stc")//解决common的bean无法加载的问题
@EnableScheduling
@SpringBootApplication
public class FugitiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(FugitiveApplication.class, args);
    }

}
