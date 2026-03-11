package br.com.leonardodasilvasousa.biblioteca_api.integracao.livros.dto.external.openlibrary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenLibraryDoc(
        String title,
        @JsonProperty("author_name") List<String> authorName,
        List<String> isbn,
        List<String> subject,
        @JsonProperty("first_publish_year") Integer firstPublishYear
) {
}
