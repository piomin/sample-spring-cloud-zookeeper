## Mastering Spring Cloud [![Twitter](https://img.shields.io/twitter/follow/piotr_minkowski.svg?style=social&logo=twitter&label=Follow%20Me)](https://twitter.com/piotr_minkowski)

[![CircleCI](https://circleci.com/gh/piomin/sample-spring-cloud-zookeeper.svg?style=svg)](https://circleci.com/gh/piomin/sample-spring-cloud-zookeeper)

[![SonarCloud](https://sonarcloud.io/images/project_badges/sonarcloud-black.svg)](https://sonarcloud.io/dashboard?id=piomin_sample-spring-cloud-zookeeper)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=piomin_sample-spring-cloud-zookeeper&metric=bugs)](https://sonarcloud.io/dashboard?id=piomin_sample-spring-cloud-zookeeper)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=piomin_sample-spring-cloud-zookeeper&metric=coverage)](https://sonarcloud.io/dashboard?id=piomin_sample-spring-cloud-zookeeper)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=piomin_sample-spring-cloud-zookeeper&metric=ncloc)](https://sonarcloud.io/dashboard?id=piomin_sample-spring-cloud-zookeeper)

## Architecture Overview

This project demonstrates a microservices architecture built with Spring Cloud and Apache Zookeeper for service discovery. The system consists of five microservices that work together to provide a complete e-commerce-like functionality.

```mermaid
flowchart TB
Client["Client Applications"] --> Gateway["Gateway Service:8080"]
Gateway --> Order["Order Service:8090"]
Gateway --> Customer["Customer Service:8092"]
Gateway --> Product["Product Service:8093"]
Gateway --> Account["Account Service:8091"]
Order --|Feign Client| Account
Order --|Feign Client| Product
Order --|Feign Client| Customer
Customer --|Feign Client| Account
Gateway -.-> Zookeeper["Apache Zookeeper:2181"]
Order -.-> Zookeeper
Customer -.-> Zookeeper
Product -.-> Zookeeper
Account -.-> Zookeeper
style Gateway fill:#e1f5fe
style Zookeeper fill:#fff3e0
style Order fill:#f3e5f5
style Customer fill:#e8f5e8
style Product fill:#fff8e1
style Account fill:#fce4ec
```

### Microservices Description

| Service             | Port | Description                          | Key Features                                                        | Feign Clients                        |
|---------------------|------|--------------------------------------|---------------------------------------------------------------------|--------------------------------------|
| **Gateway Service** | 8080 | API Gateway and routing              | • Routes requests to microservices<br>• Load balancing<br>• Single entry point | None                                 |
| **Order Service**   | 8090 | Order management                     | • Processes customer orders<br>• Orchestrates business logic<br>• Handles order lifecycle | AccountClient, ProductClient, CustomerClient |
| **Account Service** | 8091 | Account and balance management       | • Manages customer accounts<br>• Handles balance withdrawals<br>• Account balance tracking | None                                 |
| **Customer Service**| 8092 | Customer data management             | • Customer information storage<br>• Customer types: NEW, REGULAR, VIP<br>• Customer with accounts aggregation | AccountClient                        |
| **Product Service** | 8093 | Product catalog                      | • Product information and pricing<br>• Product lookup by IDs<br>• Inventory management | None                                 |

### Technology Stack

- **Framework**: Spring Boot 3.4.5
- **Service Discovery**: Apache Zookeeper
- **API Gateway**: Spring Cloud Gateway
- **Service Communication**: OpenFeign, RestTemplate
- **Build Tool**: Maven
- **Java Version**: 21
- **Configuration**: Spring Cloud Config with Zookeeper

### Service Communication Patterns

**Order Service** acts as the main orchestrator and communicates with:
- **CustomerClient** → Fetches customer data with accounts (`/withAccounts/{customerId}`)
- **ProductClient** → Retrieves product information by IDs (`/ids` POST endpoint)
- **AccountClient** → Processes account withdrawals (`/withdraw/{accountId}/{amount}`)

**Customer Service** enhances customer data by:
- **AccountClient** → Aggregates customer accounts (`/customer/{customerId}`)

## Prerequisites

Before running the application locally, ensure you have the following installed:

- **Java 21** or higher
- **Maven 3.6+**
- **Apache Zookeeper 3.7+**
- **Git**

## Running the Application Locally

### Step 1: Start Apache Zookeeper

1. **Download and Install Zookeeper** (if not already installed):
   ```bash
   wget https://archive.apache.org/dist/zookeeper/zookeeper-3.7.1/apache-zookeeper-3.7.1-bin.tar.gz
   tar -xzf apache-zookeeper-3.7.1-bin.tar.gz
   cd apache-zookeeper-3.7.1-bin
   ```

2. **Start Zookeeper**:
   ```bash
   cp conf/zoo_sample.cfg conf/zoo.cfg
   bin/zkServer.sh start
   ```
   Or via package manager:
   ```bash
   brew services start zookeeper
   sudo systemctl start zookeeper
   ```

3. **Verify Zookeeper is running**:
   ```bash
   echo stat | nc localhost 2181
   ```

### Step 2: Clone and Build the Project

```bash
git clone https://github.com/piomin/sample-spring-cloud-zookeeper.git
cd sample-spring-cloud-zookeeper
mvn clean install
```

### Step 3: Start the Microservices

**Option A: Start all services manually**  
Open separate terminals for each service:

```bash
# Gateway Service
cd gateway-service && mvn spring-boot:run

# Customer Service
cd customer-service && mvn spring-boot:run

# Account Service
cd account-service && mvn spring-boot:run

# Product Service
cd product-service && mvn spring-boot:run

# Order Service
cd order-service && mvn spring-boot:run
```

**Option B: Multi-zone for load testing**  
```bash
cd customer-service && mvn spring-boot:run -Dspring-boot.run.profiles=zone1 &
cd account-service && mvn spring-boot:run -Dspring-boot.run.profiles=zone1 &
cd product-service && mvn spring-boot:run -Dspring-boot.run.profiles=zone1 &
cd order-service && mvn spring-boot:run -Dspring-boot.run.profiles=zone1 &
cd gateway-service && mvn spring-boot:run -Dspring-boot.run.profiles=zone1 &
```

### Step 4: Verify the Setup

1. **Check service registration**  
   ```bash
   bin/zkCli.sh
   ls /services
   ```

2. **Test through the Gateway**  
   ```bash
   curl http://localhost:8080/customer/
   curl http://localhost:8080/product/
   curl http://localhost:8080/account/
   ```

### Step 5: Service Endpoints

| Service           | Direct URL            | Gateway URL                  | Example Endpoints                                     |
|-------------------|-----------------------|------------------------------|-------------------------------------------------------|
| Customer Service  | http://localhost:8092 | http://localhost:8080/customer/ | `GET /`<br>`GET /{id}`<br>`GET /withAccounts/{id}`     |
| Account Service   | http://localhost:8091 | http://localhost:8080/account/  | `GET /`<br>`GET /customer/{customerId}`<br>`PUT /withdraw/{accountId}/{amount}` |
| Product Service   | http://localhost:8093 | http://localhost:8080/product/  | `GET /`<br>`GET /{id}`<br>`POST /ids`                  |
| Order Service     | http://localhost:8090 | http://localhost:8080/order/    | `POST /`<br>`GET /{id}`                                |

## Testing the Application

### Sample API Calls

```bash
curl http://localhost:8080/customer/
curl http://localhost:8080/customer/withAccounts/1
curl -X POST http://localhost:8080/product/ids -H "Content-Type: application/json" -d '[1,2,3]'
curl http://localhost:8080/account/customer/1
curl -X POST http://localhost:8080/order/ -H "Content-Type: application/json" -d '{"customerId":1,"productIds":[1,2],"status":"NEW"}'
curl -X PUT http://localhost:8080/account/withdraw/1/1000
```

### Sample Data

- **Customers**: 3 customers (NEW, REGULAR, VIP)  
- **Products**: 10 products with prices between 800–3500  
- **Accounts**: 9 accounts, each starting with a 50,000 balance  

## Troubleshooting

1. **Zookeeper Connection Issues**  
   - Ensure Zookeeper is running on port 2181  
   - Verify configuration in application.yml  
   - Check firewall settings  

2. **Service Registration Problems**  
   - Use `echo stat | nc localhost 2181`  
   - Check logs for connection errors  
   - Ensure unique service names  

3. **Port Conflicts**  
   - Run `netstat -an | grep 808`  
   - Modify ports in application.yml if needed  

4. **Feign Client Errors**  
   - Verify services are registered  
   - Check Feign client configuration matches service names  

## Logs & Monitoring

- Service logs output via `CommonsRequestLoggingFilter`  
- Enable debug: `logging.level.org.springframework.web.filter.CommonsRequestLoggingFilter=DEBUG`  
- Zookeeper logs in installation directory  
- Health check: `curl http://localhost:8080/actuator/health`  

## Development Notes

- In-memory repositories with pre-populated test data  
- Stateless services for scalability  
- Feign clients with Spring Cloud LoadBalancer  

## Project Structure

```
sample-spring-cloud-zookeeper/
├── gateway-service/      # API Gateway (Spring Cloud Gateway)
├── order-service/        # Order processing service
│   └── client/           # Feign clients for external services
├── customer-service/     # Customer management service
│   └── client/           # Feign client for accounts
├── account-service/      # Account and balance management
├── product-service/      # Product catalog service
└── pom.xml               # Parent Maven configuration
```

## Related Resources

- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)  
- [Apache Zookeeper Documentation](https://zookeeper.apache.org/doc/current/)  
- [Spring Cloud Gateway Documentation](https://spring.io/projects/spring-cloud-gateway)  
- [OpenFeign Documentation](https://spring.io/projects/spring-cloud-openfeign)  
- [Mastering Spring Cloud Book](https://www.packtpub.com/application-development/mastering-spring-cloud)
This repository is an example of application created for a demo of content described in my book: [Mastering Spring Cloud](https://www.packtpub.com/application-development/mastering-spring-cloud) 