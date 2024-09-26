# Ark Plugin Gradle打包插件使用
`sofa-ark-plugin-gradle-plugin`模块是Ark Plugin打包工具的Gradle版本实现，和Maven打包工具`sofa-ark-plugin-maven-plugin`有同样的功能。在后文，使用**Gradle插件**来指代`sofa-ark-plugin-gradle-plugin`。

本小节会对**Gradle插件**进行介绍，随后会展示如何使用**Gradle插件**打包Ark Plugin。

### 配置项
**Gradle插件**提供了和Maven基本相同的配置项，在使用上略有不同，想要使用配置项，需要在打包的项目build.gradle使用arkPlugin：

```
arkPlugin{
    //具体配置项
}
```

- activator使用
```
activator = 'sample.activator.SamplePluginActivator'
```

- excludes使用
```
excludes = ['com.fasterxml.jackson.module:jackson-module-parameter-names', 'org.example:common']
```

- exported使用
```
exported {
    packages = [
            'com.alipay.sofa.ark.sample.common'

    ]
    classes = [
            'sample.facade.SamplePluginService'
    ]

    resource = [
        'META-INF/spring/bean.xml'
    ]

}
```


### 引入
引入方式可以分为两种：
1. 本地引入
2. 远程引入（正在申请）

本地引入的方式是将Gradle插件发布到本地的Maven仓库中，之后使用Gradle进行加载。

#### 将Gradle插件发布到本地仓库
1. 在**Gradle插件**的build.gradle的plugin解开注释，如下所示：
```
plugins {
    id 'java'
    id 'java-gradle-plugin'

//    本地调试用，发布到maven
    id 'maven-publish'
}
```

2. 配置publish
   在build.gradle中增加如下内容：
```
publishing {
    // 配置Plugin GAV
    publications {
        maven(MavenPublication) {
            groupId = group
            artifactId = 'plugin'
            version = version
            from components.java
        }
    }
    // 配置仓库地址
    repositories {
        maven {
            url file('E:/repo')
        }

    }
}
```
点击在IDEA的右侧Gradle中的 Tasks > publishing > publish 将插件发布到本地仓库。

#### 在本地项目中引入

1. 在Gradle项目根目录的setting.gradle中设置pluginManagement

```
 pluginManagement {
    repositories {
        // 指定maven仓库
        maven {
            url "file:///E:/repo"
        }
    }
}
```

2. 在需要打包的项目中的build.gradle进行如下的配置

```
buildscript {
    repositories {
        // ...
        maven {
            url "file:///E:/repo"
        }

    }
    dependencies {
        classpath("sofa.ark.gradle:sofa-ark-gradle-plugin:1.1")
    }
}

plugins {
    id 'sofa.ark.gradle.plugin' version "1.1"
}
```

3. 增加配置

在需要打包的项目中的build.gradle创建配置项：

```
arkPlugin{
    outputDirectory = layout.buildDirectory.dir("custom-output")

    activator = 'sample.activator.SamplePluginActivator'
    excludes = ['com.fasterxml.jackson.module:jackson-module-parameter-names']

    shades = [
            'org.example:common:1.0'
    ]
    exported {
        packages = [
                'com.alipay.sofa.ark.sample.common'

        ]
        classes = [
                'sample.facade.SamplePluginService'
        ]

    }
}

```

使用Gradle刷新后，如果一切正常，会在IDEA右侧Gradle任务列表中出现arkPluginJar，具体如下： Tasks > build > arkPluginJar，点击arkPluginJa执行，会在指定的outputDirectory中输出Ark Plugin包。