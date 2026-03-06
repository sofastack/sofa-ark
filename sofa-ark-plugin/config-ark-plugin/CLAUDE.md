# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Overview

**Artifact ID**: `config-ark-plugin`
**Package**: `com.alipay.sofa.ark.config`

This is a built-in Ark Plugin that provides dynamic configuration management for business modules using ZooKeeper or Apollo.

## Purpose

- Receive dynamic configuration from configuration centers
- Control dynamic Biz install/uninstall based on configuration changes
- Support ZooKeeper and Apollo as configuration sources

## Key Classes

### `ConfigBaseActivator`
Plugin activator that initializes configuration listeners.

### Configuration Listeners
- Listen for configuration changes from ZK or Apollo
- Parse configuration to determine Biz operations
- Trigger install/uninstall/switch operations

## Configuration Format

The plugin expects configuration in this format:
```yaml
bizs:
  - name: my-biz
    version: 1.0.0
    url: http://example.com/my-biz-1.0.0.jar
    operation: INSTALL
```

## Supported Operations

- `INSTALL` - Install a new Biz
- `UNINSTALL` - Uninstall a Biz
- `SWITCH` - Switch active Biz version

## Usage

Add plugin dependency:
```xml
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>config-ark-plugin</artifactId>
    <classifier>ark-plugin</classifier>
</dependency>
```

Configure in `conf/ark/bootstrap.yml`:
```yaml
ark:
  config:
    zk:
      server: localhost:2181
      path: /sofa-ark/config
```

## Dependencies

- `sofa-ark-spi` - Service interfaces
- `sofa-ark-api` - API for Biz operations
- `curator-recipes` - ZooKeeper client
- `apollo-client` - Apollo client (provided scope)

## Used By

- Applications requiring dynamic Biz management via configuration center