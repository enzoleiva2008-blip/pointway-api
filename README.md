# Pointway API — Sprint 4 (Domain Driven Design Using Java)

API RESTful em **Spring Boot** que converte pontos digitais SoulUp em créditos de
transporte público (Desafio 2: Utilização de Pontos para Transporte Público).
É a evolução do projeto Java da Sprint 3: as mesmas entidades, o mesmo banco e as
mesmas regras de negócio, agora expostas como endpoints HTTP para o front-end React.

**Equipe Pointway** — Kauã Fiuza (RM572369), Lucas Almeida (RM572499),
Enzo Leiva (RM568877), Matheus Segura (RM570878) — turma 1TDSPO.

- Deploy: `https://SEU-LINK-DO-DEPLOY` *(preencher após publicar)*
- Front-end: `https://SEU-LINK-DA-VERCEL` *(preencher)*

## Tecnologias
- Java 17 · Spring Boot 3.5 (spring-boot-starter-web)
- JDBC puro com padrão **DAO + Connection Factory** (sem JPA, como em aula)
- MySQL 8 (H2 opcional para testes locais)
- Maven · Docker (para o deploy)

## Estrutura (camadas)
```
src/main/java/com/pointway/
├── PointwayApplication.java      # classe main do Spring Boot
├── model/                        # entidades + regras de negócio (vindas da Sprint 3)
├── dao/                          # ConnectionFactory + CRUD JDBC de cada tabela
├── bo/                           # validações e regras de negócio (Business Object)
├── resource/                     # endpoints REST (@RestController)
├── dto/                          # JSON de entrada e saída
├── exception/                    # exceções + GlobalExceptionHandler (status HTTP)
└── config/                       # CorsFilter + DatabaseInitializer
src/main/resources/
├── application.properties
└── schema.sql                    # tabelas criadas automaticamente ao subir a API
```

## Endpoints
Todas as respostas são JSON. Erros sempre voltam no formato
`{ dataHora, status, erro, mensagem, caminho }`.

| Recurso | Verbo | URI | Sucesso | Erros possíveis |
|---|---|---|---|---|
| Status | GET | `/` | 200 | — |
| Impacto geral | GET | `/impacto` | 200 | 500 |
| Modais e fator de CO2 | GET | `/modais` | 200 | — |
| Usuários | GET | `/usuarios` | 200 | 500 |
| | GET | `/usuarios/{id}` | 200 | 404 |
| | GET | `/usuarios/cpf/{cpf}` | 200 | 400, 404 |
| | GET | `/usuarios/{id}/resumo` | 200 | 404 |
| | POST | `/usuarios` | 201 | 400, 409 |
| | PUT | `/usuarios/{id}` | 200 | 400, 404, 409 |
| | PATCH | `/usuarios/{id}/pontos` | 200 | 400, 404, 422 |
| | DELETE | `/usuarios/{id}` | 204 | 404, 409 |
| Operadoras | GET | `/operadoras?cidade=` | 200 | 500 |
| | GET | `/operadoras/{id}` | 200 | 404 |
| | POST | `/operadoras` | 201 | 400 |
| | PUT | `/operadoras/{id}` | 200 | 400, 404 |
| | DELETE | `/operadoras/{id}` | 204 | 404, 409 |
| Vouchers | GET | `/vouchers?usuarioId=&status=` | 200 | 400, 404 |
| | GET | `/vouchers/{id}` | 200 | 404 |
| | GET | `/vouchers/codigo/{codigo}` | 200 | 404 |
| | POST | `/vouchers` (converte pontos) | 201 | 400, 404, 422 |
| | PUT | `/vouchers/{id}` | 200 | 400, 404, 422 |
| | DELETE | `/vouchers/{id}` (cancela e devolve pontos) | 204 | 404, 422 |
| Viagens | GET | `/viagens?usuarioId=` | 200 | 404 |
| | GET | `/viagens/{id}` | 200 | 404 |
| | POST | `/viagens` (usa o voucher na catraca) | 201 | 400, 404, 422 |
| | PUT | `/viagens/{id}` | 200 | 400, 404 |
| | DELETE | `/viagens/{id}` | 204 | 404 |

Significado dos códigos: **200** OK · **201** criado (com header `Location`) ·
**204** removido · **400** dado inválido · **404** não encontrado ·
**409** conflito (CPF repetido, registro com vínculos) · **422** regra de negócio
impediu (saldo insuficiente, limite de resgates, voucher já usado) · **500** erro no servidor/banco.

