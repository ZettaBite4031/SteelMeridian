# Steel Meridian Architecture

> **Status:** Early architectural direction.
>
> This document records project-wide rules and intended system boundaries. It is not a frozen API contract.

Steel Meridian is a large-scale industrial overhaul for Minecraft inspired by Factorio. Its architecture separates **physical representation** from **simulation representation**.

A conveyor belt may be placed as individual Minecraft blocks because Minecraft is a block world. It does not follow that every belt block should own an independent simulation object that wakes twenty times per second.

That distinction is the spine of the project.

## Architectural Doctrine

**Factorio defines the industrial rules.** Where behavior is already well established by Factorio, that behavior is the default reference.

**Minecraft defines the physical world and player embodiment.** Factories exist inside a three-dimensional Minecraft world with terrain, chunks, players, inventories, dimensions, multiplayer, and other mods.

**Compatibility happens through adapters, not by weakening native systems.** Forge Energy, fluid handlers, item handlers, recipe viewers, and other mod APIs are integration boundaries.

**Minecraft-specific additions should improve the adaptation.** Vertical factories, dedicated lifts, terrain-following rail slopes, and similar ideas are welcome when they solve a genuine 3D-world problem.

## Server Authority

Gameplay state is server-authoritative.

The server owns machine state, inventories, transfers, transport, power, fluids, research, rail state, logistics, construction jobs, pollution, and enemy strategic state.

Clients receive enough state to render, animate, display interfaces, and provide responsive interaction.

## Simulation Is Not Block Ticking

Minecraft blocks and block entities are world integration and presentation objects. They are not automatically the fundamental simulation model.

High-cardinality systems should use mod-owned simulation structures.

```text
Physical belt blocks
        ↓
logical belt topology
        ↓
transport lines / lanes

Physical power poles
        ↓
electrical graph
        ↓
power network simulation

Machine structure blocks
        ↓
logical machine identity
        ↓
scheduled process state
```

No subsystem should assume its instances are rare.

## Scheduling and Sleeping

The preferred model is event-driven and scheduled.

Idle machines should leave the active scheduler where practical and wake on meaningful changes such as input arrival, output availability, power changes, recipe changes, topology changes, or scheduled completion.

Long-running operations should use completion times or scheduled work rather than incrementing progress every Minecraft tick unless continuous updates are genuinely needed.

Different systems do not need the same update frequency.

## Simulation Activity

World loading and simulation activity are related, but they should not be identical.

Useful conceptual levels are:

```text
FULL      Nearby / fully interactive simulation and presentation.
FACTORY   Important industrial simulation continues without unnecessary world work.
LAZY      State advances coarsely or from elapsed time.
DORMANT   Nothing runs until an event wakes the system.
```

The names are not a frozen API. The rule is to minimize forced chunk loading.

## Machines and Multiblocks

A machine has one authoritative logical identity.

Small machines may map naturally to one Minecraft block. Larger machines can use multiblock structures, but constituent blocks must not become independent simulations.

The logical machine owns process state, recipe, inventory semantics, power demand, fluids, modules, configuration, and scheduled completion.

## Recipes and Processing

Recipes describe transformations. Machines describe capabilities.

A process recipe can define item inputs, fluid inputs, outputs, base time, process category, handcraftability, and optional capability requirements.

Possible categories include:

```text
crafting
advanced_crafting
smelting
chemistry
oil_processing
centrifuging
rocket_building
```

Minecraft shaped crafting and Steel Meridian process crafting remain distinct concepts.

## Player Inventory and Hand Crafting

Do not mutate Minecraft's fundamental player inventory layout just to mimic Factorio.

The intended model is:

```text
vanilla hotbar + vanilla inventory
              +
Steel Meridian extended storage
              ↓
       unified inventory view
```

Factorio-style hand crafting is a timed personal crafting queue and can recursively craft eligible prerequisites. Minecraft grid crafting remains useful for instant, manual, shape-based recipes.

## Item Transport

Belt items are not Minecraft `ItemEntity` instances.

Physical belt blocks form logical transport topology. Consecutive compatible segments should be represented as larger structures such as transport lines or lanes.

Topology rebuilds happen when the world changes, not every simulation step.

Ordinary belts should not climb arbitrary vertical surfaces. Vertical movement should use dedicated infrastructure.

## Electricity

Steel Meridian uses a native Factorio-style electrical model.

Internally, power is represented using Factorio-like production and consumption semantics rather than Forge Energy.

Electrical networks should preserve generation, demand, satisfaction, brownouts, accumulators, priority, and connectivity.

Forge Energy belongs at explicit conversion or compatibility boundaries.

## Fluids

