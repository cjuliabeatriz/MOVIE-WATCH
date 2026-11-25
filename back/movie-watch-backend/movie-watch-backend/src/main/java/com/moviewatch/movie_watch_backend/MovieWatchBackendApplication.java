package com.moviewatch.movie_watch_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

// Classe principal da aplicação.
// @ConfigurationPropertiesScan habilita a leitura de MovieWatchProperties.
@SpringBootApplicationfc
@ConfigurationPropertiesScan
public class MovieWatchBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MovieWatchBackendApplication.class, args);
	}

}
