# S02 Service Interfaces and External Integration Tasks

#### T-001: Preserve ShoppingCartService interface contract
|**Class**: rewrite
|**Findings**: none (interface-level changes handled in S01)
|**Goal**: Maintain ShoppingCartService interface method signatures unchanged
|**Target design**: 
- Legacy interface `/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartService.java` → `src/main/java/com/demo/service/ShoppingCartService.java`
- Interface signatures preserved exactly (getShoppingCart, getProduct, deleteItem, checkout, addItem, set, priceShoppingCart)
- No DI annotations at interface level (already clean)
|**Acceptance**: ShoppingCartService.java interface preserved in target; method signatures unchanged

#### T-002: Convert CatalogService from Feign to REST client
|**Class**: rewrite
|**Findings**: demo-env-integration-00001 (1)
|**Goal**: Replace Spring Cloud Feign imports with Quarkus REST client annotations
|**Target design**:
- Legacy file `/projects/legacy/src/main/java/com/redhat/coolstore/service/CatalogService.java` → `src/main/java/com/demo/service/CatalogService.java`
- `@FeignClient` → `@RegisterRestClient`
- `@GetMapping("/api/products")` → `@GET @Path("/api/products")`
- Environment configuration `${CATALOG_ENDPOINT}` preserved
|**Acceptance**: CatalogService.java with REST client annotations; CATALOG_ENDPOINT config preserved

#### T-003: Create minimal acceptance endpoint placeholder
|**Class**: rewrite
|**Findings**: none (deployment validation requirement)
|**Goal**: Create placeholder endpoint to satisfy migration.yaml acceptance path requirement
|**Target design**:
- New file `src/main/java/com/demo/rest/AcceptanceEndpoint.java`
- JAX-RS resource: `@ApplicationScoped @Path("/api/cart")`  
- Method: `GET /acceptance-check` returns simple status JSON
- Placeholder implementation: `{"status": "service_interfaces_ready", "story": "S02"}`
- Marked as S02 placeholder - to be extended by S04 CartEndpoint
|**Acceptance**: `/api/cart/acceptance-check` serves placeholder response; migration.yaml acceptance path satisfied

#### T-004: Implement CDI for ShoppingCartService interface
|**Class**: infer
|**Findings**: springboot-di-to-quarkus-00003 (dependency injection pattern)
|**Goal**: Convert ShoppingCartService implementation to CDI with constructor injection
|**Target design**:
- Legacy file `/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java` → `src/main/java/com/demo/service/ShoppingCartServiceImpl.java`
- Interface: ShoppingCartService → unchanged signatures
- Class: @ApplicationScoped (not @Singleton)
- Dependencies: constructor injection for ShippingService, PromoService, CatalogService
- Thread-safe state: ConcurrentHashMap for cart storage
- **Architecture-profile §7**: normalize-before-derive (dedupeCartItems() before pricing), no-clear-on-miss product cache with bounded refresh policy
- Business logic: pricing calculations, promotional logic, shipping calculations preserved
|**Acceptance**: ShoppingCartServiceImpl.java with CDI annotations; compilation passes; service methods callable

#### T-005: Implement CDI for CatalogService with REST client
|**Class**: infer
|**Findings**: demo-env-integration-00001 (1)
|**Goal**: Convert CatalogService to use Quarkus REST client with environment config
|**Target design**:
- Legacy file `/projects/legacy/src/main/java/com/redhat/coolstore/service/CatalogService.java` → `src/main/java/com/demo/service/CatalogService.java`
- Interface: @RegisterRestClient with constructor injection
- URL config: ${CATALOG_ENDPOINT} preserved (or quarkus.rest-client.catalog-service.url)
- Method: products() returns List<Product>
- Exception handling: 503 on downstream failures (target contract)
- No fallback to mock data (forbidden per brief)
|**Acceptance**: CatalogService.java with REST client; environment config preserved; no mock fallbacks

#### T-006: Port characterization tests for service interfaces
|**Class**: infer
|**Findings**: none (test porting required for contract validation)
|**Goal**: Port legacy service tests to validate interface contracts and external integration
|**Target design**:
- Legacy test `/projects/legacy/src/test/java/com/redhat/coolstore/service/ShoppingCartServiceTest.java` → `src/test/java/com/demo/service/ShoppingCartServiceTest.java`
- Test doubles for CatalogService (real HTTP calls forbidden in unit tests)
- Behavior validation: pricing calculations, promotional logic, cart operations
- Assertion values preserved: cart totals, shipping promotions, product lookups
- Mock CatalogService for isolation (not-yet-converted REDESIGN type per PLANNING.md guidance)
|**Acceptance**: ShoppingCartServiceTest.java compiles and passes; interface contracts validated; service behavior pinned

## Explicit Waivers

**Legacy UI Surface Coverage**: The legacy REST API surface (GET `/cart/{cartId}`, POST `/cart/{cartId}/{itemId}/{quantity}`, POST `/cart/{cartId}/{tmpId}`, DELETE `/cart/{cartId}/{itemId}/{quantity}`, POST `/checkout/{cartId}`) is explicitly waived in S02. CartEndpoint is classified REDESIGN per architecture profile §7 and will be handled in S04 CartEndpoint story. S02 scope is limited to service interfaces (ShoppingCartService, CatalogService) and external integration.