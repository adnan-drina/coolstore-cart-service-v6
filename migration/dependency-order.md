# Legacy dependency analysis (scripted, M1)

- Classes: 12; intra-project reference edges: 19
- Edges from explicit imports AND same-package simple-name references (token scan; over-approximates on name collisions, which only tightens coupling groups).

## God nodes (highest fan-in — pin behavior with characterization tests BEFORE converting)

| class | fan-in | fan-out |
|---|---|---|
| com.redhat.coolstore.model.ShoppingCart | 5 | 1 |
| com.redhat.coolstore.model.Product | 4 | 0 |
| com.redhat.coolstore.model.ShoppingCartItem | 3 | 1 |
| com.redhat.coolstore.service.ShoppingCartService | 2 | 2 |
| com.redhat.coolstore.rest.CartEndpoint | 1 | 2 |

## Conversion order (dependencies first — the tree must compile at every commit)

1. com.redhat.coolstore.model.Product (src/main/java/com/redhat/coolstore/model/Product.java) — god-node: characterization tests first
2. com.redhat.coolstore.model.Promotion (src/main/java/com/redhat/coolstore/model/Promotion.java)
3. com.redhat.coolstore.CartServiceApplication (src/main/java/com/redhat/coolstore/CartServiceApplication.java)
4. com.redhat.coolstore.model.ShoppingCartItem (src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java) — god-node: characterization tests first
5. com.redhat.coolstore.service.CatalogService (src/main/java/com/redhat/coolstore/service/CatalogService.java)
6. com.redhat.coolstore.model.ShoppingCart (src/main/java/com/redhat/coolstore/model/ShoppingCart.java) — god-node: characterization tests first
7. com.redhat.coolstore.service.ShoppingCartService (src/main/java/com/redhat/coolstore/service/ShoppingCartService.java)
8. com.redhat.coolstore.service.PromoService (src/main/java/com/redhat/coolstore/service/PromoService.java)
9. com.redhat.coolstore.service.ShippingService (src/main/java/com/redhat/coolstore/service/ShippingService.java)
10. com.redhat.coolstore.rest.CartEndpoint (src/main/java/com/redhat/coolstore/rest/CartEndpoint.java)
11. com.redhat.coolstore.service.ShoppingCartServiceImpl (src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java)
12. com.redhat.coolstore.rest.JerseyConfig (src/main/java/com/redhat/coolstore/rest/JerseyConfig.java)
