# Ark Gradle打包插件使用
`sofa-ark-gradle-plugin`模块是Ark打包工具的Gradle版本实现，和Maven打包工具`sofa-ark-maven-plugin`有同样的功能，用于打包ark包和biz包。
# 配置
`sofa-ark-gradle-plugin` 使用 arkConfig 来进行配置。

# 如何使用
1. 本地发布引用
2. 远程仓库引入（待申请）

参考`sofa-ark-plugin-gradle-plugin`的本地发布和引入。
使用Gradle刷新后，如果一切正常，会在IDEA右侧Gradle任务列表中出现arkJar，具体如下： Tasks > build > arkJar，点击arkJar执行，会在指定的outputDirectory中输出ark包和biz包。