### Exemplos de corpo (JSON)
```json
POST /usuarios      { "nome": "Enzo Leiva", "cpf": "529.982.247-25", "email": "enzo@email.com", "saldoPontos": 2000 }
PATCH /usuarios/1/pontos   { "pontos": 500 }
POST /operadoras    { "nome": "SPTrans", "cidade": "Sao Paulo", "sistemaBilhetagem": "Bilhete Unico" }
POST /vouchers      { "usuarioId": 1, "operadoraId": 1, "pontos": 800 }
PUT /vouchers/1     { "dataValidade": "2026-12-20", "status": "ATIVO" }
POST /viagens       { "codigoVoucher": "<codigo do voucher>", "modal": "ONIBUS", "distanciaKm": 12.5 }
PUT /viagens/1      { "modal": "METRO", "distanciaKm": 10 }
```
O arquivo `requests.http` tem todas essas chamadas prontas para testar.

## Regras de negócio
1. 100 pontos = R$ 1,00; conversão mínima de 500 pontos; exige saldo suficiente.
2. Antifraude: no máximo 3 vouchers emitidos por usuário em 24 horas.
3. Voucher com código único (UUID) e validade de 30 dias (pode ser prorrogado até 90).
4. Voucher é de uso único; vencido ou já usado é recusado na catraca.
5. CO2 economizado = distância × fator do modal (ônibus 0,09 · metrô 0,04 · trem 0,05 · BRT 0,07 kg/km).
6. Cancelar um voucher não usado devolve os pontos ao usuário.
7. CPF validado pelos dígitos verificadores e único; e-mail validado.
8. Usuário/operadora com vouchers vinculados não pode ser excluído (preserva o histórico).
9. Vouchers vencidos passam automaticamente para `EXPIRADO`.

## Como rodar localmente
**Ferramentas:** JDK 17+, Maven 3.9+ (ou a IDE: IntelliJ IDEA, Eclipse, VS Code com
Extension Pack for Java), MySQL 8.

1. Inicie o MySQL e crie o banco: `CREATE DATABASE pointway_db;`
   (ou rode `database/pointway_schema.sql`).
2. Usuário/senha padrão no código: `root` / `root` (em `dao/ConnectionFactory.java`).
   Se forem outros, altere lá ou use as variáveis `DB_URL`, `DB_USER`, `DB_PASSWORD`.
3. Rode:
   ```
   mvn spring-boot:run
   ```
   Ou, na IDE, execute a classe `PointwayApplication`.
4. Acesse `http://localhost:8080/` — deve aparecer `"status": "online"`.

As tabelas são criadas sozinhas na primeira execução, junto com 4 operadoras de exemplo.

**Sem MySQL?** Dá para testar com banco em memória:
`DB_URL="jdbc:h2:mem:pointway;MODE=MySQL;DB_CLOSE_DELAY=-1" DB_USER=sa DB_PASSWORD=sa mvn spring-boot:run`
(os dados somem ao parar a API).

### Importar na IDE
- **IntelliJ:** File > Open > pasta `pointway-api` (a que tem o `pom.xml`).
- **Eclipse:** File > Import > Maven > Existing Maven Projects.
- **VS Code:** abra a pasta com o Extension Pack for Java instalado.

## Deploy (Railway — API + MySQL no mesmo lugar)
1. Suba este projeto para o GitHub.
2. Em https://railway.com crie um projeto **Deploy from GitHub repo** e escolha o repositório
   (o Railway detecta o `Dockerfile`).
3. No mesmo projeto: **+ New > Database > MySQL**.
4. No serviço da API, aba **Variables**, adicione:
   ```
   DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=false&allowPublicKeyRetrieval=true
   DB_USER=${{MySQL.MYSQLUSER}}
   DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
   ```
5. Aba **Settings > Networking > Generate Domain**. Esse link é a URL da API para o front.
6. (Opcional) `CORS_ORIGIN=https://seu-front.vercel.app` para liberar só o seu front.

**Alternativa Render:** New > Web Service > repositório > Runtime **Docker**, e as mesmas
variáveis `DB_URL`, `DB_USER`, `DB_PASSWORD` apontando para um MySQL na nuvem
(ex.: o MySQL do Railway pela URL pública, ou Aiven).

## Consumindo no front-end (React + TypeScript)
```ts
const API = import.meta.env.VITE_API_URL;
const resp = await fetch(`${API}/vouchers`, {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({ usuarioId: 1, operadoraId: 1, pontos: 800 }),
});
if (!resp.ok) alert((await resp.json()).mensagem);
```
