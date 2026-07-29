# S04: JAX-RS endpoint modernization

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

What this story achieves and why it is next: its place in the roadmap,
what it unblocks, which stories it depends on (cite
dependency-order.md / architecture-profile.md).

This story modernizes the final REST surface layer, converting the Spring MVC JAX-RS endpoint to native JAX-RS with Quarkus. It is the culmination of the dependency order (dependency-order.md line 11) and enables the first deploy milestone. The endpoint depends on ShoppingCartService which is finalized in S03, making this the logical final step. This story implements the target contract from architecture-profile §7: GET-idempotent (404, never creates), validation + error mapping enabled. **Run evidence**: S01 achieved 75% findings reduction and green pipeline - demonstrating the complete M1-M5 workflow viability.

## In scope

The exact legacy classes/files this story modernizes. For each, quote
the load-bearing legacy code (the lines being transformed — imports,
annotations, key methods), so the story never starts from a blank
read:

- `src/main/java/com/redhat/coolstore/rest/CartEndpoint.java` — REST endpoint (Spring MVC → JAX-RS)
  ```java
  package com.redhat.coolstore.rest;

  import java.io.Serializable;

  import javax.ws.rs.DELETE;
  import javax.ws.rs.GET;
  import javax.ws.rs.POST;
  import javax.ws.rs.Path;
  import javax.ws.rs.PathParam;
  import javax.ws.rs.Produces;
  import javax.ws.rs.core.MediaType;

  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.context.annotation.Scope;
  import org.springframework.web.bind.annotation.RestController;
  import org.springframework.web.context.WebApplicationContext;

  import com.redhat.coolstore.model.ShoppingCart;
  import com.redhat.coolstore.service.ShoppingCartService;

  @RestController
  @Scope(scopeName = WebApplicationContext.SCOPE_SESSION)
  @Path("/cart")
  public class CartEndpoint implements Serializable {

      private static final long serialVersionUID = -7227732980791688773L;

      @Autowired
      private ShoppingCartService shoppingCartService;

      @GET
      @Path("/{cartId}")
      @Produces(MediaType.APPLICATION_JSON)
      public ShoppingCart getCart(@PathParam("cartId") String cartId) {
          return shoppingCartService.getShoppingCart(cartId);
      }

      @POST
      @Path("/{cartId}/{itemId}/{quantity}")
      @Produces(MediaType.APPLICATION_JSON)
      public ShoppingCart add(@PathParam("cartId") String cartId,
                              @PathParam("itemId") String itemId,
                              @PathParam("quantity") int quantity) throws Exception {
          return shoppingCartService.addItem(cartId, itemId, quantity);
      }

      @POST
      @Path("/{cartId}/{tmpId}")
      @Produces(MediaType.APPLICATION_JSON)
      public ShoppingCart set(@PathParam("cartId") String cartId,
                              @PathParam("tmpId") String tmpId) throws Exception {
          return shoppingCartService.set(cartId, tmpId);
      }

      @DELETE
      @Path("/{cartId}/{itemId}/{quantity}")
      @Produces(MediaType.APPLICATION_JSON)
      public ShoppingCart delete(@PathParam("cartId") String cartId,
                                 @PathParam("itemId") String itemId,
                                 @PathParam("quantity") int quantity) throws Exception {
          return shoppingCartService.deleteItem(cartId, itemId, quantity);
      }

      @POST
      @Path("/checkout/{cartId}")
      @Produces(MediaType.APPLICATION_JSON)
      public ShoppingCart checkout(@PathParam("cartId") String cartId) {
          return shoppingCartService.checkout(cartId);
      }
  }
  ```

## Out of scope

What neighboring code this story must NOT touch, and which story owns
it. (The tree must stay buildable: name any temporary seams — e.g. a
dependent class that keeps compiling against the old shape until its
own story.)

- Service implementations (already handled in S03)
- Domain models (already handled in S01)
- All foundation work (already handled in S01-S03)

## Class roles & target contract (from architecture-profile §7)

For each in-scope class, its role and — for REDESIGN classes — the target
contract carried forward from profile §7, so M3 writes tasks and tests to
the target (not the legacy):

- `com.redhat.coolstore.rest.CartEndpoint` — REDESIGN
  - (REDESIGN only) target: `@ApplicationScoped @Path` JAX-RS resource with constructor injection. Target contract: GET returns **404** on missing cart (never creates), POST endpoints **400** on invalid input, **503** via JAX-RS ExceptionMapper on catalog failures. GET is idempotent (read-only), validation enabled, error mapping enabled.

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

**JAX-RS conversion (MAPPINGS jakarta→quarkus):**
- Replace jakarta JAX-RS dependency
- Decided target: `quarkus-rest` dependency

**Dependency injection conversion (MAPPINGS spring-di→quarkus):**
- Apply Quarkus Spring DI conversion guidance for common Spring DI annotations
- Decided target: native CDI constructor injection (NOT the spring-di extension)

**Metrics conversion (MAPPINGS spring-metrics→quarkus):**
- Replace the Micrometer code with Microprofile Metrics code
- Decided target: metrics call sites → MP Metrics annotations (design per site)

**Story ordering:** REST surface last per SEQUENCING.md §4.

## Contracts owned by this story

- **Findings**: jakarta-jaxrs-to-quarkus-00010, springboot-di-to-quarkus-00003, springboot-metrics-to-quarkus-0200
- **Preserve**: CATALOG_ENDPOINT configuration surface (through service layer)
- **Behavioral pins**: Endpoint-level oracles from CartServiceBoundaryTest.java:
  - HTTP POST `/api/cart/1/1111/2` returns ShoppingCart with same totals as service-level test
  - Matches service test expectations — 2000.0 cartItemTotal, -10.99 shippingPromoSavings
  - GET `/cart/{cartId}` returns 404 on missing cart (legacy creates on get, target changes to 404)
  - POST endpoints return 400 on invalid input (legacy behavior may differ)
- **Forbidden**: getMockProducts, "Fallback to mock" - real catalog service integration

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- Endpoint uses JAX-RS (not Spring MVC) with constructor injection
- GET returns 404 on missing cart (never creates on get)
- POST endpoints validate input and return 400 on invalid data
- Service-level and integration tests pass (CartServiceBoundaryTest.java assertions)
- `/api/cart/*` endpoints serve correctly with Quarkus REST
- Full cart lifecycle works: create→add→price→delete→checkout
- **deploy**: true - factory pipeline green, deployed, acceptance path serving
