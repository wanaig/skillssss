# REASONIX.md — Harness Engineering

## Stack
- **Language**: Markdown (prompt templates), Node.js (tooling only)
- **Framework**: Multi-agent prompt orchestration system — 34 sub-agents across 7 domains
- **Key dep**: markdownlint-cli2 (lint only)
- **Python**: ancillary one-off scripts (`fix_prompts.py`)
- **No runtime**: this repo is a prompt/template collection, not an executable application

## Layout
- `architecture/` — Phase 0: tech architecture design (6 agents)
- `backend/` — Phase 1: Spring Boot backend API (5 agents)
- `frontend/` — Phase 1: Vue 3 frontend (5 agents)
- `flutter/` — Phase 1: Flutter cross-platform (5 agents)
- `blockchain/` — Phase 1: FISCO BCOS smart contracts (5 agents)
- `fullstack/` — Phase 2: integration & contract alignment (5 agents)
- `deploy/` — Phase 3: production deployment (3 agents)
- `docs/` — architecture.md, design_principles.md, workflow.md
- `.github/workflows/validate.yml` — CI: structure checks, naming, markdown lint, broken links
- Each domain: `agents/` dir (flat `.md` files) + `main_agent_prompt*.md` orchestrator

## Commands
- **Install**: `npm ci`
- **Lint all markdown**: `npm run lint`
- **Lint one file**: `npx markdownlint-cli2 path/to/file.md`
- **CI structure check**:
  ```bash
  for dir in architecture backend frontend fullstack flutter deploy blockchain; do
    [ -d "$dir" ] || { echo "Missing directory: $dir"; exit 1; }
  done
  ```
- **No build, no test suite** — validation is prompt structure checks + markdown lint

## Conventions
- Agent files MUST start with `# Skill: <underscore_name>` (CI enforces this)
- Required sections in every agent: `## When to Use This Skill`, `## Core Workflow` or `## Core Content`
- Naming is underscore-based: `be_planner`, `dg_vue_planner`, `fa_techstack`, etc.
- Phase pipeline: `architecture/` → (`frontend/` + `backend/` + `flutter/` + `blockchain/`) → `fullstack/` → `deploy/`
- File-based state is canonical: `main-log.md`, `dev-plan.md`, `agent-registry/*.json`, `test-report.json`, `lessons-learned.md`
- `BATCH_SIZE` defaults to 1; dev and test batch sizes must match within each domain loop
- `.github/copilot-instructions.md` is the authoritative reference for tool conventions in this repo

## Watch out for
- Do NOT edit agent prompt structure carelessly — CI validates `# Skill:` header + required sections on every PR
- `fix_prompts.py` is a one-off migration script, not part of the regular workflow
- Markdown lint uses a permissive config (`.markdownlint-cli2.jsonc` disables most rules); CI runs an incremental gate that only fails on NEW lint issues
- This repo has NO application code — it's exclusively prompt templates and design docs
- Internal markdown links are validated in CI; use relative paths correctly
- Every domain's `agents/` dir is a flat list of `.md` files with no subdirectories
