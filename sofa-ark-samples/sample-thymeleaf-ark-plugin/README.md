## 简介
该样例工程演示了如何把 Spring Boot 官方 `spring-boot-starter-thymeleaf` 改造成 `Ark Plugin`，对应版本为 `1.4.2.RELEASE`; 

## 背景
Spring Boot 官方 `spring-boot-starter-thymeleaf` 是 Spring Boot 提供的 Web 模板解析器；基于 Spring Boot 进行项目开发时，可能会遇到 Spring Boot 依赖包和项目其他三方包冲突的情况，这个时候可以考虑把 Spring Boot 依赖做成 `Ark Plugin`; 以 `spring-boot-starter-thymeleaf` 为例，该工程将演示将一个 Spring Boot Starter 改造 `Ark Plugin` 的大致步骤以及相关注意点；

## 步骤
#### 1、分析 `spring-boot-starter-thymeleaf` 依赖
查看依赖关系，可以看到 `spring-boot-starter-thymeleaf` 主要会引入如下五个依赖包:

+ `spring-boot-starter`

+ `spring-boot-starter-web`

+ `thymeleaf-spring4`

+ `thymeleaf-layout-dialect`

+ `thymeleaf`

虽然我们可以把 Spring 所有相关依赖打成 Ark Plugin，通常情况下我们不推荐这么做，当然开发者可以按照实际情况而定；在这里，我们假设 Spring 相关依赖没有打成 Ark Plugin; 因此依赖 `spring-boot-starter` 和`spring-boot-starter-web` 也不适合放在改造后的 Ark Plugin 设置为导出类；这里其实隐含了一个理由，因为 `Ark Plugin` 只能加载其他 `Ark Plugin` 的导出类，加载不到 Ark Biz 包含的任何类；因此如果只把这一部分 Spring 相关类放在 Ark Plugin 中导出，容易出现 `ClassNotFound`; 

依赖包 `thymeleaf-spring4` 负责桥接 Spring 和 `thymeleaf`，需要依赖 Spring，因为 Spring 已经交给 Ark Biz 加载，也即意味着依赖 Ark Biz 加载的类，因此这个包也需要放在 Ark Biz 中加载，不会被打入到 Ark Plugin;

作为底层负责模板解析的依赖包 `thymeleaf-layout-dialect` 和 `thymeleaf` 及其依赖的三方包，可以一起打包在 Ark Plugin 中，并设置为导出类；

### 2、新建 ark plugin 打包工程
分析完 `spring-boot-starter-thymeleaf` 依赖包，大概也就弄清楚了如何配置 `sofa-ark-plugin-maven-plugin` 了；新建 Ark Plugin 打包工程，在工程主 `pom.xml` 中添加 Starter 依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```
版本号在父 `pom.xml` 中定义，为 1.4.2.RELEASE。然后如下配置 Ark Plugin 打包插件配置：

```xml
<configuration>

    <!-- configure exported class -->
    <exported>
        <!-- configure package-level exported class-->
        <packages>
            <!-- thymeleaf-layout-dialect -->
            <package>nz.net.ultraq.thymeleaf.*</package>
            <!-- thymeleaf -->
            <package>org.thymeleaf.*</package>
            <!-- javax.servlet.ServletRequest -->
            <package>javax.*</package>
        </packages>
    </exported>

    <excludeGroupIds>
        <excludeGroupId>org.springframework</excludeGroupId>
        <excludeGroupId>org.springframework.boot</excludeGroupId>
    </excludeGroupIds>

    <excludes>
        <exclude>org.thymeleaf:thymeleaf-spring4</exclude>
    </excludes>

    <!--specify destination where ark-plugin will be saved, default saved to ${project.build.directory}-->
    <outputDirectory>./target</outputDirectory>

</configuration>
```

在配置导出类时，`<package>nz.net.ultraq.thymeleaf.*</package>` 负责把包 `thymeleaf-layout-dialect` 的所有类导出；`<package>org.thymeleaf.*</package>` 负责把包 `thymeleaf` 的所有类导出；值得注意的是 `thymeleaf` 依赖一系列 `javax.*` 的类，如 `javax.servlet.http.HttpServletRequest`，这些类包含在 tomcat 等包中；而对于 Spring Boot Web 应用，他们也需要依赖 tomcat 中的这些类；为了避免这些类在 Ark Plugin 和 Ark Biz 各自被加载，导致在类转换的时候发生 `LinkageError`, 因此在该演示工程中暴力地把 `javax.*` 配置成导出类；其实更好的做法是把 tomcat 单独打包成一个 Ark Plugin，然后导出相关类；

在打包 Ark Plugin 时，`sofa-ark-plugin-maven-plugin` 默认会把工程所有 `scop` 为 `compile`, `runtime` 的依赖打入到 Ark Plugin 中；根据之前分析，Spring 相关依赖会放在 Ark Biz 中加载，所以部分依赖包没有必要打入到 Ark Plugin 中，导致插件包体量过大，因此使用如下配置排除了 Spring 相关包：

```xml
<excludeGroupIds>
    <excludeGroupId>org.springframework</excludeGroupId>
    <excludeGroupId>org.springframework.boot</excludeGroupId>
</excludeGroupIds>

<excludes>
    <exclude>org.thymeleaf:thymeleaf-spring4</exclude>
</excludes>
```

以上，`spring-boot-starter-thymeleaf` 则被打包成了 Ark Plugin；基于 1.4.2.RELEASE 版 Spring Boot, 用户添加如下依赖：

```xml
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>thymeleaf-ark-plugin</artifactId>
    <classifier>ark-plugin</classifier>
    <version>0.3.0-SNAPSHOT</version>
</dependency>
```

即可使用 Ark Plugin 版的 `spring-boot-starter-thymeleaf`