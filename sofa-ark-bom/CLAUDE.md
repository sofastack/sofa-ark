# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Overview

**Artifact ID**: `sofa-ark-bom`
**Packaging**: `pom`

This is the Bill of Materials (BOM) module for SOFAArk. It provides centralized dependency version management for the entire SOFAArk project.

## Purpose

- Centralized version management for all SOFAArk modules and third-party dependencies
- Ensures consistent dependency versions across all modules
- Simplifies dependency declarations in other modules

## Key Dependencies Managed

### SOFAArk Modules
- `sofa-ark-all`, `sofa-ark-spi`, `sofa-ark-api`, `sofa-ark-container`, `sofa-ark-archive`
- `sofa-ark-common`, `sofa-ark-exception`
- Maven plugins: `sofa-ark-maven-plugin`, `sofa-ark-plugin-maven-plugin`
- Spring Boot integration modules

### Third-Party Libraries
- Google Guice 6.0.0 (DI framework)
- Guava 33.0.0-jre
- ASM 9.4 (bytecode manipulation)
- Spring Boot 2.7.14
- Logback 1.2.13, SLF4J 1.7.32
- Maven Core/Plugin dependencies 3.8.1
- Netty 4.1.109.Final
- JGit 5.13.3

## Usage

Other SOFAArk modules inherit from this BOM:

```xml
<parent>
    <artifactId>sofa-ark-bom</artifactId>
    <groupId>com.alipay.sofa</groupId>
    <version>${sofa.ark.version}</version>
</parent>
```

## When to Modify

- Adding new third-party dependencies that are shared across modules
- Updating dependency versions
- Adding new SOFAArk modules to dependency management