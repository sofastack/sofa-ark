# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Overview

**Artifact ID**: `web-ark-plugin`
**Package**: `com.alipay.sofa.ark.web`

This is a built-in Ark Plugin that provides embedded web server support for business modules.

## Purpose

- Enable multiple web applications in different Biz modules
- Provide embedded Tomcat server support
- Handle web context isolation between Biz modules

## Key Features

### Embedded Server
- Starts embedded Tomcat server
- Each Biz can have its own web context path
- Supports Spring Boot web applications

### Web Context Isolation
- Different Biz modules can serve at different context paths
- Static resource isolation
- Session isolation support

## Usage

Add plugin dependency:
```xml
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>web-ark-plugin</artifactId>
    <classifier>ark-plugin</classifier>
</dependency>
```

Configure web context in Maven plugin:
```xml
<configuration>
    <webContextPath>/my-app</webContextPath>
</configuration>
```

## Dependencies

- `sofa-ark-spi` - Service interfaces
- `sofa-ark-api` - API for operations
- Tomcat embedded (provided scope)
- Spring Boot (provided scope)

## Used By

- Web applications running on SOFAArk
- Multiple web applications merged into single deployment