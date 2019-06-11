## 简介
该样例工程演示了如何借助 `maven` 插件将一个普通的 Java 工程打包成标准格式规范的 `Ark Plugin` 

## 背景
现实开发中，常常会遇到依赖包冲突的情况；假设我们开发了一个类库 `sample-lib` , 业务应用在引入使用时，可能存在跟已有的依赖发生冲突的情况；通常这个时候，我们会希望自己的类库能够和业务其他依赖进行隔离，互不协商双方依赖包版本。 `Ark Plugin` 正是基于这种需求背景下的实践产物； `Ark Plugin` 运行在 `Ark Container` 之上，由容器负责加载启动，任何一个 `Ark Plugin` 由独立的 ClassLoader 加载，从而做到相互隔离。`Ark Plugin` 存在四个概念：
* 导入类：插件启动时，优先委托给导出该类的插件负责加载，如果加载不到，才会尝试从本插件内部加载；

* 导出类：其他插件如果导入了该类，优先从本插件加载；

* 导入资源：插件在查找资源时，优先委托给导出该资源的插件负责加载，如果加载不到，才会尝试从本插件内部加载；

* 导出资源：其他插件如果导入了该资源，优先从本插件加载；


**[详细请参考插件规范](https://alipay.github.io/sofastack.github.io/docs/ark-plugin.html#插件规范)**


## 工具
官方提供了 `Maven` 插件 - `sofa-ark-plugin-maven-plugin` ，只需要简单的配置项，即可将普通的 Java 工程打包成标准格式规范的 `Ark Plugin` ，插件坐标为:

```xml
<plugin>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>sofa-ark-plugin-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</plugin>
```

**[详细请参考插件配置文档](https://alipay.github.io/sofastack.github.io/docs/ark-plugin.html#完整配置模板)**

## 入门
基于该用例工程，我们一步步描述如何构建一个 `Ark Plugin` 

### 创建标准 Maven 工程
该用例工程是一个标准的 Maven 工程，一共包含两个模块：
* common 模块：包含了插件导出类

* plugin 模块：包含了 `com.alipay.sofa.ark.spi.service.PluginActivator` 接口实现类和一个插件服务类，插件打包工具 `sofa-ark-plugin-maven-plugin` 即配置在该模块的 `pom.xml` 中；

### 配置打包插件
在 plugin 模块的 `pom.xml` 中按如下配置打包插件：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.alipay.sofa</groupId>
            <artifactId>sofa-ark-plugin-maven-plugin</artifactId>
            <version>${project.version}</version>
            <executions>
                <execution>
                    <id>default-cli</id>
                    <goals>
                        <goal>ark-plugin</goal>
                    </goals>

                    <configuration>

                        <!--can only configure no more than one activator-->
                        <activator>com.alipay.sofa.ark.sample.activator.SamplePluginActivator</activator>

                        <!-- configure exported class -->
                        <exported>
                            <!-- configure package-level exported class-->
                            <packages>
                                <package>com.alipay.sofa.ark.sample.common</package>
                            </packages>

                            <!-- configure class-level exported class -->
                            <classes>
                                <class>com.alipay.sofa.ark.sample.facade.SamplePluginService</class>
                            </classes>
                        </exported>

                        <!--specify destination where ark-plugin will be saved, default saved to ${project.build.directory}-->
                        <outputDirectory>../target</outputDirectory>

                    </configuration>
                </execution>

            </executions>
        </plugin>
    </plugins>
</build>
```

在用例工程中，我们只配置了一部分配置项，这已经足够生成一个可用的 `Ark Plugin`，各配置项含义如下：
* activator: Ark 容器启动插件的入口类，最多只能配置一个；通常来说，在插件的 `activator` 会执行一些初始化操作，比如发布插件服务；在本样例工程中，即发布了插件服务。

* 导出包：包级别的导出类配置，插件中所有以导出包名为前缀的类，包括插件的三方依赖包，都会被导出；

* 导出类：精确类名的导出类配置，导出具体的类；

* outputDirectory： `mvn package` 打包后，输出的 ark plugin 文件存放目录；

需要指出的是，在用例工程中，我们只导出了工程创建的类；实际在使用时，也可以把工程依赖的三方包也导出去。

### 打包、安装、发布、引入
和普通的工程操作类似，使用 `mvn package` , `mvn install` , `mvn deploy` 即可完成插件包的安装和发布；需要注意的是，默认发布的 `Ark Plugin` 其 Maven 坐标会增加 `classifier=ark-plugin` ；例如在该样例工程中，如果需要使用该 ark plugin，必须如下配置依赖：

```xml
<dependency>
     <groupId>com.alipay.sofa</groupId>
     <artifactId>sample-ark-plugin</artifactId>
     <classifier>ark-plugin</classifier>
     <version>1.0.0-SNAPSHOT</version>
 </dependency>
```

