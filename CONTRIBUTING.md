# Contributing to Order Management & Tracking System

Thank you for your interest in contributing! This document explains how to get started, how the project is organized, and what is expected from contributors.

---

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Commit Messages](#commit-messages)
- [Pull Request Guidelines](#pull-request-guidelines)
- [Reporting Bugs](#reporting-bugs)
- [Suggesting Features](#suggesting-features)

---

## Code of Conduct

By participating in this project you agree to abide by the [Contributor Covenant Code of Conduct](https://www.contributor-covenant.org/version/2/1/code_of_conduct/). Be respectful, inclusive, and collaborative.

---

## Getting Started

1. **Fork** the repository on GitHub.
2. **Clone** your fork locally:
   ```bash
   git clone https://github.com/<your-username>/order-management-tracking-system.git
   cd order-management-tracking-system
   ```
3. Set the upstream remote:
   ```bash
   git remote add upstream https://github.com/vizarce/order-management-tracking-system.git
   ```
4. Install prerequisites — see the [Prerequisites section in the README](README.md#prerequisites).
5. Start the infrastructure and verify everything works:
   ```bash
   docker compose up -d
   ./mvnw verify
   ```

---

## Development Workflow

1. **Sync with upstream** before starting new work:
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```
2. **Create a feature branch** from `main`:
   ```bash
   git checkout -b feature/my-improvement
   # or for bug fixes:
   git checkout -b fix/issue-123-short-description
   ```
3. Make your changes in small, logical commits.
4. **Run tests** continuously during development:
   ```bash
   ./mvnw verify -pl order-service        # for order-service changes
   ./mvnw verify -pl tracking-service     # for tracking-service changes
   ./mvnw verify                          # full suite
   ```
5. Push your branch and open a **Pull Request** against `main`.

---

## Coding Standards

- **Language:** Java 17 — use records, sealed classes, switch expressions, and text blocks where appropriate.
- **Architecture:** follow the existing Clean Architecture / hexagonal layering per module:
  - `domain` — pure business logic, no framework dependencies.
  - `application` — use cases, application services, DTOs.
  - `infrastructure` — adapters (JPA, Kafka, Redis, Feign).
  - `web` — controllers and exception handlers.
- **Reactive:** tracking-service uses Project Reactor (`Mono`/`Flux`); do not introduce blocking calls.
- **Style:** match the style of the file you are editing. No tabs — use 4-space indentation.
- **Imports:** no wildcard imports. Organize imports (static last).
- **Null safety:** prefer `Optional` over `null` returns in domain and application layers.
- **Logging:** use SLF4J + MDC keys defined in `MdcConstants`. Do not log sensitive data.

---

## Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/):

```
<type>(<scope>): <short description>

[optional body]

[optional footer: Closes #123]
```

| Type | When to use |
|---|---|
| `feat` | New feature |
| `fix` | Bug fix |
| `refactor` | Code change that is neither a fix nor a feature |
| `test` | Adding or fixing tests |
| `docs` | Documentation only |
| `chore` | Build, CI, dependency updates |
| `security` | Security-related fix |

**Examples:**
```
feat(order-service): add product stock validation on order creation

fix(tracking-service): restore MDC context after Kafka consumer processing

docs: add distributed tracing section to README
```

---

## Pull Request Guidelines

- **Title:** follow the same Conventional Commits format.
- **Description:** explain *what* changed, *why*, and *how to test* it.
- **Scope:** keep PRs focused — one logical change per PR.
- **Tests:** every new feature or bug fix must include corresponding tests.
- **CI:** all CI checks must pass before the PR can be merged.
- **Review:** at least one approving review is required from a code owner.

### PR Checklist

Before requesting a review, make sure:

- [ ] `./mvnw verify` passes locally.
- [ ] New public classes/methods have Javadoc where non-obvious.
- [ ] No secrets or credentials are committed.
- [ ] The PR title/description is clear and complete.
- [ ] Related GitHub issue is linked (if applicable): `Closes #<issue>`.

---

## Reporting Bugs

1. Search [existing issues](https://github.com/vizarce/order-management-tracking-system/issues) to avoid duplicates.
2. Open a new issue and include:
   - Steps to reproduce.
   - Expected vs actual behaviour.
   - Environment (OS, Java version, Docker version).
   - Relevant log output.

---

## Suggesting Features

Open a [GitHub Discussion](https://github.com/vizarce/order-management-tracking-system/discussions) or a GitHub Issue labelled `feature`. Describe the problem you are solving and a proposed solution. Large features should be discussed before implementation work begins.
