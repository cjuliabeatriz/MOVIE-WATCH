package com.moviewatch.movie_watch_backend.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

// Testes do cliente HTTP da Cinemeta.
class CinemetaClientTest {

    private MockRestServiceServer metaServer;
    private MockRestServiceServer catalogServer;
    private CinemetaClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder metaBuilder = RestClient.builder()
                .baseUrl("https://v3-cinemeta.strem.io");
        metaServer = MockRestServiceServer.bindTo(metaBuilder).build();

        RestClient.Builder catalogBuilder = RestClient.builder()
                .baseUrl("https://cinemeta-catalogs.strem.io");
        catalogServer = MockRestServiceServer.bindTo(catalogBuilder).build();

        client = new CinemetaClient(metaBuilder.build(), catalogBuilder.build());
    }

    @Test
    void getMovieDeveConsultarEndpointMeta() {
        metaServer.expect(requestTo("https://v3-cinemeta.strem.io/meta/movie/tt0111161.json"))
                .andRespond(withSuccess(
                        "{\"meta\":{\"imdb_id\":\"tt0111161\",\"name\":\"Filme\"}}",
                        MediaType.APPLICATION_JSON
                ));

        JsonNode resposta = client.getMovie("tt0111161");

        assertTrue(resposta.has("meta"));
        metaServer.verify();
    }

    @Test
    void getMovieDeveLancarNotFoundEm404() {
        metaServer.expect(requestTo("https://v3-cinemeta.strem.io/meta/movie/tt0000000.json"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(
                com.moviewatch.movie_watch_backend.exception.NotFoundException.class,
                () -> client.getMovie("tt0000000")
        );
    }

    @Test
    void searchDeveConsultarCatalogoComQueryCodificada() {
        catalogServer.expect(requestTo(
                        "https://cinemeta-catalogs.strem.io/catalog/movie/top/search=Matrix.json"
                ))
                .andRespond(withSuccess("{\"metas\":[]}", MediaType.APPLICATION_JSON));

        JsonNode resposta = client.search("Matrix");

        assertTrue(resposta.has("metas"));
        catalogServer.verify();
    }

    @Test
    void topDeveConsultarCatalogoDeDestaques() {
        catalogServer.expect(requestTo(
                        "https://cinemeta-catalogs.strem.io/top/catalog/movie/top.json"
                ))
                .andRespond(withSuccess("{\"metas\":[]}", MediaType.APPLICATION_JSON));

        JsonNode resposta = client.top();

        assertTrue(resposta.has("metas"));
        catalogServer.verify();
    }
}