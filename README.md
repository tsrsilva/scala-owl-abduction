# Scala OWL Abductive Reasoner (Skeleton)

A learning-oriented Scala project skeleton for single-cell abductive completion in phenotype supermatrices.

## Goal

For one missing matrix cell `(taxon, character)`, the system:

1. Generates finite candidate states for that character.
2. Filters candidates using OWL consistency checks.
3. Scores candidates with a simple hand-designed function.
4. Returns a ranked list with lightweight explanations.

## Project Layout

- `model`: core domain types and result objects
- `abduction`: candidate generation, filtering, scoring, and orchestration
- `scoring`: pluggable feature + weighted linear scorer
- `reasoning`: reasoner abstraction and an in-memory mock implementation
- `app`: runnable demo

## Quick Start

```bash
cd ~PATH/scala-owl-abduction
sbt run
```

This runs a toy example from `Main` and prints ranked candidate states.

## Done

- Replace `InMemoryReasoningPort` with a real OWL API backed adapter used by the main app path.
- Keep ELK integration tests as default, and run HermiT guard tests only when needed.
- Add CSV/TSV matrix readers and batch completion.
- Add masked-cell evaluation (top-1 and top-k) for quality tracking.
