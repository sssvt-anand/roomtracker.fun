package com.room.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.room.app")
public class RoomexpenseappApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoomexpenseappApplication.class, args);
	}

}
