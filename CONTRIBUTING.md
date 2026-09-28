# Contributing to Steel Meridian

Steel Meridian is in early development. The architecture is still being established, so focused changes are much easier to review than large unsolicited feature drops.

Bug reports, design discussion, tests, documentation improvements, and targeted pull requests are welcome.

## Development Setup

On Linux:

```bash
./scripts/bootstrap.sh
./scripts/check.sh
```

The bootstrap script installs a project-local Java 21 runtime under `.tooling/`. The Gradle wrapper included in the repository is the authoritative Gradle version.

To launch a client:

```bash
./scripts/client.sh
```

To launch a server:

```bash
./scripts/server.sh
```

Before submitting a change:

```bash
./scripts/check.sh
```

## Before Implementing a Large Feature

Please open a design proposal first when a change:

- introduces a new core gameplay system
- changes established Factorio behavior
- affects save data or network protocol behavior
- adds a new compatibility layer
- changes simulation ownership, scheduling, or threading
- introduces a high-cardinality system such as belts, power networks, fluids, logistics, enemies, or trains

Small bug fixes and localized improvements do not need ceremony.

## Design Principles

**Factorio is the default behavioral reference.** Minecraft-specific changes should improve the adaptation rather than replace recognizable mechanics without a reason.

**Minecraft owns the physical world.** The mod should embrace three-dimensional factories, terrain, multiplayer, and Minecraft's broader ecosystem where those things genuinely improve the design.

**The server is authoritative.** Clients may predict or render state, but gameplay state belongs to the server.

**High-cardinality systems must be designed for scale.** No subsystem should assume its instances are rare.

**Avoid work before optimizing work.** Sleeping, event-driven scheduling, aggregation, topology caching, and coarse simulation should come before micro-optimizing unnecessary updates.

**Compatibility belongs at boundaries.** Steel Meridian's native systems should keep their semantics internally and expose adapters to NeoForge capabilities and other mods.

**Gameplay data should be data-driven where practical.**

Read [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) before changing core systems.

## Code Style

Keep related concepts together. Prefer a clear subsystem with a handful of meaningful types over dozens of tiny classes that scatter one idea across the project.

Comments should explain why, invariants, edge cases, or non-obvious constraints. Avoid comments that merely narrate the code.

Use descriptive commit messages, for example:

```text
Add initial simulation scheduler
Implement furnace process state
Fix stale transport topology rebuild
```

Conventional Commits are not required.

## Pull Requests

A pull request should explain:

- what changed
- why the change belongs in Steel Meridian
- how it was tested
- whether it changes saved data, networking, or compatibility behavior
- whether it introduces new performance-sensitive work
