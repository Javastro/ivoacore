# AGENTS.md

## Scope
- This guide is for `ivoacore` (multi-module Java library) and its immediate integration with sibling `tap-server`.
- No existing AI-agent rule files were found in this repo via the requested glob; external conventions were sourced from `tap-server/README.md`.

## Repo Map (What Lives Where)
- `common/`: shared utilities used by protocol modules.
- `dal/`: DALI/VOSI abstractions (`org.javastro.ivoacore.vosi.*`), including reusable `BaseVOSIResource`.
- `uws/`: async job framework (`JobManager`, `BaseUWSJob`, job store, execution policy, JAX-RS base resources).
- `tap/`: TAP-on-UWS layer (`TAPJob`, `TAPJobSpecification`, schema providers).
- `pgsphere/`: Hibernate dialect/types for PostgreSQL PgSphere (`PgSphereDialect`); versioned independently (`0.9-SNAPSHOT`).
- `clients/*`: protocol client libraries (registry active, tap/vospace in progress).

## Big-Picture Architecture
- Core design is protocol logic first, web framework second: reusable interfaces/base resources in `ivoacore`, framework wiring in host apps.
- UWS execution flow: `JobManager.createJob(...)` -> `JobStore.store(...)` -> `runJob/setPhase("RUN")` -> `BaseUWSJob.performAction()` async via executor -> `results` exposed through UWS endpoints.
- TAP async endpoint pattern (in `tap-server`) composes core pieces: `AsyncQueryResource` creates `TAPJobSpecification`, delegates to `JobManager`, returns `303 See Other` to `/async/{id}`.
- VOSI pattern: service-specific resource extends `BaseVOSIResource` and injects `VOSIProvider` (see `tap-server/src/main/java/org/javastro/ivoa/tap/VOSIResource.java`).
- Current maturity is mixed: several `Not yet implemented`/`FIXME` paths are intentional and discoverable (e.g., sync TAP query, some UWS control endpoints).

## Build/Test/Docs Workflows
- Use Java 17 everywhere (CI and toolchains enforce this).
- Full build (matches CI): `./gradlew build`
- Fast module iteration: `./gradlew :uws:test` or `./gradlew :tap:test`
- Docs build: `pip install -r doc/requirements.txt` then `./gradlew docs` (runs aggregated Javadoc + Sphinx).
- Aggregated API docs only: `./gradlew aggregateJavadoc`
- Publishing expects `UKSRC_REPO_USERNAME` and `UKSRC_REPO_PASSWORD` env vars (`./gradlew publish`).

## Project-Specific Conventions
- Apply shared Gradle convention plugin (`id("ivoacore-conventions")`) in each module.
- Default group/version come from `buildSrc` (`org.javastro.ivoa.core`, snapshot-driven publishing/signing behavior).
- Tests use JUnit 5; `integrationTest` suite is preconfigured by conventions plugin.
- Prefer extension by composition/interfaces: provide concrete `*Provider` or `JobFactory` implementations instead of embedding protocol logic in endpoints.
- Keep JAX-RS resources thin; delegate protocol behaviour to `JobManager`, `SchemaProvider`, `VOSIProvider`.

## Integration Points (Sibling `tap-server`)
- `tap-server/settings.gradle.kts` uses `includeBuild("../ivoacore")` for local composite builds while APIs are in flux.
- `tap-server` depends on published coordinates (`org.javastro.ivoa.core:tap|dal|pgsphere`) and wires runtime beans in `TapConfiguration`.
- Runtime config coupling exists in `tap-server/src/main/resources/application.properties` (e.g., `ivoa.tap.schema`, PgSphere dialect, datasource/devservices).
- When changing `uws`/`tap` interfaces, verify both `ivoacore` tests and `tap-server` startup paths still compile.

## Safe Change Heuristics for Agents
- Treat TODO/FIXME-rich areas as evolving contracts; avoid "completing" behaviour unless requested.
- For API-shape changes, update both core module usage and Quarkus adapters in `tap-server` in the same change set.
