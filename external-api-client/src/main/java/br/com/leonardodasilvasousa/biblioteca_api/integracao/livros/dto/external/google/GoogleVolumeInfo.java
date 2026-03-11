package br.com.leonardodasilvasousa.biblioteca_api.integracao.livros.dto.external.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoogleVolumeInfo(
        String title,
        List<String> authors,
        List<String> categories,
        Double averageRating,
        Integer ratingsCount,
        String publishedDate,
        List<GoogleIndustryIdentifier> industryIdentifiers
) {
}
