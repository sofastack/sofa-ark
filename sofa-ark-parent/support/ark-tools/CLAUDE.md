# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Overview

**Artifact ID**: `sofa-ark-tools`
**Package**: `com.alipay.sofa.ark.tools`

This module provides the core repackaging utilities for creating Ark Fat JARs. It is used by both `ark-maven-plugin` and `ark-plugin-maven-plugin`.

## Purpose

- Repackage standard JARs into Ark Fat JAR format
- Layout management for different archive types
- Main class discovery
- Git information embedding

## Key Classes

### `Repackager`
Core class that transforms a regular JAR into an Ark package:
- `repackage(File target, File module, Libraries libraries)` - Perform repackaging
- `setBizName(String)` / `setBizVersion(String)` - Set module identity
- `setMainClass(String)` - Set entry point class
- `setDenyImportPackages/classes/Resources` - Configure classloader filtering
- `setDeclaredMode(boolean)` - Enable declared dependency mode

### Layouts (`Layouts.java`)
Define JAR structure for different archive types:
- `ARK_EXECUTABLE` - Executable Ark JAR structure
- `ARK_BIZ` - Business module JAR structure
- `ARK_PLUGIN` - Plugin JAR structure

### `JarWriter`
Lower-level JAR writing utility:
- Write entries to JAR
- Copy libraries
- Write manifest

### `MainClassFinder`
Scan class files to find main method entry point.

### `git.JGitParser`
Parse Git repository information to embed in JAR:
- Commit hash, branch, author
- Build time information

### `ArtifactItem`
Represents a Maven artifact with groupId, artifactId, version, classifier.

## Dependencies

- `sofa-ark-common` - Utilities
- `spring-boot-loader` - JAR layout from Spring Boot
- `org.eclipse.jgit` - Git information parsing

## Used By

- `ark-maven-plugin` - Uses Repackager for business modules
- `ark-plugin-maven-plugin` - Uses Repackager for plugins