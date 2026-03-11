package br.com.leonardodasilvasousa.biblioteca_api.integracao.livros.dto.external.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoogleBooksVolumesResponse(List<GoogleBookItem> items) {
}
