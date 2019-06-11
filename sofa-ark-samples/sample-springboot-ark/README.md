## 简介
该样例工程演示了如何借助 `Maven` 插件将一个 Spring Boot Web 工程打包成标准格式规范的可执行 Ark 包；

## 工具
官方提供了 `Maven` 插件 - `sofa-ark-maven-plugin` ，只需要简单的配置项，即可将 Spring Boot Web 工程打包成标准格式规范的可执行 Ark 包，插件坐标为：

```xml
<plugin>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>sofa-ark-maven-plugin</artifactId>
    <version>${sofa.ark.version}</version>
</plugin>
```

**[详细请参考插件使用文档](https://alipay.github.io/sofastack.github.io/docs/ark-jar.html)**


## 入门
基于该样例工程，我们一步步描述如何将一个 Spring Boot Web 工程打包成可运行 Ark 包

### 创建 SpringBoot Web 工程
在官网 [https://start.spring.io/](https://start.spring.io/) 下载一个标准的 Spring Boot Web 工程

### 配置打包插件
在工程主 `pom.xml` 中如下配置 `Maven` 插件 `sofa-ark-maven-plugin` :

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.alipay.sofa</groupId>
            <artifactId>sofa-ark-maven-plugin</artifactId>
            <version>${sofa.ark.version}</version>
            <executions>
                <execution>
                    <id>default-cli</id>
                    
                    <!--goal executed to generate executable-ark-jar -->
                    <goals>
                        <goal>repackage</goal>
                    </goals>
                    
                    <configuration>
                        <!--specify destination where executable-ark-jar will be saved, default saved to ${project.build.directory}-->
                        <outputDirectory>./target</outputDirectory>
                        
                        <!--default none-->
                        <arkClassifier>executable-ark</arkClassifier>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

在该样例工程中，我们只配置了一部分配置项，这已经足够生成一个可用的可执行 `Ark` 包，各配置项含义如下：
* outputDirectory: `mvn package` 打包后，输出的 `Ark` 包文件存放目录；

* arkClassifier: 指定发布的 `Ark` 包其 `Maven` 坐标包含的 `classifier` 值，默认为空；

**关于 arkClassifier 配置项需要特别注意下，默认值为空；如果不指定 classifier ，上传到仓库的 Jar 包其实是一个可运行的 Ark 包；如果需要和普通的打包加以区分，需要配置该项值。**

### 打包、安装、发布
和普通的工程操作类似，使用 `mvn package` , `mvn install` , `mvn deploy` 即可完成插件包的安装和发布；以 `mvn package` 命令为例，可以看到有三个文件生成：
+ sofa-ark-sample-springboot-ark-1.0.0-SNAPSHOT.jar
> 因为设置了 arkClassifier, 因此这个文件不是 Ark 包，而是普通的模块 Jar 包

+ sofa-ark-sample-springboot-ark-1.0.0-SNAPSHOT-ark-biz.jar
> 因为没有设置 bizClassifier, 因此使用默认的 ark-biz 当成 bizClassifier，这个包就是 Biz 包。值得一提的是，因为我们没有配置 attach = true, 所以只是生成了 Biz 包，但是并不会安装到本地仓库或者发布到远程仓库。

+ sofa-ark-sample-springboot-ark-1.0.0-SNAPSHOT-executable-ark.jar
> 因为设置了 arkClassifier=executable-ark，可以看到该包就是插件 sofa-ark-maven-plugin 生成的可执行 Ark 包，可以通过 java -jar 启动。

### 运行
为了方便在 Spring Boot/SOFABoot 工程中方便的集成 SOFAArk, 只需要在应用中引入如下依赖即可：

```xml
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>sofa-ark-springboot-starter</artifactId>
    <version>${sofa.ark.version}</version>
</dependency>
```

值得一提的是，对于普通的 Java 应用，不仅需要添加如下依赖：

```xml
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>sofa-ark-support-starter</artifactId>
    <version>${sofa.ark.version}</version>
</dependency>
```

还需要在工程 `main` 方法最开始处，执行容器启动，如下：

```java
public class Application{
    
    public static void main(String[] args) { 
        SofaArkBootstrap.launch(args);
        ...
    }
    
}
```

### 运行测试用例
SOFAArk 提供了 `org.junit.runner.Runner` 的两个实现类，`ArkJUnit4Runner` 和 `ArkBootRunner`，分别用于集成 JUnit4 测试框架和 Spring Test；对于 TestNG 测试框架，提供了注解 `@TestNGOnArk`，对于任何 TestNG 测试用例，只有打有 `@TestNGOnArk` 的测试用例才会跑在 Ark Container 之上，否则普通用例一样。

#### ArkJUnit4Runner
`ArkJUnit4Runner` 类似 `JUnit4`，使用注解 `ArkJUnit4Runner`，即可在 SOFAArk 容器之上运行普通的 JUnit4 测试用例；示范代码如下：

```java
@RunWith(ArkJUnit4Runner.class)
public class UnitTest {

    @Test
    public void test() {
        Assert.assertTrue(true);
    }

}
```

`ArkJUnit4Runner` 和 `JUnit4` 使用基本完全一致，`JUnit4` 测试框架的其他特性都能够完全兼容，

#### ArkBootRunner
`ArkBootRunner` 类似 `SpringRunner`，参考[文档](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html)学习 `SpringRunner` 的用法；为了能够在 SOFAArk 容器之上运行 Spring Boot 测试用例，只需要简单使用 `@RunWith(ArkBootRunner.class)` 替代 `@RunWith(SpringRunner.class)` 即可；示范代码如下：

```java
@RunWith(ArkBootRunner.class)
@SpringBootTest(classes = SpringbootDemoApplication.class)
public class IntegrationTest {

    @Autowired
    private SampleService sampleService;

    @Test
    public void test() {
        Assert.assertTrue("A Sample Service".equals(sampleService.service()));
    }

}
```

`ArkBootRunner` 和 `SpringRunner` 使用基本完全一致；

#### TestNGOnArk
注解 `@TestNGOnArk` 是 SOFAArk 提供给开发者用于标记哪些 TestNG 用例跑在 SOFAArk 之上，哪些只是普通的运行。例如：

```java
@TestNGOnArk
public class TestNGTest {

    public static final String TEST_CLASSLOADER = "com.alipay.sofa.ark.container.test.TestClassLoader";

    @Test
    public void test() {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        ClassLoader loader = this.getClass().getClassLoader();
        Assert.assertTrue(tccl.equals(loader));
        Assert.assertTrue(tccl.getClass().getCanonicalName().equals(TEST_CLASSLOADER));
    }

}
```

上述用例打了 `@TestNGTest`，因此在执行该测试用例时，会先启动 Ark Container。
