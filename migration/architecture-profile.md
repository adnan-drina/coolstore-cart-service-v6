# M1 Architecture Profile — Coolstore Cart Service

## 1. Purpose & domain

The Coolstore cart service is a shopping cart management microservice that provides cart lifecycle operations (create, add items, remove items, checkout) with integrated pricing, promotional discounts, and shipping calculation for an e-commerce platform. It serves as the central cart state manager for the Coolstore application, maintaining persistent cart state across user sessions and applying business rules for pricing and promotions.

The core domain concepts are:
- **ShoppingCart**: A cart with a unique ID containing line items, pricing totals, and promotional state
- **ShoppingCartItem**: A line item with product reference, quantity, unit price, and promotional savings
- **Product**: Catalog product with ID, name, description, and current price
- **Promotion**: Percentage-based discounts applied to specific products or cart-level shipping

The service implements a tiered shipping cost structure (2.99, 4.99, 6.99, 8.99, 10.99 based on cart totals) and applies promotional pricing including 25% off product "329299" and free shipping for carts over $75 (src/main/java/com/redhat/coolstore/service/PromoService.java:27, src/main/java/com/redhat/coolstore/service/ShippingService.java:12-22).

## 2. Components & relationships

The application follows a layered architecture with REST endpoint → service layer → external service pattern (dependency-order.md:2-4,19-29):

```
┌──────────────────┐
│   CartEndpoint   │ JAX-RS resource
│   (@RestController)
└────────┬─────────┘
         │ depends on
         ▼
┌──────────────────┐
│ShoppingCartService│ Service layer
│  (ShoppingCart
│   ServiceImpl)
└────────┬─────────┘
         │ uses
         ▼
┌──────────────────┐
│  PromoService    │ Promotion engine
│  ShippingService │ Shipping calculator
│  CatalogService  │ Feign REST client
└────────┬─────────┘
         │ fetches from
         ▼
    External Catalog
      Service (/api/products)
```

God nodes identified by dependency analysis (dependency-order.md:8-14 shows fan-in analysis):
- ShoppingCart (fan-in: 5) — central domain object referenced by all services
- Product (fan-in: 4) — shared product data used across pricing calculations  
- ShoppingCartItem (fan-in: 3) — line item structure used by cart operations

The service layer is tightly coupled to domain models (src/main/java/com/redhat/coolstore/model/ShoppingCart.java:1, src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java:1, src/main/java/com/redhat/coolstore/model/Product.java:1) and has bidirectional dependencies between ShoppingCartService and the pricing services (src/main/java/com/redhat/coolstore/service/PromoService.java:1, src/main/java/com/redhat/coolstore/service/ShippingService.java:1). This coupling creates conversion risk — these services must be modernized together to maintain cart pricing behavior.

## 3. Integration surfaces

**Exposed REST API** (CartEndpoint.java:24,31-68):
- GET `/cart/{cartId}` — retrieve cart by ID, returns ShoppingCart JSON
- POST `/cart/{cartId}/{itemId}/{quantity}` — add item to cart
- POST `/cart/{cartId}/{tmpId}` — transfer items from temporary cart
- DELETE `/cart/{cartId}/{itemId}/{quantity}` — remove specified quantity of item
- POST `/checkout/{cartId}` — checkout cart and reset items

Base path configured via JerseyConfig.java:9 registers CartEndpoint at `/cart`. Spring Boot application path `/api` set in application.properties:3, making effective API path `/api/cart/*`.

**Consumed External Service** (CatalogService.java:10-13):
- Feign client to `${CATALOG_ENDPOINT}/api/products` 
- Retrieves Product list for pricing calculations
- Configuration: `CATALOG_ENDPOINT` env/property (application.properties:6)
- Falls back to localhost:8081 when not configured

**Persistence Surface**: In-memory HashMap cart storage (ShoppingCartServiceImpl.java:42,49) — NOT persistent across service restarts. Product cache (ShoppingCartServiceImpl.java:44) with no expiration policy.

**Preserve contract candidate**: The `CATALOG_ENDPOINT` environment-driven configuration must be preserved (demo-env-integration-00001) — target should maintain `${CATALOG_ENDPOINT}` or `quarkus.rest-client.catalog-service.url` configuration.

## 4. Behavioral contract sources

The expected behavior is pinned by three test classes with specific assertion values:

**ShoppingCartServiceTest.java** establishes core pricing behavior:
- Line 31-35: New cart initializes with all totals at 0.0 (cartItemPromoSavings, cartItemTotal, shippingPromoSavings, cartTotal)
- Line 49-53: Adding 2 units of $1000 product produces $2000 cartItemTotal, -10.99 shippingPromoSavings (free shipping promo), $2000 cartTotal
- Line 58-63: Product lookup returns exact product by ID with matching name/description/price

**CartServiceBoundaryTest.java** validates end-to-end behavior:
- Line 36: HTTP POST `/api/cart/1/1111/2` returns ShoppingCart with same totals as service-level test
- Line 40-42: Matches service test expectations — 2000.0 cartItemTotal, -10.99 shippingPromoSavings

**ProductsObjectMother.java** provides contract test data:
- Line 11: Product "1111" = Car, Super car, 1000.0
- Line 12: Product "2222" = Bike, Super bike, 200.0

