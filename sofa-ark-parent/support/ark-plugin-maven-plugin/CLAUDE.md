# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Overview

**Artifact ID**: `sofa-ark-plugin-maven-plugin`
**Packaging**: `maven-plugin`
**Goal**: `ark-plugin`

This Maven plugin packages multiple JARs into a single Ark Plugin. Plugins provide class-isolated shared capabilities that can be used by business modules.

## Purpose

- Create Ark Plugin archives from Maven dependencies
- Configure plugin class import/export settings
- Set plugin priority and activator

## Key Classes

### `ArkPluginMojo`
Main Maven Mojo bound to `package` phase:
- Aggregates multiple JARs into a single plugin
- Configures classloader isolation settings

Key configuration parameters:
- `activator` - Plugin activator class (implements `PluginActivator`)
- `excludeGroupIds` / `excludeArtifactIds` - Dependencies to exclude from plugin
- `exportPackages` - Packages to export for other plugins/biz to use
- `exportClasses` - Specific classes to export
- `importPackages` - Packages to import from other plugins
- `importClasses` - Specific classes to import
- `exportResources` - Resources to export
- `importResources` - Resources to import
- `priority` - Plugin startup priority (higher starts earlier)

### `ImportConfig`
Configuration for imported packages/classes/resources.

### `ExportConfig`
Configuration for exported packages/classes/resources.

### `LinkedProperties` / `LinkedManifest` / `LinkedAttributes`
Utility classes for maintaining order in manifest entries.

## Plugin Structure

```
plugin.jar
├── com/alipay/sofa/ark/plugin/  # Plugin metadata
│   └── export.index             # Export index
├── lib/                         # Bundled JARs
└── META-INF/MANIFEST.MF         # Plugin metadata
```

## Usage

```xml
<plugin>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>sofa-ark-plugin-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>ark-plugin</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <activator>com.example.MyPluginActivator</activator>
        <exportPackages>com.example.api</exportPackages>
        <priority>1000</priority>
    </configuration>
</plugin>
```

## Dependencies

- Maven Core/Plugin APIs
- `sofa-ark-common` - Utilities
- `sofa-ark-tools` - Repackaging utilities