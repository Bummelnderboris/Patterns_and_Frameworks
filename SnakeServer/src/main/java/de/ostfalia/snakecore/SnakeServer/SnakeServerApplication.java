package de.ostfalia.snakecore.SnakeServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "de.ostfalia.snakecore.model")
public class SnakeServerApplication {

	// Diese Klasse musst du ausführen um den Server zu starten
	public static void main(String[] args) {
		SpringApplication.run(SnakeServerApplication.class, args);
	}

}
