# S02 Plan: Service Interfaces and External Integration

## Quarkus Mapping Strategy

This story modernizes the service interfaces and external integration layer, building on S01's platform foundation. The focus is on converting Spring dependency injection patterns to CDI and preparing Feign-based external service calls for Quarkus REST client integration.

### Class Modernization Strategy

**ShoppingCartService Interface - REDESIGN:**
- **Rewrite:** Preserve interface completely (method signatures unchanged)
- **Infer:** Convert implementation to `@ApplicationScoped` with CDI constructor injection
- **Evidence:** ShoppingCartService.java:6-19 shows clean interface without DI annotations

**CatalogService Interface - REDESIGN:**
- **Rewrite:** Replace Spring Cloud Feign imports with Quarkus REST client
- **Infer:** Convert to `@RegisterRestClient` interface with constructor injection
- **Evidence:** CatalogService.java:10 shows FeignClient annotation with `${CATALOG_ENDPOINT}` config

## Finding Resolution

### Spring DI Conversion

**Finding: springboot-di-to-quarkus-00003**
- **Target:** Apply Quarkus Spring DI conversion guidance
- **Decision:** Native CDI constructor injection (NOT spring-di extension per MAPPINGS)
- **Classes Affected:** ShoppingCartServiceImpl, PromoService, ShippingService, CartEndpoint, JerseyConfig
- **Task Classification:** infer (design decision required for CDI patterns)

### Environment Configuration Preservation

**Finding: demo-env-integration-00001**
- **Target:** `${CATALOG_ENDPOINT}` environment-driven configuration must be preserved
- **Decision:** Keep environment variable configuration, adapt for Quarkus REST client
- **Classes Affected:** CatalogService
- **Task Classification:** rewrite (mechanical config preservation)

### Jakarta Namespace Migration

**Finding: javax-to-jakarta-import-00001**
- **Target:** Replace `javax.*` imports with `jakarta.*`
- **Decision:** Already executed by OpenRewrite recipes (migration/recipe-log.md)
- **Task Classification:** harvest (mechanical import replacement already done)

## Interface Contract Mapping

### ShoppingCartService

**Legacy Interface (preserved):**
```java
public interface ShoppingCartService {
    ShoppingCart getShoppingCart(String cartId);
    Product getProduct(String itemId);
    ShoppingCart deleteItem(String cartId, String itemId, int quantity);
    ShoppingCart checkout(String cartId);
    ShoppingCart addItem(String cartId, String itemId, int quantity);
    ShoppingCart set(String cartId, String tmpId);
    void priceShoppingCart(ShoppingCart sc);
}
```

**Target Contract:**
- Interface signatures unchanged (architecture-profile §7)
- Implementation becomes `@ApplicationScoped` CDI bean
- Constructor injection for dependencies (ShippingService, PromoService, CatalogService)
- Thread-safe state management using ConcurrentHashMap

### CatalogService

**Legacy Interface:**
```java
@FeignClient(name = "catalogService", url = "${CATALOG_ENDPOINT}")
interface CatalogService {
    @GetMapping("/api/products")
    List<Product> products();
}
```

**Target Contract:**
- `@RegisterRestClient` annotation (replacing @FeignClient)
- JAX-RS `@GET` annotation (replacing @GetMapping)
- Environment configuration preserved: `${CATALOG_ENDPOINT}`
- Constructor injection support
- **503** on downstream failures (target contract, not legacy behavior)

## Dependency Order Integration

Per dependency-order.md conversion sequencing:

**Line 7:** `com.redhat.coolstore.service.ShoppingCartService`
- Interfaces converted before implementations per SEQUENCING.md §4
- Prepares service layer for S03 implementation conversion

**Line 12:** `com.redhat.coolstore.service.CatalogService`  
- External integration preparation for S03 implementation
- Environment-driven config preserved for cloud readiness

## Architecture Profile Compliance

**Class Roles:**
- ShoppingCartService: REDESIGN (interface preserved, CDI conversion)
- CatalogService: REDESIGN (Feign→REST client conversion)

**Target Contract Compliance:**
- Interface method signatures unchanged
- Environment configuration preserved
- CDI constructor injection patterns
- Exception handling strategy (503 on downstream failures)

## Test Strategy Alignment

**Characterization Tests:**
- Early placement per PLANNING.md guidance
- ShoppingCartServiceTest.java behavior validation
- Service interface contract preservation
- External integration contract verification

**Test Doubles:**
- CatalogService stubbing for test isolation
- No real HTTP calls in unit tests
- Test doubles for not-yet-converted REDESIGN types