# Bank Account Service

## Project Report

**Author:** Hicham Ouaouche  
**Program:** Second Year SDIA (Distributed Systems and DevOps)  
**Institution:** ENSET Mohammedia  
**Project type:** Practical project - Account microservice

## 1. Overview

Bank Account Service is a Spring Boot application that manages bank accounts and customers through both REST and GraphQL APIs. It demonstrates the implementation of a microservice using Spring Data JPA, an in-memory H2 database, DTOs, a service layer, entity mapping and repository-based persistence.

The application is configured as a standalone service and runs on port `8081`. At startup, it automatically creates four sample customers and forty sample bank accounts (ten per customer) so that the APIs can be tested immediately.

## 2. Objectives

This project was developed to practice the following concepts:

- Building a Java backend with Spring Boot.
- Designing a RESTful CRUD API.
- Implementing a GraphQL API with Spring GraphQL.
- Persisting data with Spring Data JPA.
- Using H2 as a lightweight development database.
- Separating entities, DTOs, services, mappers and controllers.
- Handling enumerated domain values with Java enums.
- Managing entity relationships (one-to-many between Customer and BankAccount).
- Writing a basic Spring Boot integration test.

## 3. Technologies Used

| Technology | Role |
|---|---|
| Java 21 | Programming language and runtime target |
| Spring Boot 4.1.1 | Application framework |
| Spring Web MVC | REST controllers and HTTP handling |
| Spring GraphQL | GraphQL API implementation with GraphiQL UI |
| Spring Data JPA | Repository and persistence abstraction |
| Hibernate | JPA implementation used by Spring Boot |
| H2 Database | In-memory database for development and testing |
| Spring Data REST | Repository REST support |
| Springdoc OpenAPI | API documentation support |
| Lombok | Reduction of boilerplate code |
| Maven Wrapper | Reproducible Maven commands |

## 4. Application Architecture

The application follows a layered structure with dual API support (REST and GraphQL):

```text
HTTP client
    |
    +--> AccountRestController (REST API)
    |    |
    |    +--> AccountService / AccountServiceImpl
    |    |        |
    |    |        +--> AccountMapper
    |    |        |
    |    |        +--> BankAccountRepository / CustomerRepository
    |    |
    +--> BankAccountGraphQLController (GraphQL API)
    |    |
    |    +--> AccountService / AccountServiceImpl
    |         |
    |         +--> BankAccountRepository / CustomerRepository
    |
    v
BankAccount entity <--> Customer entity <--> H2 database
```

### Main packages

```text
org.sid.bak_account_service
├── dto             Request and response objects
├── entities        JPA entities (BankAccount, Customer)
├── enums           AccountType enumeration
├── exceptions      Custom exception handling
├── mappers         Entity-to-DTO conversion
├── repositories    Spring Data JPA repositories
├── service         Business service interface and implementation
└── web             REST and GraphQL controllers
```

## 5. Domain Model

The application manages two main entities: `Customer` and `BankAccount` with a one-to-many relationship.

### Customer Entity

| Field | Type | Description |
|---|---|---|
| `id` | `Long` | Auto-generated unique customer identifier |
| `name` | `String` | Customer name |
| `bankAccounts` | `List<BankAccount>` | List of accounts owned by the customer (one-to-many) |

### BankAccount Entity

| Field | Type | Description |
|---|---|---|
| `id` | `String` | Unique account identifier generated as a UUID |
| `createdAt` | `Date` | Account creation date |
| `balance` | `Double` | Current account balance |
| `currency` | `String` | Currency code, for example `MAD` |
| `type` | `AccountType` | Account category |
| `customer` | `Customer` | Associated customer (many-to-one) |

Supported account types are:

- `CURRENT_ACCOUNT`
- `SAVING_ACCOUNT`

The account type is stored as a string in the database using `@Enumerated(EnumType.STRING)`.

## 6. REST API

The REST controller base path is `/api`.

### List all accounts

```http
GET http://localhost:8081/api/bankAccounts
```

Returns all accounts currently stored in the database.

### Find one account

```http
GET http://localhost:8081/api/bankAccounts/{id}
```

Example:

```bash
curl http://localhost:8081/api/bankAccounts/<ACCOUNT_ID>
```

If the identifier does not exist, the service raises an account-not-found error.

### Create an account

```http
POST http://localhost:8081/api/bankAccounts
Content-Type: application/json
```

Request body:

```json
{
  "balance": 15000.0,
  "currency": "MAD",
  "type": "SAVING_ACCOUNT"
}
```

The service generates the UUID and creation date, saves the entity and returns a `BankAccountResponseDTO`.

### Update an account

```http
PUT http://localhost:8081/api/bankAccounts/{id}
Content-Type: application/json
```

Example body:

```json
{
  "balance": 17500.0,
  "currency": "MAD",
  "type": "CURRENT_ACCOUNT"
}
```

The update is partial for the supported fields: balance, currency, type and creation date.

### Delete an account

```http
DELETE http://localhost:8081/api/bankAccounts/{id}
```

Deletes the account identified by the path parameter.

## 7. GraphQL API

The GraphQL endpoint is available at `/graphql` with GraphiQL UI enabled for testing at `http://localhost:8081/graphiql`.

### GraphQL Schema

The schema defines the following types and operations:

