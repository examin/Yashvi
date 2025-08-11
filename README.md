# Setup and Run Guide for Shamir’s Secret Sharing Spring Boot Project

Follow these steps to clone, build, and run the project locally.

***

## 1. Prerequisites

- **Java Development Kit (JDK) 17+**  
- **Maven 3.6+**  
- **Git**  
- (Optional) **jq** for pretty-printing JSON in cURL tests  

***

## 2. Clone the Repository

```bash
git clone https://github.com//shamir-secret-sharing.git
cd shamir-secret-sharing
```

*(Replace `` with your GitHub username.)*

***

## 3. Review the Project Structure

```
shamir-secret-sharing/
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/catalog/secretsharing/
│   │   │   ├── ShamirSecretSharingApplication.java
│   │   │   ├── controller/SecretSharingController.java
│   │   │   ├── model/{Keys,RootPoint,TestCaseInput,SecretResponse}.java
│   │   │   └── service/{BaseDecoderService,LagrangeInterpolationService,SecretSharingService}.java
│   │   └── resources/application.properties
│   └── test/java/com/catalog/secretsharing/service/SecretSharingServiceTest.java
└── test-cases/{test-case-1.json,test-case-2.json}
```

***

## 4. Configure Application Properties

Defaults are in `src/main/resources/application.properties`:

```properties
server.port=8080
spring.jackson.serialization.write-dates-as-timestamps=false
springdoc.swagger-ui.path=/swagger-ui.html
management.endpoints.web.exposure.include=health,info
```

You can change `server.port` if 8080 is in use.

***

## 5. Build the Project

From the project root:

```bash
mvn clean install
```

This compiles the code, runs all unit tests, and packages the application.

***

## 6. Run the Application

```bash
mvn spring-boot:run
```

You should see startup logs ending with:

```
🔐 SHAMIR'S SECRET SHARING APPLICATION STARTED
📄 API Documentation: http://localhost:8080/swagger-ui/index.html
🌐 Base URL: http://localhost:8080/api/secret-sharing
```

***

## 7. Access API Documentation

Open in your browser:

```
http://localhost:8080/swagger-ui/index.html
```

This provides interactive Swagger UI for all endpoints.

***

## 8. Run Predefined Test Cases

Using cURL (or your HTTP client):

```bash
# Test Case 1
curl -s http://localhost:8080/api/secret-sharing/test/1 | jq '.'

# Test Case 2
curl -s http://localhost:8080/api/secret-sharing/test/2 | jq '.'
```

***

## 9. Calculate via JSON Payload

```bash
curl -X POST http://localhost:8080/api/secret-sharing/calculate \
  -H "Content-Type: application/json" \
  -d @test-cases/test-case-1.json | jq '.'
```

***

## 10. Calculate via File Upload

```bash
curl -X POST http://localhost:8080/api/secret-sharing/calculate-from-file \
  -F "file=@test-cases/test-case-1.json" | jq '.'
```

***

## 11. Health Check & Info

```bash
curl -s http://localhost:8080/api/secret-sharing/health | jq '.'
curl -s http://localhost:8080/api/secret-sharing/info  | jq '.'
```

***

## 12. Customize & Extend

- **Add new test cases** under `test-cases/`  
- **Modify polynomial degree** by changing input JSON  
- **Adjust logging levels** in `application.properties`  

***

You’re now ready to develop, test, and extend the Shamir’s Secret Sharing Spring Boot application!