**Contract gaps identified**:
- No tests for deleteItem() behavior with partial quantity removal
- No tests for cart session persistence across service calls
- No tests for invalid product ID handling in addItem()
- No tests for shipping calculation at different cart total thresholds

These gaps represent behavioral uncertainty that migration specs must close with characterization tests.

## 5. Modernization surface

**MUST change** (mandatory findings, findings-inventory.md):
- **javax-to-jakarta-import-00001**: CartEndpoint.java:5-11, ShoppingCartServiceImpl.java:11 — replace `javax.*` imports with `jakarta.*` 
- **jakarta-jaxrs-to-quarkus-00010**: pom.xml:60 — replace JAX-RS dependency with `quarkus-rest`
- **javaee-pom-to-quarkus-***: pom.xml:4,17,104 — adopt Quarkus BOM, Maven plugins, spring-boot→quarkus-maven-plugin
- **springboot-annotations-to-quarkus-00000**: CartServiceApplication.java:7 — remove `@SpringBootApplication` + main class (Quarkus auto-discovers)
- **springboot-actuator-to-quarkus-0100**: pom.xml:65 — replace Spring Actuator with `quarkus-smallrye-health`

**SHOULD change** (optional findings):
- **springboot-di-to-quarkus-00003**: CartEndpoint.java:28, JerseyConfig.java:6, PromoService.java:15, ShippingService.java:7, ShoppingCartServiceImpl.java:28-39 — convert Spring `@Autowired` to CDI constructor injection
- **springboot-metrics-to-quarkus-0200**: pom.xml:65 — replace Micrometer with MP Metrics

**Platform contract examination**:
- **localhost-http-00001**: application.properties:6, ShoppingCartServiceTest.java:18 — hardcoded localhost URLs → env-driven config for cloud readiness
- **demo-env-integration-00001**: application.properties:6 — `CATALOG_ENDPOINT` configuration must be preserved through migration

## 6. Domain boundaries

This is effectively a single bounded context focused on cart lifecycle management. While the service has internal component separation (endpoint, services, models), these components are tightly coupled through shared domain objects (src/main/java/com/redhat/coolstore/model/ShoppingCart.java:1, src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:1) and cross-references that prevent meaningful independent modernization (dependency-order.md:18-29 shows tightly coupled conversion order preventing independent modernization).

The only candidate seam is the **pricing subdomain** (PromoService, ShippingService, ShoppingCartServiceImpl pricing methods), which could theoretically be extracted as a separate service. However, this separation would require major architectural changes including shared pricing cache strategy and distributed cart state management. The current tight coupling makes incremental modernization of individual components impractical.

## 7. Class roles & target contract

Every class classified for modernization:

**HARVEST classes** (data/DTO/value-object carried over faithfully):
- `com.redhat.coolstore.model.Product` — immutable data carrier, copy unchanged
- `com.redhat.coolstore.model.ShoppingCartItem` — immutable data carrier, copy unchanged  
- `com.redhat.coolstore.model.ShoppingCart` — immutable data carrier, copy unchanged
- `com.redhat.coolstore.model.Promotion` — immutable data carrier, copy unchanged

**REDESIGN classes** (runtime behavior, CDI/JAX-RS, modernized):
- `com.redhat.coolstore.rest.CartEndpoint` (src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:21-23,28) → `@ApplicationScoped @Path` JAX-RS resource with constructor injection. Target contract: GET returns **404** on missing cart (never creates), POST endpoints **400** on invalid input, **503** via JAX-RS ExceptionMapper on catalog failures.
- `com.redhat.coolstore.service.ShoppingCartService` (src/main/java/com/redhat/coolstore/service/ShoppingCartService.java:1) → interface preserved, implementation becomes `@ApplicationScoped` with thread-safe state using **ConcurrentHashMap** and compute() methods for atomic updates.
- `com.redhat.coolstore.service.ShoppingCartServiceImpl` (src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:28,33-40) → `@ApplicationScoped` with CDI constructor injection (ShippingService, CatalogService, PromoService). Target: **normalize-before-derive** (dedupeCartItems() before pricing), no-clear-on-miss product cache with bounded refresh policy.
- `com.redhat.coolstore.service.PromoService` (src/main/java/com/redhat/coolstore/service/PromoService.java:15) → `@ApplicationScoped` with constructor injection. Business logic preserved: 25% off "329299", free shipping over $75.
- `com.redhat.coolstore.service.ShippingService` (src/main/java/com/redhat/coolstore/service/ShippingService.java:7) → `@ApplicationScoped` with constructor injection. Business logic preserved: tiered shipping costs (2.99/4.99/6.99/8.99/10.99).
- `com.redhat.coolstore.service.CatalogService` (src/main/java/com/redhat/coolstore/service/CatalogService.java:10) → `@RegisterRestClient` interface with constructor injection. Target: **503** on downstream failures, environment-driven URL config preserved.
- `com.redhat.coolstore.CartServiceApplication` (src/main/java/com/redhat/coolstore/CartServiceApplication.java:7) → removed — Quarkus auto-discovery subsumes Spring Boot bootstrap.
- `com.redhat.coolstore.rest.JerseyConfig` (src/main/java/com/redhat/coolstore/rest/JerseyConfig.java:6) → removed — Quarkus auto-discovers JAX-RS resources, Jersey config unnecessary.