**Types:**
- `BankAccount`: Represents a bank account with id, createdAt, balance, currency, type, and customer
- `Customer`: Represents a customer with id, name, and associated bank accounts
- `BankAccountDTO`: Input type for creating/updating accounts

**Queries:**
- `accountsList`: Returns all bank accounts
- `bankAccountById(id: String)`: Returns a specific bank account by ID
- `customers`: Returns all customers

**Mutations:**
- `addAccount(bankAccount: BankAccountDTO)`: Creates a new bank account
- `updateAccount(id: String, bankAccount: BankAccountDTO)`: Updates an existing bank account
- `deleteAccount(id: String)`: Deletes a bank account

### Example GraphQL Queries

**List all accounts:**
```graphql
query {
  accountsList {
    id
    balance
    currency
    type
    customer {
      id
      name
    }
  }
}
```

**Get account by ID:**
```graphql
query {
  bankAccountById(id: "ACCOUNT_ID") {
    id
    balance
    currency
    type
    createdAt
    customer {
      name
    }
  }
}
```

**List all customers:**
```graphql
query {
  customers {
    id
    name
    bankAccounts {
      id
      balance
      type
    }
  }
}
```

### Example GraphQL Mutations

**Create an account:**
```graphql
mutation {
  addAccount(bankAccount: {
    balance: 15000.0,
    currency: "MAD",
    type: "SAVING_ACCOUNT"
  }) {
    id
    balance
    currency
    type
  }
}
```

**Update an account:**
```graphql
mutation {
  updateAccount(id: "ACCOUNT_ID", bankAccount: {
    balance: 17500.0,
    currency: "MAD",
    type: "CURRENT_ACCOUNT"
  }) {
    id
    balance
    currency
    type
  }
}
```

**Delete an account:**
```graphql
mutation {
  deleteAccount(id: "ACCOUNT_ID")
}
```

## 8. Repository Features

`BankAccountRepository` extends `JpaRepository<BankAccount, String>`, which provides standard persistence operations such as `findAll`, `findById`, `save` and `deleteById`.

It also declares a query method for filtering accounts by type:

```java
List<BankAccount> findByType(AccountType type);
```

`CustomerRepository` extends `JpaRepository<Customer, Long>` and provides standard CRUD operations for customer entities.

Because Spring Data REST is included and both repositories are annotated with `@RepositoryRestResource`, repository endpoints may also be exposed by Spring Data REST in addition to the explicitly defined controller endpoints.

## 9. Database Configuration

The application uses the following development configuration:

```properties
spring.application.name=bak-account-service
spring.datasource.url=jdbc:h2:mem:account-db
spring.h2.console.enabled=true
server.port=8081
spring.graphql.graphiql.enabled=true
```

The database is in memory. Therefore, all data is lost when the application stops. The H2 console is enabled for local inspection; its exact URL can depend on the Spring Boot version and configuration.

## 10. Running the Project

### Prerequisites

- Java Development Kit 21 or later.
- A terminal or IDE such as IntelliJ IDEA or VS Code.
- Internet access for the first Maven dependency download.

### Start the application

On Linux or macOS:

```bash
./mvnw spring-boot:run
```

On Windows:

```bat
mvnw.cmd spring-boot:run
```

The service will be available at:

- REST API: `http://localhost:8081/api`
- GraphQL API: `http://localhost:8081/graphql`
- GraphiQL UI: `http://localhost:8081/graphiql`
- H2 Console: `http://localhost:8081/h2-console`

### Build the project

```bash
./mvnw clean package
```

### Run tests

```bash
./mvnw test
```

## 11. Testing

The project currently contains a Spring Boot context test:

```java
void contextLoads()
```

This test verifies that the application context can start successfully. Additional controller and repository tests would improve coverage of the CRUD operations and error cases. GraphQL-specific tests using `@GraphQlTest` could also be added to verify the GraphQL API functionality.

## 12. Technical Observations and Future Improvements

The current application is a functional educational prototype. The following improvements would be appropriate for a production-ready service:

- Add validation annotations such as `@NotNull`, `@PositiveOrZero` and `@Size` to request DTOs.
- Return explicit HTTP status codes and structured error responses.
- Replace generic runtime exceptions with dedicated exception handling using `@ControllerAdvice`.
- Add automated tests for create, read, update and delete operations for both REST and GraphQL APIs.
- Use `BigDecimal` instead of `Double` for monetary values.
- Use `Instant` or `LocalDateTime` instead of the legacy `Date` type.
- Configure a persistent database for data that must survive restarts.
- Add authentication and authorization before exposing financial operations.
- Implement GraphQL data loaders for N+1 query optimization when fetching customers with their accounts.
- Improve the startup sample-data logic so both account types are generated (currently only SAVING_ACCOUNT is generated).
- Review the update behavior so changing `createdAt` does not modify the original creation timestamp unintentionally.
- Add pagination and filtering capabilities to both REST and GraphQL APIs.
- Implement proper error handling for GraphQL operations using custom error handlers.

## 13. Conclusion

This project implements the core of a bank account management service with a clear Spring Boot layered architecture. It provides a practical foundation for studying REST APIs, GraphQL APIs, persistence, DTO mapping, entity relationships and service design in a distributed systems and DevOps training context. Its current H2 configuration makes experimentation simple, while the listed improvements provide a path toward a more robust and production-oriented implementation.

---

**Prepared by Hicham Ouaouche**  
**Second Year SDIA - ENSET Mohammedia**
