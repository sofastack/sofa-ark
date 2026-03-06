# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Overview

**Artifact ID**: `netty-ark-plugin`
**Package**: `com.alipay.sofa.ark.netty`

This is a built-in Ark Plugin that provides Netty-based telnet server support for runtime management.

## Purpose

- Enable remote telnet access to Ark container
- Provide command-line interface for runtime operations
- Support management commands: biz, plugin, info queries

## Key Features

### Telnet Server
- Netty-based telnet server implementation
- Listen on configurable port
- Command parsing and execution

### Supported Commands
- `biz` - Manage business modules (install, uninstall, switch, check)
- `plugin` - Manage plugins (check, install)
- `info` - Query container and module information
- `help` - Show available commands

## Usage

Add plugin dependency:
```xml
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>netty-ark-plugin</artifactId>
    <classifier>ark-plugin</classifier>
</dependency>
```

Connect via telnet:
```bash
telnet localhost 1234
```

## Dependencies

- `sofa-ark-spi` - Service interfaces
- `sofa-ark-api` - API for operations
- `netty-all` - Netty networking framework

## Used By

- Applications requiring remote management capabilities
- Operations teams for runtime monitoring