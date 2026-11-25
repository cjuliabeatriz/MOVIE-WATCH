package com.moviewatch.movie_watch_backend.client;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import com.moviewatch.movie_watch_backend.config.MovieWatchProperties;
import com.moviewatch.movie_watch_backend.exception.BadRequestException;
import com.moviewatch.movie_watch_backend.exception.ExternalServiceException;
import com.moviewatch.movie_watch_backend.exception.NotFoundException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

// Cliente HTTP para a API Cinemeta.
//
// A Cinemeta continua sendo a fonte de dados dos filmes.
// Nenhuma tabela local de filmes é criada.
@Component
public class CinemetaClient {

    private final RestClient metaClient;
    private final RestClient catalogClient;

    // Construtor usado pela aplicação.
    @Autowired
    public CinemetaClient(MovieWatchProperties properties) {
        this(
                buildClient(properties.getCinemeta().getMetaBaseUrl(), properties),
                buildClient(properties.getCinemeta().getCatalogBaseUrl(), properties)
        );
    }

    // Construtor usado nos testes para injetar clientes com MockRestServiceServer.
    CinemetaClient(RestClient metaClient, RestClient catalogClient) {
        this.metaClient = metaClient;
        this.catalogClient = catalogClient;
    }

    // Busca os metadados completos de um filme pelo ID do Cinemeta.
    public JsonNode getMovie(String imdbId) {
        validarId(imdbId);

        try {
            JsonNode resposta = metaClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/meta/movie/{id}.json").build(imdbId))
                    .retrieve()
                    .body(JsonNode.class);

            if (resposta == null) {
                throw new ExternalServiceException("Resposta vazia do Cinemeta.");
            }

            return resposta;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new NotFoundException("Filme nao encontrado.");
            }

            throw new ExternalServiceException("Nao foi possivel consultar o Cinemeta.");
        } catch (ExternalServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ExternalServiceException("Nao foi possivel consultar o Cinemeta.");
        }
    }

    // Pesquisa filmes no catálogo do Cinemeta por texto.
    public JsonNode search(String query) {
        if (query == null || query.isBlank()) {
            throw new BadRequestException("Informe um termo para a busca.");
        }

        try {
            JsonNode resposta = catalogClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/catalog/movie/top/search={query}.json").build(query))
                    .retrieve()
                    .body(JsonNode.class);

            if (resposta == null) {
                throw new ExternalServiceException("Resposta vazia do Cinemeta.");
            }

            return resposta;
        } catch (RestClientResponseException ex) {
            throw new ExternalServiceException("Nao foi possivel consultar o Cinemeta.");
        } catch (ExternalServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ExternalServiceException("Nao foi possivel consultar o Cinemeta.");
        }
    }

    // Lista os filmes em destaque do catálogo do Cinemeta.
    public JsonNode top() {
        try {
            JsonNode resposta = catalogClient.get()
                    .uri("/top/catalog/movie/top.json")
                    .retrieve()
                    .body(JsonNode.class);

            if (resposta == null) {
                throw new ExternalServiceException("Resposta vazia do Cinemeta.");
            }

            return resposta;
        } catch (RestClientResponseException ex) {
            throw new ExternalServiceException("Nao foi possivel consultar o Cinemeta.");
        } catch (ExternalServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ExternalServiceException("Nao foi possivel consultar o Cinemeta.");
        }
    }

    // Monta um RestClient com os timeouts configurados.
    private static RestClient buildClient(String baseUrl, MovieWatchProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getCinemeta().getConnectTimeoutMs());
        factory.setReadTimeout(properties.getCinemeta().getReadTimeoutMs());

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    // Valida o identificador externo do filme.
    private void validarId(String imdbId) {
        if (imdbId == null || imdbId.isBlank()) {
            throw new BadRequestException("O identificador do filme e obrigatorio.");
        }
    }
}