# Autonomous run report

## Executive summary

Autonomous migration of coolstore-cart-service-v6:
story gate passed (non-deploy story): pipeline + quality gate green. Findings delta and per-task detail: migration/run-log.md;
debt: migration/debt.md. Orchestrator custom:maas-m2/minimax-m2,
worker qwen27b/qwen3-6-27b, 13 model sessions.

- Outcome: story gate passed (non-deploy story): pipeline + quality gate green
- Supervisor version: 619bb869; run base: 72aedf8778db5334c0e65de296831eeb1c1863f0
- Orchestrator: custom:maas-m2/minimax-m2; worker: qwen27b/qwen3-6-27b

## Sessions

| session | seconds | rc |
|---|---|---|
| batch-T-001-T-002-T-003 | 623 | rc=0 |
| batch-T-004-T-005-T-006 | 243 | rc=0 |
| batch-T-007-T-008-T-009 | 69 | rc=0 |
| batch-verify-sfix | 736 | rc=0 |
| batch-T-010-T-011-T-012 | 466 | rc=0 |
| batch-T-013-T-014-T-015 | 227 | rc=0 |
| batch-verify-sfix | 440 | rc=0 |
| T-017-a1p0 | 534 | rc=0 |
| m5-evaluate-a1p0 | 144 | rc=0 |
| retro | 98 | rc=0 |
| batch-T-001-T-002-T-003 | 406 | rc=0 |
| T-004-a1p0 | 493 | rc=0 |
| m5-evaluate-a1p0 | 168 | rc=0 |

- Escalations (KPI, from supervisor events): 0 (untested: 0)

## Classified events

```
      5 already_complete
      4 success
      2 story_gate_pass
      2 sensor_red_post_commit
      2 pipeline_succeeded
      1 style_autofix
      1 later_story_class
```
