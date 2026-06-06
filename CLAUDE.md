# matilda-java-plugin

The **"java" plugin** for [Matilda](https://github.com/nadavgu/matilda) — the one reached
as `process.plugins.java` in the core's README. It provides JVM/Android reflection over
Matilda's RPC: locating classes, invoking methods/constructors, reading and writing
fields, and creating dynamic proxy objects in the remote JVM.

## Relationship to the template

This repo is a **fork of [matilda-plugin-template](https://github.com/nadavgu/matilda-plugin-template)**,
rebased on its **`kotlin` branch** (see the `template` git remote). That branch is the
Kotlin + KSP + **Dagger**, JVM + Android flavor — which is why this plugin's agent is
Kotlin with a `plugin` + `plugin-android` module split and uses Dagger for DI.

To pull in template changes: `git fetch template && git rebase template/kotlin`. The
template's own `CLAUDE.md` documents the branch stack (`dev → kotlin → native`) and the
general plugin-authoring/codegen/RPC conventions — this file only covers what's specific
to the java plugin.

## Layout

- `java/` — the Python package (pip name `matilda-java-plugin`, import package `java`,
  registered under the `matilda.plugins` entry point as `java = 'java.java_plugin'`).
  - `java_plugin.py` — `PLUGIN_ENTRY_POINTS` (JVM + ANDROID), `load_plugin`, and the
    `JavaPlugin` API (`find_class`, `new_proxy_instance`).
  - `java_class.py`, `java_method.py`, `java_field.py`, `java_constructor.py`,
    `java_object.py`, `java_primitive_type.py`, `java_value.py`, `java_type.py` — the
    user-facing reflection wrappers.
  - `proxy_handler*.py` — support for `new_proxy_instance` callbacks (a Python handler
    invoked on each method call of the proxy).
  - `generated/`, `protos/*_pb2.py`, `resources/` — **build output, gitignored. Do not
    hand-edit.**
- `agent-plugin/` — the Gradle agent build.
  - `plugin/` — JVM plugin: `org.matilda.java.JavaPlugin` (the `createCommandRegistry`
    entry point) and `services/reflection/` (`ReflectionService`, `ObjectRepository`,
    proxy handling, etc.).
  - `plugin-android/` — the Android build of the same plugin (packaged as an `.apk`).

## How it plugs in

The agent's `@MatildaService` (`ReflectionService`) exposes `@MatildaCommand`s; KSP
generates the matching Python service into `java/generated/`. `java_plugin.py` declares
`PLUGIN_ENTRY_POINTS` per platform and a `load_plugin(DependencyContainer)` that returns
the `JavaPlugin` object exported as `process.plugins.java`. DI on the agent side is
**Dagger** (`Dagger…Component`); on the Python side it is **maddie**.

**Regenerate after changing any service, command, or `.proto`** — `java/generated/` and
`protos/*_pb2.py` are produced by the build, not committed.

## Build / test

- Requires the **core installed first**: `cd ../matilda && ./setup.sh install` (publishes
  `org.matilda:*` to mavenLocal — this plugin's Gradle build consumes
  `org.matilda:commands-generator*` from there).
- `./setup.sh install` — `gradlew assemble` then `pip install .`.
- `./setup.sh clean` — Gradle clean + wipe generated code, `*_pb2.py`, build output, and
  non-committed resources.
- Agent only: `cd agent-plugin && ./gradlew assemble`.

To verify a change, build then run the end-to-end tests: `./setup.sh install`, then
`pytest` from the repo root. The tests load the plugin into a real agent process and
exercise the reflection API over the RPC.

Working branch is `dev`. Don't commit generated code or `resources/` binaries — both
gitignored.