Physical pipes form logical fluid networks.

The implementation should prioritize predictable behavior, useful throughput constraints, efficient network updates, clear machine ports, and strong interoperability through NeoForge fluid capabilities.

Complexity must justify itself through gameplay.

## Railways and Trains

Rails form a graph.

Train pathfinding operates on meaningful rail nodes, junctions, blocks, stations, and signals rather than Minecraft blocks.

Connectivity includes elevation. Routing should use graph search over the rail network.

## Blueprints, Construction, and Logistics

Blueprints are first-class.

A blueprint eventually needs to serialize placement, orientation, recipes, filters, modules, circuit configuration, and multiblock structures.

Construction ghosts represent intended future placement.

Construction and logistics robots should use explicit job systems rather than generic Minecraft entity AI where possible.

## Pollution and Enemies

Vanilla hostile mobs retain normal Minecraft behavior.

Biters are a separate ecology and faction. They react to Steel Meridian pollution and evolution systems.

Large biter attacks should avoid one expensive vanilla pathfinder per creature when group routing and cheaper local steering can represent the same strategic behavior.

## Data-Driven Content

Recipes, machines, technologies, science packs, resources, fluids, items, enemies, and progression relationships should be data-driven where practical.

Java provides behavior and validated schema. Data describes content.

## Compatibility Boundaries

Steel Meridian owns its internal semantics and exposes adapters.

```text
Steel Meridian watts
      ↕
FE adapter
      ↕
Forge Energy

Steel Meridian inventory semantics
      ↕
item-handler adapter
      ↕
NeoForge / external mods

Steel Meridian fluid network
      ↕
fluid adapter
      ↕
NeoForge fluid capabilities

Steel Meridian process recipes
      ↕
viewer / machine adapters
      ↕
EMI, JEI, storage networks, automation mods
```

Compatibility code should live in explicit integration modules rather than being scattered through core systems.

## Threading and Determinism

The architecture should permit future parallelism, but initial implementations should remain serial until they are correct and measurable.

Minecraft world state is not casually mutated from worker threads.

A safe future model is:

```text
1. capture stable state
2. perform independent simulation work
3. produce intents or results
4. resolve conflicts deterministically
5. commit authoritative mutations on the server thread
```

Potential asynchronous workloads include independent belt networks, electrical solving, train routing, pollution, logistics planning, enemy strategic routing, and topology rebuild calculations.

Async results should carry version information so stale results can be rejected.

Determinism is more important than maximum thread utilization.

## Persistence

Persistent simulation data should use explicit identities and versioned formats.

Save data must not depend on transient Java object identity.

Important structures should reconstruct safely when chunks load in different orders and should support migration as formats evolve.

## Networking

Do not synchronize the entire simulation every tick.

Network messages should carry authoritative state changes, interaction requests, compact snapshots, and occasional corrections where prediction is used.

Rendering state and simulation state should remain separable.

## Performance Rules

Before implementing a system expected to exist in large quantities, ask:

1. What work is actually necessary?
2. Can idle instances sleep?
3. Can identical work be aggregated?
4. Can topology be cached?
5. Can updates be event-driven?
6. Can hot state be represented compactly?
7. Can expensive calculations run less frequently?
8. Can independent work eventually be parallelized safely?

Optimize the model before optimizing instructions.

## Testing

Core simulation should be testable without requiring a rendered Minecraft client where practical.

Useful tests include deterministic recipe completion, transfer ordering, topology construction, power accounting, serialization round trips, graph routing, stale async-result rejection, unload/reload reconstruction, and multiplayer validation.

NeoForge GameTests are appropriate when behavior genuinely depends on world placement.

## First Development Milestone

The first playable vertical slice is:

```text
resource patch
    ↓
miner
    ↓
belt
    ↓
inserter
    ↓
furnace
    ↓
assembler
    ↓
science
    ↓
lab
    ↓
research
```

Before expanding beyond it, establish:

1. a mod-owned simulation scheduler
2. data-driven process recipes
3. generic machine process state
4. semantic inventory endpoints
5. a belt transport prototype
6. authoritative server interactions
7. persistence sufficient for the prototype

Multithreading should be architecturally possible, but not enabled merely because it sounds impressive.

## Current Non-Goals

Steel Meridian is not currently trying to:

- make every Minecraft crafting recipe automatically assemblable
- convert its native power model into Forge Energy
- keep every factory chunk fully loaded
- simulate belt items as spawned entities
- give every machine an unconditional per-tick update
- reproduce Factorio's 2D limitations where Minecraft's third dimension offers a better adaptation
- force Nether or End progression into the base technology tree without a strong design reason
- solve every compatibility target before the native systems exist
