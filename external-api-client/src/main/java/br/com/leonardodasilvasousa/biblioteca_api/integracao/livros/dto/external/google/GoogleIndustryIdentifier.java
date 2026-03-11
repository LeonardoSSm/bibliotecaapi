package br.com.leonardodasilvasousa.biblioteca_api.integracao.livros.dto.external.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoogleIndustryIdentifier(String type, String identifier) {
}
