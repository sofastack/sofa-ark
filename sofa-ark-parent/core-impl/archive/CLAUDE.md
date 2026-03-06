# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Overview

**Artifact ID**: `sofa-ark-archive`
**Package**: `com.alipay.sofa.ark.loader`

This module implements archive handling for SOFAArk - parsing and loading JAR files, directories, and embedded archives.

## Purpose

- Parse Ark package structure (Fat Jar)
- Load Biz and Plugin archives from JAR files or directories
- Handle executable Ark JAR format

## Key Classes

### Archive Implementations
- `ExecutableArkBizJar` - Main entry point for executable Ark JAR
- `JarBizArchive` - Business module from JAR file
- `JarPluginArchive` - Plugin from JAR file
- `JarContainerArchive` - Container archive from JAR
- `DirectoryBizArchive` - Business module from exploded directory
- `ExplodedBizArchive` - Exploded JAR directory archive
- `DirectoryContainerArchive` - Container from directory
- `EmbedClassPathArchive` - Embedded classpath archive for testing

### Archive Utilities
- `archive.JarFileArchive` - Spring Boot style JAR archive
- `archive.ExplodedArchive` - Exploded directory archive
- `jar.*` - JAR file handling utilities

## Archive Structure

The Ark Fat Jar structure follows Spring Boot executable JAR format:
```
ark-executable.jar
├── SOFA-ARK/
│   ├── biz/           # Business module JARs
│   └── plugin/        # Plugin JARs
├── SOFA-ARK-CONTAINER/  # Container JAR
└── BOOT-INF/          # Application classes
```

## Dependencies

- `sofa-ark-spi` - Archive interfaces
- `sofa-ark-common` - Utilities
- `spring-boot-loader` - Spring Boot loader for JAR handling

## Used By

- `sofa-ark-container` - Uses archives to load Biz and Plugins
- `ark-maven-plugin` - Creates the archive structure