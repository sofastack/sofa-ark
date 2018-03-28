# SOFA Ark Project

[![Build Status](https://travis-ci.org/sofastack/sofa-ark.svg?branch=master)](https://travis-ci.org/sofastack/sofa-ark)
[![Coverage Status](https://coveralls.io/repos/github/sofastack/sofa-ark/badge.svg?branch=master)](https://coveralls.io/github/sofastack/sofa-ark?branch=master)
[![Gitter](https://img.shields.io/badge/chat-on%20gitter-orange.svg)](https://gitter.im/sofa-ark/Lobby)
![license](https://img.shields.io/badge/license-Apache--2.0-green.svg)
![maven](https://img.shields.io/badge/maven-v0.1.0-blue.svg)


SOFA Ark is a light-weight，java based classloader isolation framework 
open sourced by Ant Financial. Please visit [sofastack.github.io](https://sofastack.github.io/)
for quick start and detail information.

## Background

In Java world, dependency is always a problem, and can cause various errors, such as `LinkageError`, `NoSuchMethodError` etc. There are many ways to solve the dependency problems, the Spring Boot's way is using a dependency management to manage all the dependencies, make sure that all the dependencies in the dependency management will not conflict and can work pretty well. This is quite a simple and efficient way, it can cover most scenario, but there is some exceptions.

For example, there is a project that need protobuf version 2 and protobuf version 3, ant because protobuf version 3 is not compatible with version 2, so the project can not simply upgrade the protobuf to version 3 to solve the problem. There is same problem for hessian version 3 and version 4.

To cover the exceptions, we need to introduce a classloader-isolation way to the problem, make different version of a jar loaded by different classloader. There are many framework that can do classloader isolation, perhaps the most famous one is OSGi, but OSGi classloader schema is too complex, beside classloader-isolation, it is have ability to do hot deploy and a lot of other functionalities there we actually don't want.

So this is the origin of SOFA Ark, it wants to use a light-weight classloader isolation mechanism to  solve the problem that Spring Boot did not solve. And just a remind that SOFA Ark is not bind to Spring Boot, actually it is a more general classloader isolation framework that can be used with any other frameworks too.

## How SOFA Ark Works

TODO

## Sample

* [Sample projects](https://github.com/sofastack/sofa-ark/tree/master/sofa-ark-samples)
    * [Ark Plugin Based On Maven Project](https://github.com/sofastack/sofa-ark/tree/master/sofa-ark-samples/sample-ark-plugin) - Sample Project for Ark-Plugin
    * [Ark Based On Spring Boot](https://github.com/sofastack/sofa-ark/tree/master/sofa-ark-samples/sample-springboot-ark) Sample Project for Ark based on Spring Boot Project

## Community

* [Gitter channel](https://gitter.im/sofa-ark/Lobby) - Online chat room with SOFA Ark developers.
* [Issues](https://github.com/alipay/sofa-ark/issues)

## Contribution

* [Contributing](./CONTRIBUTING.md) : Guides for contributing to SOFA Ark.

## Documentation

* [SOFA Ark 用户手册(中文)](https://sofastack.github.io/docs/) : Describe how to used SOFA Ark and its features. 
