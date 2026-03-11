# Biblioteca Universitaria API

API em Java para gerenciamento de biblioteca universitaria, iniciando pela Feature 1 com foco em TDD.

## Feature 1 - Modulo de Reserva de Livros

### Funcionalidade escolhida
Implementacao do modulo de **Reserva de Livros**.

### Como se integra ao projeto
A feature adiciona a regra de negocio de reserva entre as entidades `Livro`, `Leitor` e `Reserva`, garantindo consistencia de estoque (`quantidadeDisponivel`) e evitando reservas ativas duplicadas para o mesmo livro/leitor.

### Principais classes implementadas
- `Livro` (`livro.model`): entidade com `quantidadeTotal` e `quantidadeDisponivel`.
- `Leitor` (`leitor.model`): entidade do leitor.
- `Reserva` (`reserva.model`): entidade de reserva com `dataReserva`, `status` e `prazoDias`.
- `ReservaStatus` (`reserva.model`): enum `ATIVA` e `CANCELADA`.
- `ReservaService` (`reserva.service`): aplica regras de criacao e cancelamento.
- `ReservaRepository` (`reserva.repository`): consulta de reserva ativa duplicada.
- Excecoes de dominio (`reserva.exception`):
  - `LivroIndisponivelException`
  - `ReservaDuplicadaException`
  - `ReservaNaoEncontradaException`

## TDD aplicado (Red-Green-Refactor)

### Red
Primeiro foram criados os testes unitarios do `ReservaService` para os cenarios de sucesso e erro. Nesse momento, o projeto falhou por ausencia das classes de dominio/servico (falha esperada no ciclo TDD).

### Green
Foi implementado o codigo minimo para fazer os testes passarem:
- entidades `Livro`, `Leitor`, `Reserva`;
- enum `ReservaStatus`;
- repositorios JPA;
- excecoes de negocio;
- `ReservaService` com regras de negocio.

### Refactor
O `ReservaService` foi reorganizado com metodos privados de validacao/busca para melhorar legibilidade sem alterar comportamento, mantendo os testes verdes.

## Cenarios de teste unitario (JUnit 5)

1. `@DisplayName("Deve criar reserva ativa com prazo padrao e reduzir quantidade disponivel do livro")`
- Objetivo: validar criacao com `status=ATIVA`, `prazoDias=7` e decremento de estoque.

2. `@DisplayName("Deve lancar erro ao reservar livro sem quantidade disponivel")`
- Objetivo: garantir erro quando nao ha exemplares disponiveis.

3. `@DisplayName("Deve lancar erro ao tentar criar reserva ativa duplicada para o mesmo livro e leitor")`
- Objetivo: impedir duplicidade de reserva ativa para o mesmo par livro/leitor.

4. `@DisplayName("Deve cancelar reserva ativa e devolver uma unidade para quantidade disponivel")`
- Objetivo: validar cancelamento da reserva e incremento do estoque.

5. `@DisplayName("Deve lancar erro ao cancelar uma reserva inexistente")`
- Objetivo: garantir tratamento de erro para reserva nao encontrada.

## Estrutura de pacotes da Feature 1

- `br.com.leonardodasilvasousa.biblioteca_api.livro.model`
- `br.com.leonardodasilvasousa.biblioteca_api.livro.repository`
- `br.com.leonardodasilvasousa.biblioteca_api.leitor.model`
- `br.com.leonardodasilvasousa.biblioteca_api.leitor.repository`
- `br.com.leonardodasilvasousa.biblioteca_api.reserva.model`
- `br.com.leonardodasilvasousa.biblioteca_api.reserva.repository`
- `br.com.leonardodasilvasousa.biblioteca_api.reserva.service`
- `br.com.leonardodasilvasousa.biblioteca_api.reserva.exception`
- `br.com.leonardodasilvasousa.biblioteca_api.reserva.service` (testes)

## Como executar

```bash
./mvnw test
./mvnw spring-boot:run
```

H2 Console:
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:biblioteca_db`
- User: `sa`
- Password: (vazio)

## Feature 2 - Integracao e orquestracao com APIs externas

### API externa escolhida e justificativa
- `Google Books API`: fonte principal de metadados de livros (titulo, autores, categorias, nota e volume de avaliacoes).
- `Open Library API`: fonte complementar para enriquecer dados quando necessario.

Essa escolha e aderente ao dominio de biblioteca e atende ao requisito da Feature 2 de consumir APIs externas relevantes.

### Arquitetura aplicada (camadas)
- `client` (OpenFeign): consumo de APIs externas.
- `service`: orquestracao dos dados externos + dados internos + algoritmo de score.
- `controller`: exposicao do endpoint REST consumivel por outros sistemas.
- `dto.external`: mapeamento das respostas brutas das APIs externas.
- `dto.response`: contrato consolidado retornado pelo endpoint.

### Endpoint criado
- `GET /api/v1/livros/enriquecidos?query={termo}&max={1..20}`

Exemplo:
```bash
curl \"http://localhost:8080/api/v1/livros/enriquecidos?query=clean%20architecture&max=5\"
```

### Algoritmo implementado (além de pass-through)
Cada livro recebe `scoreRecomendacao` (0-100), considerando:
- disponibilidade interna (peso alto),
- nota externa,
- volume de avaliacoes,
- recencia de publicacao,
- completude de metadados.

Com isso, a API nao apenas repassa dados externos: ela transforma e prioriza resultados para consumo inteligente.

### Principais classes da Feature 2
- `integracao.livros.client.GoogleBooksClient`
- `integracao.livros.client.OpenLibraryClient`
- `integracao.livros.service.LivrosIntegracaoService`
- `integracao.livros.controller.LivrosIntegracaoController`
- `integracao.livros.dto.external.google.*`
- `integracao.livros.dto.external.openlibrary.*`
- `integracao.livros.dto.response.*`

### Configuracao
No `application.yml`:
- `app.integracoes.google-books.base-url`
- `app.integracoes.google-books.api-key` (via `GOOGLE_BOOKS_API_KEY`, opcional)
- `app.integracoes.open-library.base-url`

### Testes da Feature 2
Testes unitarios do servico:
- consolidacao de dados externos + internos e calculo de score;
- fallback quando Open Library falha;
- ordenacao por score e limite de retorno;
- validacao de query invalida.

Arquivo:
- `src/test/java/br/com/leonardodasilvasousa/biblioteca_api/integracao/livros/service/LivrosIntegracaoServiceTest.java`

### Consumo pelo projeto da 1a disciplina (requisito 7)
No projeto da primeira disciplina, crie um client HTTP para consumir:
- `GET http://<host-da-biblioteca-api>:8080/api/v1/livros/enriquecidos?query={termo}&max={n}`

Exemplo de uso esperado no projeto consumidor:
- chamar o endpoint para obter lista consolidada,
- exibir `titulo`, `quantidadeDisponivelInterna` e `scoreRecomendacao`,
- usar o score para ordenar recomendacoes na interface.
