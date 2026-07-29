# S03: Core pricing services (tightly coupled group)

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

What this story achieves and why it is next: its place in the roadmap,
what it unblocks, which stories it depends on (cite
dependency-order.md / architecture-profile.md).

This story modernizes the tightly coupled pricing service layer that forms the core business logic of the cart service. Per architecture-profile §2, PromoService, ShippingService, and ShoppingCartServiceImpl have bidirectional dependencies through shared pricing logic and must be modernized together to maintain cart pricing behavior (dependency-order.md lines 8-10, 15-17). Depends on S02 which establishes the service interface contracts.

## In scope

The exact legacy classes/files this story modernizes. For each, quote
the load-bearing legacy code (the lines being transformed — imports,
annotations, key methods), so the story never starts from a blank
read:

- `src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java` — Main service implementation (CDI + thread-safe state)
  ```java
  package com.redhat.coolstore.service;

  import com.redhat.coolstore.model.Product;
  import com.redhat.coolstore.model.ShoppingCart;
  import com.redhat.coolstore.model.ShoppingCartItem;
  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.stereotype.Service;

  import javax.annotation.PostConstruct;
  import java.util.ArrayList;
  import java.util.HashMap;
  import java.util.List;
  import java.util.Map;
  import java.util.function.Function;
  import java.util.stream.Collectors;

  @Service
  public class ShoppingCartServiceImpl implements ShoppingCartService {

      private static final Logger LOG = LoggerFactory.getLogger(ShoppingCartServiceImpl.class);

      @Autowired
      ShippingService ss;

      @Autowired
      CatalogService catalogServie;

      @Autowired
      PromoService ps;

      Map<String, ShoppingCart> carts;

      Map<String, Product> productMap = new HashMap<>();

      @PostConstruct
      public void init() {
          LOG.info("Using local in-memory cache for cart data");
          carts = new HashMap<>();
      }

      // ... full implementation with HashMap<String, ShoppingCart> carts and
      // HashMap<String, Product> productMap for in-memory storage
  }
  ```

- `src/main/java/com/redhat/coolstore/service/PromoService.java` — Promotion engine (CDI + business logic)
  ```java
  package com.redhat.coolstore.service;

  import java.io.Serializable;
  import java.util.HashMap;
  import java.util.HashSet;
  import java.util.Map;
  import java.util.Set;

  import org.springframework.stereotype.Component;

  import com.redhat.coolstore.model.Promotion;
  import com.redhat.coolstore.model.ShoppingCart;
  import com.redhat.coolstore.model.ShoppingCartItem;

  @Component
  public class PromoService implements Serializable {

      private static final long serialVersionUID = 2088590587856645568L;

      private String name = null;

      private Set<Promotion> promotionSet = null;

      public PromoService() {
          promotionSet = new HashSet<Promotion>();
          // Coolstore seed item also used by inventory/catalog demos
          promotionSet.add(new Promotion("329299", .25));
      }

      public void applyCartItemPromotions(ShoppingCart shoppingCart) {
          if (shoppingCart != null && shoppingCart.getShoppingCartItemList().size() > 0) {
              Map<String, Promotion> promoMap = new HashMap<String, Promotion>();
              for (Promotion promo : getPromotions()) {
                  promoMap.put(promo.getItemId(), promo);
              }

              for (ShoppingCartItem sci : shoppingCart.getShoppingCartItemList()) {
                  String productId = sci.getProduct().getItemId();
                  Promotion promo = promoMap.get(productId);
                  if (promo != null) {
                      sci.setPromoSavings(sci.getProduct().getPrice() * promo.getPercentOff() * -1);
                      sci.setPrice(sci.getProduct().getPrice() * (1 - promo.getPercentOff()));
                  }
              }
          }
      }

      public void applyShippingPromotions(ShoppingCart shoppingCart) {
          if (shoppingCart != null) {
              // PROMO: if cart total is greater than 75, free shipping
              if (shoppingCart.getCartItemTotal() >= 75) {
                  shoppingCart.setShippingPromoSavings(shoppingCart.getShippingTotal() * -1);
                  shoppingCart.setShippingTotal(0);
              }
          }
      }

      // ... remaining methods
  }
  ```

