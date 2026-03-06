# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Overview

**Artifact ID**: `sofa-ark-springboot-starter`
**Package**: `com.alipay.sofa.ark.springboot`

This is the main Spring Boot Starter for SOFAArk. It provides auto-configuration and integration with Spring Boot applications.

## Purpose

- Auto-configure SOFAArk in Spring Boot applications
- Provide Spring Boot style integration
- Enable Ark features with minimal configuration

## Key Features

### Auto-Configuration
- Automatically activates when SOFAArk container is running
- Conditionally loads version-specific compatibility modules
- Exposes Ark services as Spring beans

### Integration Points
- `ApplicationContext` integration with Ark Biz
- Spring Environment bridging
- Event system integration

## Usage

Add dependency:
```xml
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>sofa-ark-springboot-starter</artifactId>
</dependency>
```

## Dependencies

- `sofa-ark-common-springboot` - Common integration
- `sofa-ark-compatible-springboot1` - Spring Boot 1.x support (optional)
- `sofa-ark-compatible-springboot2` - Spring Boot 2.x support (optional)
- `sofa-ark-support-starter` - Startup support

## Used By

- Spring Boot applications running on SOFAArk