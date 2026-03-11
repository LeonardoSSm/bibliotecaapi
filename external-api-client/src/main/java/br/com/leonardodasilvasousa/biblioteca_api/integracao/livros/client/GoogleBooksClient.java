package br.com.leonardodasilvasousa.biblioteca_api.integracao.livros.client;

import br.com.leonardodasilvasousa.biblioteca_api.integracao.livros.dto.external.google.GoogleBooksVolumesResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "googleBooksClient", url = "${app.integracoes.google-books.base-url}")
public interface GoogleBooksClient {

    @GetMapping("/volumes")
    GoogleBooksVolumesResponse buscarVolumes(
            @RequestParam("q") String query,
            @RequestParam("maxResults") Integer maxResults,
            @RequestParam(value = "key", required = false) String apiKey
    );
}