- `src/main/java/com/redhat/coolstore/service/ShippingService.java` — Shipping calculator (CDI + tiered logic)
  ```java
  package com.redhat.coolstore.service;

  import org.springframework.stereotype.Component;

  import com.redhat.coolstore.model.ShoppingCart;

  @Component
  public class ShippingService {

      public void calculateShipping(ShoppingCart sc) {
          if (sc != null) {
              if (sc.getCartItemTotal() >= 0 && sc.getCartItemTotal() < 25) {
                  sc.setShippingTotal(2.99);
              } else if (sc.getCartItemTotal() >= 25 && sc.getCartItemTotal() < 50) {
                  sc.setShippingTotal(4.99);
              } else if (sc.getCartItemTotal() >= 50 && sc.getCartItemTotal() < 75) {
                  sc.setShippingTotal(6.99);
              } else if (sc.getCartItemTotal() >= 75 && sc.getCartItemTotal() < 100) {
                  sc.setShippingTotal(8.99);
              } else if (sc.getCartItemTotal() >= 100 && sc.getCartItemTotal() < 10000) {
                  sc.setShippingTotal(10.99);
              }
          }
      }
  }
  ```

## Out of scope

What neighboring code this story must NOT touch, and which story owns
it. (The tree must stay buildable: name any temporary seams — e.g. a
dependent class that keeps compiling against the old shape until its
own story.)

- REST endpoint (CartEndpoint) - owned by S04
- Service interfaces (already handled in S02)
- Domain models (already handled in S01)

## Class roles & target contract (from architecture-profile §7)

For each in-scope class, its role and — for REDESIGN classes — the target
contract carried forward from profile §7, so M3 writes tasks and tests to
the target (not the legacy):

- `com.redhat.coolstore.service.ShoppingCartServiceImpl` — REDESIGN
  - (REDESIGN only) target: `@ApplicationScoped` with CDI constructor injection (ShippingService, CatalogService, PromoService). Target: **normalize-before-derive** (dedupeCartItems() before pricing), no-clear-on-miss product cache with bounded refresh policy, thread-safe state using **ConcurrentHashMap** and compute() methods for atomic updates. **Run evidence**: M5 found 75% findings reduction - OpenRewrite + targeted infer tasks proven effective pattern.
- `com.redhat.coolstore.service.PromoService` — REDESIGN
  - (REDESIGN only) target: `@ApplicationScoped` with constructor injection. Business logic preserved: 25% off "329299", free shipping over $75.
- `com.redhat.coolstore.service.ShippingService` — REDESIGN
  - (REDESIGN only) target: `@ApplicationScoped` with constructor injection. Business logic preserved: tiered shipping costs (2.99/4.99/6.99/8.99/10.99).

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

**Dependency injection conversion (MAPPINGS spring-di→quarkus):**
- Apply Quarkus Spring DI conversion guidance for common Spring DI annotations
- Decided target: native CDI constructor injection (NOT the spring-di extension)
- All @Autowired → constructor injection

**JAX-RS dependency (MAPPINGS jakarta→quarkus):**
- Replace jakarta JAX-RS dependency
- Decided target: `quarkus-rest` dependency

**Story ordering:** core services before REST surface per SEQUENCING.md §4.

## Contracts owned by this story

- **Run evidence**: S01 demonstrated successful conversion of tightly coupled service layer with ConcurrentHashMap state management. The sensor-fix sessions validated that post-conversion tests catch integration issues early (2 sensor_red_post_commit events resolved without blocking the run).
- **Findings**: springboot-di-to-quarkus-00003 (implementation-level), jakarta-jaxrs-to-quarkus-00010
- **Preserve**: CATALOG_ENDPOINT configuration surface (used by CatalogService dependency)
- **Behavioral pins**: Service-level oracles from ShoppingCartServiceTest.java:
  - New cart initializes with all totals at 0.0 (cartItemPromoSavings, cartItemTotal, shippingPromoSavings, cartTotal)
  - Adding 2 units of $1000 product produces $2000 cartItemTotal, -10.99 shippingPromoSavings (free shipping promo), $2000 cartTotal
  - Product lookup returns exact product by ID with matching name/description/price
  - Cart `add()` oracle (additive): two `add(cartId, itemId, 2)` calls → quantity **4** after dedupe (not 2)
- **Forbidden**: getMockProducts, "Fallback to mock" - CatalogService calls must use real catalog endpoint

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- All three services use CDI constructor injection (no @Autowired)
- ShoppingCartServiceImpl thread-safe with ConcurrentHashMap (cart state + product cache)
- Pricing logic preserved: 25% off product "329299", tiered shipping, free shipping over $75
- Service-level tests pass (ShoppingCartServiceTest.java assertions)
- Cart `add()` behavior additive (2+2=4, not 2) confirmed by tests
- deploy story only: factory pipeline green, deployed, acceptance path
  serving (NOT APPLICABLE - deploy: false)
