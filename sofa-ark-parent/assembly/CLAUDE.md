# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Overview

**Artifact ID**: `sofa-ark-all`
**Package**: Assembly module (no Java code)

This module aggregates all core SOFAArk modules into a single JAR that becomes the Ark Container classpath.

## Purpose

- Create the aggregated `sofa-ark-all.jar`
- This JAR is packaged into every Ark executable JAR
- Contains all core runtime dependencies

## Included Modules

The assembly includes:
- `sofa-ark-common` - Utilities
- `sofa-ark-exception` - Exceptions
- `sofa-ark-spi` - Service interfaces
- `sofa-ark-api` - Public API
- `sofa-ark-archive` - Archive handling
- `sofa-ark-container` - Core container implementation

## Build Configuration

Uses `maven-assembly-plugin` with:
- Assembly descriptor: `src/main/assembly/assembly.xml`
- Creates single JAR without classifier
- Adds manifest entries: `ArkVersion`, `Timestamp`

## Usage in Ark Packages

When `sofa-ark-maven-plugin` builds an Ark package:
1. Resolves `sofa-ark-all` artifact
2. Packages it into `SOFA-ARK-CONTAINER/` directory
3. This becomes the container classpath at runtime

## Dependencies

All core modules are dependencies (see above).