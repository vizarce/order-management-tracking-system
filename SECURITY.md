# Security Policy

## Supported Versions

| Version | Supported |
|---|---|
| `1.x` (latest `main`) | ✅ Yes |
| Older branches | ❌ No |

Only the latest code on the `main` branch receives security fixes. Please upgrade to the latest version before reporting a vulnerability.

---

## Reporting a Vulnerability

**Do not open a public GitHub issue for security vulnerabilities.**

If you discover a security vulnerability, please report it privately so it can be addressed before public disclosure.

### How to report

1. Go to the repository's [Security tab](https://github.com/vizarce/order-management-tracking-system/security) on GitHub.
2. Click **"Report a vulnerability"** to open a private security advisory.
3. Fill in the details:
   - A clear description of the vulnerability.
   - Steps to reproduce or a proof-of-concept.
   - Potential impact and affected components.
   - Any suggested mitigation (optional).

Alternatively, you can contact the maintainers directly by emailing the address listed on the GitHub profile.

---

## Response Timeline

| Step | Target timeline |
|---|---|
| Initial acknowledgement | Within 3 business days |
| Triage and severity assessment | Within 7 business days |
| Fix delivered (critical/high) | Within 14 business days |
| Fix delivered (medium/low) | Within 30 business days |
| Public disclosure (coordinated) | After fix is released |

We will keep you informed throughout the process and coordinate a disclosure date with you.

---

## Scope

The following are in scope for this security policy:

- Source code in this repository (`common`, `order-service`, `tracking-service`).
- Configuration files and infrastructure-as-code (`docker-compose.yml`, CI workflows).
- Dependencies declared in `pom.xml` files.

The following are **out of scope**:

- Third-party dependencies beyond our control (report directly to their maintainers).
- Vulnerabilities in development/test environments that are not reproducible in production configuration.
- Social engineering or phishing attacks.

---

## Preferred Languages

We accept vulnerability reports in **English**.

---

## Known Security Practices

- **Dependency management:** dependency versions are pinned and reviewed. Logback is kept at 1.4.12+ (CVE-fix).
- **No secrets in source:** credentials are passed via environment variables; no secrets are committed to the repository.
- **Input validation:** all REST endpoints validate input using Jakarta Bean Validation.
- **No wildcard CORS:** CORS is not configured permissively by default.
- **MDC / logging:** sensitive fields (passwords, card numbers) are never logged.
