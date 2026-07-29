# S02: Service interfaces and external integration

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

What this story achieves and why it is next: its place in the roadmap,
what it unblocks, which stories it depends on (cite
dependency-order.md / architecture-profile.md).

This story modernizes the service interfaces and external integration layer, building on the stable foundation from S01. It converts Spring DI annotations to CDI for the service interface (dependency-order.md line 7) and prepares the Feign-based CatalogService for conversion to Quarkus REST client (dependency-order.md line 12). The story preserves the critical `CATALOG_ENDPOINT` environment configuration that must be maintained through migration (demo-env-integration-00001). Depends on S01 for the Quarkus platform foundation.

## In scope

The exact legacy classes/files this story modernizes. For each, quote
the load-bearing legacy code (the lines being transformed — imports,
annotations, key methods), so the story never starts from a blank
read:

- `src/main/java/com/redhat/coolstore/service/ShoppingCartService.java` — Service interface (PRESERVE with CDI annotations)
  ```java
  package com.redhat.coolstore.service;

  import com.redhat.coolstore.model.Product;
  import com.redhat.coolstore.model.ShoppingCart;

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

- `src/main/java/com/redhat/coolstore/service/CatalogService.java` — Feign REST client (PREPARE for Quarkus REST client)
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

## Out of scope

What neighboring code this story must NOT touch, and which story owns
it. (The tree must stay buildable: name any temporary seams — e.g. a
dependent class that keeps compiling against the old shape until its
own story.)

- Service implementations (ShoppingCartServiceImpl, PromoService, ShippingService) - owned by S03
- REST endpoint (CartEndpoint) - owned by S04
- Bootstrap artifacts (already handled in S01)

## Class roles & target contract (from architecture-profile §7)

For each in-scope class, its role and — for REDESIGN classes — the target
contract carried forward from profile §7, so M3 writes tasks and tests to
the target (not the legacy):

- `com.redhat.coolstore.service.ShoppingCartService` — REDESIGN
  - (REDESIGN only) target: Interface preserved, implementation becomes `@ApplicationScoped` with CDI constructor injection (architecture-profile §7). Interface remains same signatures.
- `com.redhat.coolstore.service.CatalogService` — REDESIGN
  - (REDESIGN only) target: `@RegisterRestClient` interface with constructor injection. Target: **503** on downstream failures, environment-driven URL config preserved (CATALOG_ENDPOINT). Contract: GET `/api/products` returns List<Product>, 503 on catalog service unavailable.

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

**Dependency injection conversion (MAPPINGS spring-di→quarkus):**
- Apply Quarkus Spring DI conversion guidance for common Spring DI annotations
- Decided target: native CDI constructor injection (NOT the spring-di extension)

**Spring Web conversion (MAPPINGS spring-web→quarkus):**
- Replace the Spring Web artifact with Quarkus 'spring-web' extension
- Decided target: native JAX-RS resources (NOT the spring-web extension)

**Environment-driven configuration (MAPPINGS cloud-readiness):**
- Local HTTP Calls → env-driven config (`${VAR:default}`)
- Environment-driven external configuration must be preserved
- Decided target: record under migration.yaml `preserve:`; target keeps env-driven config (`${VAR:default}` / `quarkus.rest-client.<key>.url`)

**Story ordering:** interfaces before implementations per SEQUENCING.md §4.

## Contracts owned by this story

- **Findings**: (interface-level findings handled in S01 foundation work)
- **Preserve**: CATALOG_ENDPOINT environment-driven configuration (from migration.yaml preserve:) - the `${CATALOG_ENDPOINT}` URL configuration must be maintained through migration
- **Behavioral pins**: Service interface contracts preserved - ShoppingCartService method signatures unchanged, CatalogService contract: List<Product> products() from `${CATALOG_ENDPOINT}/api/products`
- **Forbidden**: getMockProducts, "Fallback to mock" - CatalogService must call real catalog endpoint, not mock data

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- Service interface converted to use CDI (no Spring @Autowired at interface level)
- CatalogService preserves CATALOG_ENDPOINT configuration (not hardcoded localhost)
- Interface methods compile and are callable from implementation layer (S03)
- No runtime calls to CatalogService yet (implementation comes in S03)
- deploy story only: factory pipeline green, deployed, acceptance path
  serving (NOT APPLICABLE - deploy: false)
