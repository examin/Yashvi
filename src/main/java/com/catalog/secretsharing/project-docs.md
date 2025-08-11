# Test Case JSON Files

## test-case-1.json
```json
{
    "keys": {
        "n": 4,
        "k": 3
    },
    "1": {
        "base": "10",
        "value": "4"
    },
    "2": {
        "base": "2",
        "value": "111"
    },
    "3": {
        "base": "10",
        "value": "12"
    },
    "6": {
        "base": "4",
        "value": "213"
    }
}
```

## test-case-2.json
```json
{
    "keys": {
        "n": 10,
        "k": 7
    },
    "1": {
        "base": "6",
        "value": "13444211440455345511"
    },
    "2": {
        "base": "15",
        "value": "aed7015a346d63"
    },
    "3": {
        "base": "15",
        "value": "6aeeb69631c227c"
    },
    "4": {
        "base": "16",
        "value": "e1b5e05623d881f"
    },
    "5": {
        "base": "8",
        "value": "316034514573652620673"
    },
    "6": {
        "base": "3",
        "value": "2122212201122002221120200210011020220200"
    },
    "7": {
        "base": "3",
        "value": "20120221122211000100210021102001201112121"
    },
    "8": {
        "base": "6",
        "value": "20220554335330240002224253"
    },
    "9": {
        "base": "12",
        "value": "45153788322a1255483"
    },
    "10": {
        "base": "7",
        "value": "1101613130313526312514143"
    }
}
```

## Application Properties
```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/

# Application Configuration
spring.application.name=shamir-secret-sharing

# Logging Configuration
logging.level.com.catalog.secretsharing=DEBUG
logging.level.org.springframework.web=INFO
logging.level.root=INFO

# JSON Configuration
spring.jackson.default-property-inclusion=NON_NULL
spring.jackson.serialization.write-dates-as-timestamps=false

# Swagger/OpenAPI Configuration
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true

# File Upload Configuration
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Actuator Configuration
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always
```

## README.md Content

# Shamir's Secret Sharing - Spring Boot Implementation

## Problem Analysis

This project implements **Shamir's Secret Sharing** algorithm to reconstruct polynomial secrets from encoded y-values using **Lagrange interpolation**.

### Core Concepts

1. **Polynomial Reconstruction**: Given k points, we can uniquely reconstruct a polynomial of degree k-1
2. **Base Decoding**: Y-values are encoded in various bases (2-36) and must be decoded to decimal
3. **Secret Extraction**: The secret is the constant term (coefficient of x⁰) of the polynomial
4. **Lagrange Interpolation**: Mathematical method to find polynomial coefficients from points

### Algorithm Flow

1. **Input Parsing**: Parse JSON containing n points with k minimum required
2. **Base Conversion**: Decode y-values from their respective bases to decimal
3. **Point Selection**: Choose k points from n available points
4. **Lagrange Interpolation**: Calculate f(0) to get the constant term (secret)
5. **Response**: Return secret with metadata and processing details

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+

### Running the Application

```bash
# Clone and navigate to project
cd shamir-secret-sharing

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start at `http://localhost:8080`

### API Endpoints

- **POST** `/api/secret-sharing/calculate` - Calculate secret from JSON
- **POST** `/api/secret-sharing/calculate-verified` - Calculate with verification
- **POST** `/api/secret-sharing/calculate-from-file` - Upload JSON file
- **GET** `/api/secret-sharing/test/{1|2}` - Run predefined test cases
- **GET** `/api/secret-sharing/info` - Algorithm information
- **GET** `/swagger-ui/index.html` - API Documentation

### Example Usage

```bash
# Test with predefined test case 1
curl -X GET http://localhost:8080/api/secret-sharing/test/1

# Test with JSON payload
curl -X POST http://localhost:8080/api/secret-sharing/calculate \
  -H "Content-Type: application/json" \
  -d @test-cases/test-case-1.json
```

### Expected Results

- **Test Case 1**: Secret = `3`
- **Test Case 2**: Secret = `{large number}`

## Architecture

### Key Components

- **BaseDecoderService**: Converts values from any base (2-36) to decimal
- **LagrangeInterpolationService**: Implements polynomial reconstruction
- **SecretSharingService**: Orchestrates the complete algorithm
- **SecretSharingController**: REST API endpoints

### Technical Features

- **High Precision Math**: Uses BigDecimal for accurate calculations
- **Base Validation**: Validates input values for their respective bases  
- **Error Handling**: Comprehensive validation and error reporting
- **Caching**: Performance optimization for base conversions
- **Documentation**: Swagger/OpenAPI documentation
- **Testing**: Comprehensive unit and integration tests

## Project Structure

```
src/main/java/com/catalog/secretsharing/
├── ShamirSecretSharingApplication.java
├── controller/
│   └── SecretSharingController.java
├── model/
│   ├── Keys.java
│   ├── RootPoint.java  
│   ├── TestCaseInput.java
│   └── SecretResponse.java
├── service/
│   ├── BaseDecoderService.java
│   ├── LagrangeInterpolationService.java
│   └── SecretSharingService.java
└── util/
    └── MathUtils.java
```

## Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## Mathematical Background

The implementation uses **Lagrange Interpolation** to reconstruct polynomials:

For points (x₀,y₀), (x₁,y₁), ..., (xₖ₋₁,yₖ₋₁), the polynomial is:

P(x) = Σᵢ yᵢ × Lᵢ(x)

Where Lᵢ(x) = Πⱼ≠ᵢ (x-xⱼ)/(xᵢ-xⱼ)

The secret is P(0) = Σᵢ yᵢ × Lᵢ(0)

## Contributing

1. Fork the repository
2. Create feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit pull request

## License

This project is created for the Catalog assignment and is for educational purposes.