# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Overview

**Artifact ID**: `sofa-ark-compatible-springboot1`
**Package**: `com.alipay.sofa.ark.springboot`

This module provides Spring Boot 1.x compatibility for SOFAArk.

## Purpose

- Enable Ark features in Spring Boot 1.x applications
- Version-specific adapter implementations

## Notes

- Spring Boot 1.x is end-of-life
- This module provides backward compatibility for legacy applications
- For new projects, use Spring Boot 2.x or later

## Dependencies

- `sofa-ark-common-springboot` - Common integration code
- Spring Boot 1.x (provided scope)

## Used By

- `ark-springboot-starter` - Conditionally loaded for Spring Boot 1.x apps