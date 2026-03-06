# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Overview

**Artifact ID**: `sofa-ark-support-starter`
**Package**: `com.alipay.sofa.ark.support`

This module provides startup support and test integration for SOFAArk applications.

## Purpose

- Bootstrap Ark container in IDE or test environments
- Provide TestNG and JUnit runners for testing on Ark
- Support embedded Ark execution

## Key Classes

### Startup (`startup/`)
- `SofaArkBootstrap` - Bootstrap Ark container for testing/IDE development
- `EmbedSofaArkBootstrap` - Embedded mode bootstrap
- `EntryMethod` - Entry method execution handler

### Test Runners (`runner/`)
- `ArkJUnit4Runner` - JUnit 4 runner for tests on Ark
- `ArkJUnit4EmbedRunner` - JUnit 4 runner for embedded Ark
- `JUnitExecutionListener` - JUnit execution listener

### TestNG Support (`listener/`)
- `TestNGOnArk` - Annotation to run TestNG tests on Ark
- `TestNGOnArkEmbeded` - Annotation for embedded mode
- `ArkTestNGExecutionListener` - TestNG execution listener
- `ArkTestNGInvokedMethodListener` - TestNG method listener
- `ArkTestNGAlterSuiteListener` - TestNG suite listener

### Common Utilities (`common/`)
- `DelegateArkContainer` - Delegate to Ark container
- `DelegateToMasterBizClassLoaderHook` - ClassLoader delegation hook
- `AddBizInResourcesHook` - Hook to add biz from resources
- `MasterBizEnvironmentHolder` - Hold master biz environment

### Threading (`thread/`)
- `IsolatedThreadGroup` - Thread group for isolated execution
- `LaunchRunner` - Run launch in isolated thread

## Usage for Testing

### JUnit 4
```java
@RunWith(ArkJUnit4Runner.class)
public class MyTest {
    @Test
    public void test() { ... }
}
```

### TestNG
```java
@TestNGOnArk
public class MyTest {
    @Test
    public void test() { ... }
}
```

## Dependencies

- `sofa-ark-container` - Container implementation
- `sofa-ark-spi` - Service interfaces
- `junit` / `testng` - Test frameworks

## Used By

- Application test code
- IDE development mode