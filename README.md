# Biblioteca Universitaria API

API de biblioteca em Java 21 com Spring Boot, evoluida nas Features 1, 2, 3 e 4.

## Stack
- Java 21
- Spring Boot
- Spring Data JPA
- Spring Security
- Validation
- H2 Database
- OpenFeign
- Maven

## Estrutura multi-modulos (Feature 3)

O projeto foi refatorado para a opcao 1 da Feature 3:

- `common-domain`
  - Entidades e enums de dominio compartilhados (`Livro`, `Leitor`, `Reserva`, `ReservaStatus`).
  - Sem anotacoes de validacao de request HTTP.
- `external-api-client`
  - Feign clients e DTOs de APIs externas (`GoogleBooksClient`, `OpenLibraryClient` e DTOs externos).
- `main-application`
  - Aplicacao Spring Boot principal.
  - Controllers, services, repositorios, DTOs de entrada/saida e tratamento de excecao.
  - Dependencias explicitas de `common-domain` e `external-api-client`.

Essa separacao protege o dominio, melhora reuso e isola integracoes externas.

## Feature 1 - Modulo de Reserva de Livros (TDD)

### Funcionalidade
Implementacao de reservas com regras:
- reserva apenas com quantidade disponivel;
- proibicao de reserva ativa duplicada para mesmo livro/leitor;
- decremento de estoque na criacao;
- incremento de estoque no cancelamento;
- status inicial `ATIVA`;
- prazo padrao de `7` dias.

### Principais classes
- `ReservaService`
- `ReservaRepository`
- `Reserva`, `ReservaStatus`
- `Livro`, `Leitor`
- `LivroIndisponivelException`, `ReservaDuplicadaException`, `ReservaNaoEncontradaException`

### TDD (Red-Green-Refactor)
- RED: testes escritos antes, falhando por ausencia das classes.
- GREEN: implementacao minima para passar.
- REFACTOR: extracao de metodos privados para validacao e busca.

### Cenarios de teste (DisplayName)
1. `Deve criar reserva ativa com prazo padrao e reduzir quantidade disponivel do livro`
2. `Deve lancar erro ao reservar livro sem quantidade disponivel`
3. `Deve lancar erro ao tentar criar reserva ativa duplicada para o mesmo livro e leitor`
4. `Deve cancelar reserva ativa e devolver uma unidade para quantidade disponivel`
5. `Deve lancar erro ao cancelar uma reserva inexistente`

## Feature 2 - Integracao e orquestracao com APIs externas

### APIs usadas
- Google Books API (fonte principal)
- Open Library API (complemento/fallback)

### Entrega tecnica
- Endpoint `GET /api/v1/livros/enriquecidos?query={termo}&max={1..20}`
- Orquestracao entre dados externos e dados internos (acervo/reservas)
- Calculo de `scoreRecomendacao`
- Testes unitarios do `LivrosIntegracaoService`

## Feature 3 - DTOs + design orientado ao cliente

### Aplicacao de DTOs
- Request DTOs no `POST` e `PUT` de livros:
  - `LivroRequestDTO` com validacoes (`@NotBlank`, `@Size`, `@Min`).
- Response DTOs para `GET`, `POST` e `PUT`:
  - `LivroResponseDTO`.
- DTOs enriquecidos para busca integrada:
  - `LivrosEnriquecidosResponse` e `LivroEnriquecidoResponse`.

### Mapeamento DTO <-> dominio
No `LivroApplicationService`:
- mapeia `LivroRequestDTO` -> `Livro` (entidade de dominio);
- aplica validacoes de regra de negocio;
- persiste via `LivroRepository`;
- mapeia `Livro` -> `LivroResponseDTO`.

### Orquestracao de dados
No `LivrosIntegracaoService`:
- consome Google Books e Open Library (modulo `external-api-client`);
- cruza com disponibilidade interna e reservas ativas;
- retorna resposta consolidada e otimizada para cliente da API.

## Feature 4 - Autenticacao e autorizacao com Spring Security

### Autenticacao (HTTP Basic)
- Todas as requisicoes exigem autenticacao.
- Mecanismo: HTTP Basic.
- Usuarios em memoria com senha codificada (BCrypt):
  - `admin` / `admin123` com role `ADMIN`
  - `user` / `user123` com role `USER`

### Autorizacao por URL/HttpMethod (2 contextos)

Contexto 1 - Catalogo interno (`/api/v1/livros/**`):
- `GET`: `ADMIN` ou `USER`
- `POST`, `PUT`, `PATCH`, `DELETE`: apenas `ADMIN`

Contexto 2 - Catalogo enriquecido (`/api/v1/livros/enriquecidos`):
- `GET`: apenas `ADMIN`

Seguranca por padrao:
- Qualquer rota fora das regras explicitas permanece com `authenticated()`.

### Evidencias de teste da Feature 4
Arquivo:
- `main-application/src/test/java/br/com/leonardodasilvasousa/biblioteca_api/config/SecurityConfigWebTest.java`

Cenarios validados:
1. `401 Unauthorized` sem credenciais
2. `200 OK` para `USER` em leitura de catalogo (`GET /api/v1/livros`)
3. `403 Forbidden` para `USER` em criacao (`POST /api/v1/livros`)
4. `201 Created` para `ADMIN` em criacao (`POST /api/v1/livros`)
5. `403 Forbidden` para `USER` no contexto enriquecido
6. `200 OK` para `ADMIN` no contexto enriquecido
7. `401 Unauthorized` em endpoint nao mapeado sem autenticacao

### Exemplo rapido com curl
Leitura com USER:
```bash
curl -u user:user123 http://localhost:8080/api/v1/livros
```

Criacao com ADMIN:
```bash
curl -u admin:admin123 -X POST http://localhost:8080/api/v1/livros \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Clean Architecture","autor":"Robert C. Martin","isbn":"9780134494166","categoria":"Arquitetura","quantidadeTotal":10,"quantidadeDisponivel":10}'
```

## Endpoints principais
- `GET /api/v1/livros`
- `GET /api/v1/livros/{id}`
- `POST /api/v1/livros`
- `PUT /api/v1/livros/{id}`
- `GET /api/v1/livros/enriquecidos?query={termo}&max={1..20}`

## Como executar

Rodar testes de todos os modulos:
```bash
./mvnw test
```

Rodar aplicacao principal:
```bash
./mvnw -pl main-application spring-boot:run
```

## H2 Console
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:biblioteca_db`
- User: `sa`
- Password: (vazio)
