package br.com.leonardodasilvasousa.biblioteca_api.config;

import br.com.leonardodasilvasousa.biblioteca_api.integracao.livros.dto.response.LivrosEnriquecidosResponse;
import br.com.leonardodasilvasousa.biblioteca_api.integracao.livros.service.LivrosIntegracaoService;
import br.com.leonardodasilvasousa.biblioteca_api.livro.dto.request.LivroRequestDTO;
import br.com.leonardodasilvasousa.biblioteca_api.livro.dto.response.LivroResponseDTO;
import br.com.leonardodasilvasousa.biblioteca_api.livro.service.LivroApplicationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LivroApplicationService livroApplicationService;

    @MockitoBean
    private LivrosIntegracaoService livrosIntegracaoService;

    @Test
    @DisplayName("Deve retornar 401 quando a requisicao nao possui autenticacao")
    void deveRetornar401SemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/v1/livros"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve permitir leitura do catalogo para usuario com role USER")
    void devePermitirLeituraParaUser() throws Exception {
        when(livroApplicationService.listarLivros()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/livros")
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve bloquear criacao de livro para usuario com role USER")
    void deveBloquearCriacaoParaUser() throws Exception {
        String payload = """
                {
                  "titulo": "Clean Code",
                  "autor": "Robert C. Martin",
                  "isbn": "9780132350884",
                  "categoria": "Engenharia de Software",
                  "quantidadeTotal": 10,
                  "quantidadeDisponivel": 8
                }
                """;

        mockMvc.perform(post("/api/v1/livros")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve permitir criacao de livro para usuario com role ADMIN")
    void devePermitirCriacaoParaAdmin() throws Exception {
        String payload = """
                {
                  "titulo": "Domain-Driven Design",
                  "autor": "Eric Evans",
                  "isbn": "9780321125217",
                  "categoria": "Arquitetura",
                  "quantidadeTotal": 12,
                  "quantidadeDisponivel": 12
                }
                """;

        when(livroApplicationService.criarLivro(any(LivroRequestDTO.class)))
                .thenReturn(new LivroResponseDTO(
                        1L,
                        "Domain-Driven Design",
                        "Eric Evans",
                        "9780321125217",
                        "Arquitetura",
                        12,
                        12
                ));

        mockMvc.perform(post("/api/v1/livros")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Deve bloquear acesso de USER ao contexto enriquecido")
    void deveBloquearContextoEnriquecidoParaUser() throws Exception {
        mockMvc.perform(get("/api/v1/livros/enriquecidos")
                        .queryParam("query", "clean architecture")
                        .queryParam("max", "5")
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve permitir acesso de ADMIN ao contexto enriquecido")
    void devePermitirContextoEnriquecidoParaAdmin() throws Exception {
        when(livrosIntegracaoService.buscarLivrosEnriquecidos("clean architecture", 5))
                .thenReturn(new LivrosEnriquecidosResponse("clean architecture", 0, List.of()));

        mockMvc.perform(get("/api/v1/livros/enriquecidos")
                        .queryParam("query", "clean architecture")
                        .queryParam("max", "5")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve exigir autenticacao por padrao para endpoints nao mapeados")
    void deveExigirAutenticacaoPorPadrao() throws Exception {
        mockMvc.perform(get("/api/v1/endpoint-inexistente"))
                .andExpect(status().isUnauthorized());
    }
}
