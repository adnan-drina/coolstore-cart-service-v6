# S02 Specification: Service Interfaces and External Integration

## Legacy Behavior and API Contract

### ShoppingCartService Interface

**Location:** `/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartService.java`

**Legacy Contract:**
The `ShoppingCartService` interface defines the core cart management operations for the Coolstore cart service. This is a service boundary interface that will be preserved in the target with method signatures unchanged.

**Method Signatures (evidence: lines 6-19):**
```java
ShoppingCart getShoppingCart(String cartId);
Product getProduct(String itemId);
ShoppingCart deleteItem(String cartId, String itemId, int quantity);
ShoppingCart checkout(String cartId);
ShoppingCart addItem(String cartId, String itemId, int quantity);
ShoppingCart set(String cartId, String tmpId);
void priceShoppingCart(ShoppingCart sc);
```

**Behavioral Requirements:**
- `getShoppingCart(String cartId)`: Returns a ShoppingCart for the given cart identifier
- `getProduct(String itemId)`: Looks up Product information by item ID (delegates to CatalogService)
- `deleteItem(String cartId, String itemId, int quantity)`: Removes specified quantity of item from cart
- `checkout(String cartId)`: Processes cart checkout and clears items
- `addItem(String cartId, String itemId, int quantity)`: Adds specified quantity of item to cart
- `set(String cartId, String tmpId)`: Transfers items from temporary cart to permanent cart
- `priceShoppingCart(ShoppingCart sc)`: Applies pricing calculations (promotions, shipping)

### CatalogService Interface

**Location:** `/projects/legacy/src/main/java/com/redhat/coolstore/service/CatalogService.java`

**Legacy Contract:**
The `CatalogService` is a Spring Cloud Feign client that provides external catalog integration. It retrieves product information from an external catalog service.

**Interface Definition (evidence: lines 1-14):**
```java
package com.redhat.coolstore.service;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.redhat.coolstore.model.Product;

@FeignClient(name = "catalogService", url = "${CATALOG_ENDPOINT}")
interface CatalogService {
    @GetMapping("/api/products")
    List<Product> products();
}
```

**Behavioral Requirements:**
- `products()`: Makes HTTP GET request to `${CATALOG_ENDPOINT}/api/products`
- Returns `List<Product>` from external catalog service
- Configuration driven by `${CATALOG_ENDPOINT}` environment variable (preserved contract from migration.yaml `preserve:` section)
- No fallback to mock data - must call real catalog endpoint

**Environment Configuration:**
- `${CATALOG_ENDPOINT}` configuration must be preserved through migration
- Falls back to localhost:8081 when not configured (application.properties:6)

## Integration Patterns

### ShoppingCartService Dependencies
- Depends on `Product`, `ShoppingCart`, `ShoppingCartItem` model classes (dependency-order.md:6,7)
- Used by `ShoppingCartServiceImpl` for cart lifecycle operations
- Called by `CartEndpoint` for REST API operations

### CatalogService Dependencies  
- References `Product` model class for type safety
- Used by `ShoppingCartServiceImpl` for product lookups and pricing
- Calls external catalog service at runtime

## Legacy Implementation Context

**Spring DI Patterns:**
- Both interfaces are currently used by Spring-managed implementations
- ShoppingCartService has no DI annotations (interface level)
- Implementation classes use Spring `@Autowired` for dependency injection

**External Service Integration:**
- CatalogService uses Spring Cloud Feign client pattern
- HTTP client configuration via environment variables
- Feign client provides declarative REST client interface

## Target Behavior Preservation

### ShoppingCartService
- Interface signatures must remain identical
- Implementation will be `@ApplicationScoped` with CDI constructor injection
- Service layer contracts preserved

### CatalogService
- Method signature preserved: `List<Product> products()`
- External catalog URL configuration preserved
- Target contract: **503** on downstream failures (not legacy behavior)
- No mock fallbacks - real HTTP calls to catalog service

**Test Evidence**

**Service Layer Behavior (ShoppingCartServiceTest.java):**
- Core pricing behavior established with specific assertion values
- Product lookup returns exact product by ID with matching name/description/price
- Cart totals calculation and promotional pricing validated

**External Integration Contract (CartServiceBoundaryTest.java):**
- End-to-end cart operations via REST API
- Product catalog integration through service layer
- HTTP contract with catalog service preserved

## Explicit Waivers

**UI Surface Waiver:**
- REST endpoints (`/cart/*`) explicitly owned by S04 - not in S02 scope
- Acceptance path (`/api/cart/acceptance-check`) served by S04 CartEndpoint
- Service interface modernization sufficient for S02 completion
- Story boundaries respected: service interfaces now, implementations later