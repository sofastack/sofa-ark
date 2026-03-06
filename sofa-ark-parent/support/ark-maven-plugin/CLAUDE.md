# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Overview

**Artifact ID**: `sofa-ark-maven-plugin`
**Packaging**: `maven-plugin`
**Goal**: `repackage`

This Maven plugin packages applications into Ark executable JARs and Biz modules.

## Purpose

- Transform a Spring Boot or plain Java application into an Ark executable JAR
- Create Biz module JARs for dynamic deployment
- Configure dependency filtering and classloader isolation

## Key Classes

### `RepackageMojo`
Main Maven Mojo bound to `package` phase:
- Input: Compiled application JAR
- Output: `*-ark-executable.jar` (Fat Jar) and/or `*-ark-biz.jar` (Biz module)

Key configuration parameters:
- `bizName` / `bizVersion` - Module identity
- `priority` - Startup priority (default: 100)
- `mainClass` - Entry point class
- `excludes` / `excludeGroupIds` / `excludeArtifactIds` - Dependencies to exclude
- `includes` / `includeGroupIds` / `includeArtifactIds` - Dependencies to include
- `denyImportPackages/classes/Resources` - Classloader filtering
- `skipArkExecutable` - Skip creating Fat Jar
- `keepArkBizJar` - Keep Biz JAR after packaging
- `declaredMode` - Enable declared dependency mode
- `webContextPath` - Web context path for web apps

### `ModuleSlimExecutor`
Handles dependency slimming - removing unnecessary dependencies from the package.

### `ModuleSlimConfig`
Configuration for dependency slimming:
- `packExcludesConfig` - Exclude rules file
- `baseDependencyParentIdentity` - Base dependency filtering

### `ArtifactsLibraries`
Handles library resolution and unpacking.

### `model.ArkConfigHolder`
Loads Ark configuration from `conf/ark/bootstrap.yml` or `bootstrap.properties`.

## Usage

```xml
<plugin>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>sofa-ark-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>repackage</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <bizName>my-app</bizName>
        <bizVersion>1.0.0</bizVersion>
    </configuration>
</plugin>
```

## Dependencies

- `sofa-ark-common` - Utilities
- `sofa-ark-tools` - Repackaging logic
- Maven Core/Plugin APIs