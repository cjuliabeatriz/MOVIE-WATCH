package com.moviewatch.movie_watch_backend.controller;

import tools.jackson.databind.JsonNode;
import com.moviewatch.movie_watch_backend.client.CinemetaClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Controller público que expõe os dados da Cinemeta.
// O backend não mantém tabela local de filmes.
@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final CinemetaClient cinemetaClient;

    public MovieController(CinemetaClient cinemetaClient) {
        this.cinemetaClient = cinemetaClient;
    }

    // Detalhes de um filme pelo ID do Cinemeta.
    @GetMapping("/{imdbId}")
    public JsonNode getMovie(@PathVariable String imdbId) {
        return cinemetaClient.getMovie(imdbId);
    }

    // Busca filmes por texto.
    @GetMapping("/search")
    public JsonNode search(@RequestParam String q) {
        return cinemetaClient.search(q);
    }

    // Lista os filmes em destaque.
    @GetMapping("/top")
    public JsonNode top() {
        return cinemetaClient.top();
    }
}