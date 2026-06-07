# API Credit Management Partners

- [Kotlin](https://kotlinlang.org)
- [Spring Boot 3.x](https://spring.io)
- [MongoDB](https://mongodb.com)
- [RabbitMQ](https://rabbitmq.com)
- [Redis](https://redis.io)
- [Docker](https://docker.com)

Microsserviço de alta performance projetado para o ecossistema B2B, encarregado pela gestão de créditos de parceiros comerciais, controle transacional de saldos, processamento de aportes e histórico de movimentações financeiras.

---

## 1. Como Inicializar o Projeto

### Pré-requisitos e Instalação do Docker

*   **Windows:**
1. Baixe o instalador oficial do [Docker Desktop para Windows](https://docker.com).
2. Reinicie o computador e abra o Docker Desktop para iniciar o serviço.

- **Linux (Ubuntu/Debian):**
  Abra o terminal e execute os comandos abaixo para instalar o motor do Docker e o Docker Compose:
  ```bash
  sudo apt update && sudo apt install docker.io docker-compose -y
  sudo systemctl enable --now docker
  ```

### Subir a Infraestrutura Inteira

Abra o terminal na pasta raiz do projeto (onde está o arquivo `docker-compose.yml`) e execute o comando abaixo para realizar o build da aplicação e subir todos os serviços com segurança via *healthchecks*:

```bash
docker-compose up -d
```

- **Verificar Logs da API:** `docker compose logs -f app`
- **Verificar Logs do Banco:** `docker compose logs -f mongodb`
- **Derrubar os Serviços e Volumes:** `docker compose down -v`
- **Executar testes:** `./gradlew test`

---

## 2. Mapa de Endpoints e cURLs

### 1. CreditController (`/api/v1/credits`)

Gerencia o fluxo de consulta de saldos, adição de créditos (aportes), débitos por consumo e histórico financeiro de transações.

- **Endpoint 1: Consultar Saldo Atual do Parceiro:**
  ```bash
  curl --request GET \
  --url http://localhost:8001/api/v1/credits/parceiro-123/balance \
  --header 'User-Agent: insomnia/12.6.0' \
  --header 'accept: */*'
  ```

- **Endpoint 2: Adicionar Créditos:**
  ```bash
   curl --request POST \
    --url http://localhost:8001/api/v1/credits/add \
    --header 'Content-Type: application/json' \
    --header 'User-Agent: insomnia/12.6.0' \
    --header 'accept: */*' \
    --header 'x-idempotency-key: 1' \
    --data '{
     "partner_id": "parceiro-123",
     "amount": 150,
     "description": "Venda de Licença API - Pedido #987"
  }'
  ```

- **Endpoint 3: Debitar Créditos:**
  ```bash
  curl --request POST \
  --url http://localhost:8001/api/v1/credits/debit \
  --header 'Content-Type: application/json' \
  --header 'User-Agent: insomnia/12.6.0' \
  --header 'accept: */*' \
  --header 'x-idempotency-key: 1' \
  --data '{
   "partner_id": "parceiro-123",
   "amount": 1509,
   "description": "Venda de Licença API - Pedido #987"
    }'
  ```

- **Endpoint 4: Buscar Histórico Completo de Transações:**
  ```bash
  curl --request GET \
  --url http://localhost:8001/api/v1/credits/parceiro-123/transactions \
  --header 'User-Agent: insomnia/12.6.0' \
  --header 'accept: application/json'
  ```

### 2. PartnerAccountController (`/api/v1/partners`)

Endpoints encarregados pelo ciclo de vida da conta do parceiro no microsserviço de crédito.

- **Endpoint 1: Criar Conta de Crédito para Novo Parceiro:**
  ```bash
  curl --request POST \
  --url http://localhost:8001/api/v1/partner-accounts \
  --header 'Content-Type: application/json' \
  --header 'User-Agent: insomnia/12.6.0' \
  --header 'accept: */*' \
  --data '{
   "partner_id": "parceiro-123"
    }'
  ```

- **Endpoint 2: Excluir Conta de Crédito de um Parceiro:**
  ```bash
  curl --request DELETE \
  --url http://localhost:8001/api/v1/partner-accounts/parceiro-123 \
  --header 'User-Agent: insomnia/12.6.0' \
  --header 'accept: */*'
  ```

---

## 3. Motivações Técnicas e Arquitetura

O sistema foi desenhado seguindo os princípios de arquitetura limpa e alta resiliência para garantir que os saldos financeiros nunca fiquem inconsistentes.

- **Lock Otimista (`@Version`):** Para o cenário distribuído e escalável com MongoDB, utilizamos o controle de concorrência otimista. Cada alteração no saldo incrementa a versão do documento. Se duas requisições tentarem atualizar o mesmo saldo simultaneamente, a última falhará de forma segura, evitando cenários de inconsistência monetária.
- **Armazenamento de Alta Precisão (`BigDecimal`):** Dinheiro não deve ser tratado com tipos flutuantes comuns (`Float`/`Double`). No MongoDB, os valores são guardados estritamente como `BigDecimal` para evitar erros de arredondamento matemático comuns em operações de crédito e débito.
- **Orquestração Segura de Inicialização (`Healthchecks`):** O ambiente Docker foi projetado de forma que a aplicação Spring Boot aguarde a total prontidão do MongoDB (via comando ping interno) e do RabbitMQ antes de liberar a porta HTTP (`8001`), eliminando erros de conexão recusada durante a subida de containers.
- **Arquitetura Baseada em Portas e Adaptadores (Hexagonal):** O desacoplamento é garantido dividindo o sistema em casos de uso de aplicação (`UseCases`), interfaces de entrada (`Entrypoints Rest`) e adaptadores de infraestrutura (`Repositories`). Isso permite trocar o mecanismo de banco de dados ou mensageria sem tocar nas regras cruciais de negócio do domínio.
