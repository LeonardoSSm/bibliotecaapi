package br.com.leonardodasilvasousa.biblioteca_api.integracao.livros.client;

import br.com.leonardodasilvasousa.biblioteca_api.integracao.livros.dto.external.openlibrary.OpenLibrarySearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "openLibraryClient", url = "${app.integracoes.open-library.base-url}")
public interface OpenLibraryClient {

    @GetMapping("/search.json")
    OpenLibrarySearchResponse buscarLivros(
            @RequestParam("q") String query,
            @RequestParam("limit") Integer limit
    );
}
