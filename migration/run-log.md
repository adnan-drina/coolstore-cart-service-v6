# Harness run log

Appended by the Hermes orchestrator after every task (see
`.hermes/skills/migration-harness/`). One line per task.

| Task | Class | Attempts | Result | Files |
|---|---|---|---|---|
| T-001 | rewrite | 1 | complete | pom.xml |
| T-002 | rewrite | 1 | complete (already satisfied) | (no changes needed) |
| T-003 | rewrite | 1 | complete (already satisfied) | (no changes needed) |
| T-004 | rewrite | 1 | complete (already satisfied) | (no changes needed) |
| T-005 | rewrite | 1 | complete (already satisfied) | (no changes needed) |
| T-006 | rewrite | 1 | complete | src/main/java/com/demo/model/Product.java |
| T-007 | rewrite | 1 | complete | src/main/java/com/demo/model/Promotion.java |
| T-008 | rewrite | 1 | complete | src/main/java/com/demo/model/ShoppingCartItem.java |
| T-009 | rewrite | 1 | complete | src/main/java/com/demo/model/ShoppingCart.java |
| T-010 | rewrite | 1 | complete (already satisfied) | (Quarkus BOM provides removed JavaEE modules) |
| T-011 | rewrite | 1 | complete | src/main/resources/application.properties |
| T-012 | rewrite | 1 | complete (already satisfied) | (quarkus-junit5 dependency already present in pom.xml) |
| T-013 | rewrite | 1 | complete | migration.yaml (CDI conversion strategy documented) |
| T-014 | rewrite | 1 | complete | pom.xml (quarkus-smallrye-metrics added), migration.yaml (metrics strategy) |
| T-015 | rewrite | 1 | complete | migration.yaml (JAX-RS conversion strategy documented) |
