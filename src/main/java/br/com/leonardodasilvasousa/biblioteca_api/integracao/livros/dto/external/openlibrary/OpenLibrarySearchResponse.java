package br.com.leonardodasilvasousa.biblioteca_api.integracao.livros.dto.external.openlibrary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenLibrarySearchResponse(List<OpenLibraryDoc> docs) {
}
