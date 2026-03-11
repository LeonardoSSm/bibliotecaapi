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
