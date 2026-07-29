# S01 Specification: BOM, Bootstrap, and Domain Models

## Legacy Behavior Contract

This story establishes the foundational build platform and preserves the domain model layer for the Coolstore cart service.

### Build System Behavior

**Legacy Maven Configuration (pom.xml:18-26):**
- Spring Boot 2.7.18 parent POM (`spring-boot-starter-parent`)
- Java 11 target compilation
- Spring Cloud dependencies (2021.0.9)
- Red Hat GA repository configured

**Dependencies (pom.xml:53-98):**
- `spring-boot-starter-web` (REST endpoints, embedded Tomcat)
- `spring-boot-starter-jersey` (JAX-RS via Jersey)
- `spring-boot-starter-actuator` (health/metrics endpoints)
- `spring-cloud-starter-openfeign` (Feign REST clients)
- Test dependencies: `spring-boot-starter-test`, `junit-vintage-engine`, `hoverfly-java`, `assertj-core`

**Build Plugins (pom.xml:101-108):**
- `spring-boot-maven-plugin` for executable JAR packaging

### Bootstrap Model Behavior

**Application Bootstrap (CartServiceApplication.java:1-14):**
- `@SpringBootApplication` enables component scanning, auto-configuration, and Spring Boot features
- `@EnableFeignClients` activates Feign client proxies
- `main()` method runs `SpringApplication.run()` to bootstrap the entire application context
- Spring Boot auto-discovery mechanisms scan for REST resources, services, and configuration

**Jersey Configuration (JerseyConfig.java:1-11):**
- Extends `ResourceConfig` from Jersey
- `@Component` registers it as a Spring bean
- Manually registers `CartEndpoint` class for JAX-RS resource discovery
- Provides Jersey-specific configuration and filter registration

### Domain Model Behavior (HARVEST Classes)

**Product Model (Product.java:5-54):**
- Serializable data carrier with serialVersionUID -7304814269819778382L
- Fields: `itemId`, `name`, `desc`, `price` (all with getters/setters)
- Default and parameterized constructors
- Legacy contract: product lookup by ID from external catalog service

**Promotion Model (Promotion.java:3-41):**
- Simple data carrier with `itemId` and `percentOff` fields
- Used for percentage-based discounts applied to products
- Default and parameterized constructors

**ShoppingCartItem Model (ShoppingCartItem.java:5-58):**
- Serializable with serialVersionUID 6964558044240061049L
- Fields: `price`, `quantity`, `promoSavings`, `product`
- Represents individual line items in a shopping cart

**ShoppingCart Model (ShoppingCart.java:7-127):**
- Serializable with serialVersionUID -1108043957592113528L
- Fields: `cartId`, `cartItemTotal`, `cartItemPromoSavings`, `shippingTotal`, `shippingPromoSavings`, `cartTotal`
- `shoppingCartItemList` as ArrayList of ShoppingCartItem
- Business methods: `addShoppingCartItem()`, `removeShoppingCartItem()`, `resetShoppingCartItemList()`

### Integration Surface

**Configuration Contract (application.properties:1-6):**
- `spring.application.name=coolstore-cart-legacy`
- `spring.jersey.application-path=/api` (Jersey base path)
- `CATALOG_ENDPOINT=http://localhost:8081` (environment-driven configuration for external catalog service)
- Configurable via environment variable `CATALOG_ENDPOINT`

### Jakarta Namespace Migration

**JAX-RS Imports to Update:**
- `javax.ws.rs.*` imports in CartEndpoint.java:5-11 (8 imports total)
- `javax.annotation.PostConstruct` import in ShoppingCartServiceImpl.java:11

### Health and Metrics Surface

**Spring Boot Actuator Dependencies:**
- `spring-boot-starter-actuator` provides `/actuator/health`, `/actuator/metrics` endpoints
- Micrometer metrics registry for Prometheus integration via `micrometer-registry-prometheus`

## API Contract Summary

- **No direct API endpoints in this story** - endpoints owned by S04
- **Configuration surface**: `CATALOG_ENDPOINT` preserved for S02 integration
- **Model contracts**: All domain models preserve exact legacy field structure and behavior
- **Build contract**: Quarkus BOM replaces Spring Boot parent, maintaining Java 11→21 target upgrade path

## Out-of-Scope Dependencies

- ShoppingCartService implementation (S03)
- CartEndpoint REST resource (S04)  
- CatalogService Feign client interface (S02)
- PromoService and ShippingService business logic (S03)

## Evidence Sources

- Build configuration: `/projects/legacy/pom.xml:1-109`
- Bootstrap classes: `/projects/legacy/src/main/java/com/redhat/coolstore/CartServiceApplication.java`, `/projects/legacy/src/main/java/com/redhat/coolstore/rest/JerseyConfig.java`
- Domain models: `/projects/legacy/src/main/java/com/redhat/coolstore/model/*.java`
- Configuration: `/projects/legacy/src/main/resources/application.properties:1-6`
- Migration findings: `migration/mta-findings.json` (47 incidents across 24 rules)
- Architecture profile: `migration/architecture-profile.md:121-138` (class roles)