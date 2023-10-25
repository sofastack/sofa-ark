
# Apollo 配置
在介绍 [Biz 生命周期](https://www.sofastack.tech/projects/sofa-boot/sofa-ark-ark-config/) 时，我们提到了有三种方式控制 Biz 的生命周期，并且介绍了使用客户端 API 实现 Biz 的安装、卸载、激活。在这一章节我们介绍如何使用 SOFAArk 提供的动态配置插件，通过 Apollo 下发指令，控制 Biz 的生命周期。

另外，在[通过Apollo管理SOFAArk模块安装示例](DEMO_SHOW.md) 中演示了如何使用Apollo配置中心来动态管理ark模块的配置。

### 引入依赖
SOFAArk 提供了 config-ark-plugin 对接 Apollo 配置中心，用于运行时接受配置，达到控制 Biz 生命周期，引入如下依赖：

```xml
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>config-ark-plugin</artifactId>
    <version>${sofa.ark.version}</version>
</dependency>

<dependency>
    <groupId>com.ctrip.framework.apollo</groupId>
    <artifactId>apollo-client</artifactId>
    <version>2.1.0</version>
</dependency>
```

### 配置
参考 [SOFAArk 配置](https://www.sofastack.tech/projects/sofa-boot/sofa-ark-ark-config/)，在 SOFAArk 配置文件 `conf/ark/bootstrap.properties` 增加如下配置：

```text
sofa.ark.config.server.type=apollo
```
如果没有配置，默认使用Zookeeper作为配置中心，当然也可以通过设置sofa.ark.config.server.type=zookeeper来显式指定配置中心类型为Zookeeper

### Apollo配置维度
+ 需要额外在Apollo后台为应用独立创建一个管理SOFAArk的Namespace，Namespace的名称为sofa-ark
+ 创建key-value来管理 SOFAArk 的模块加载， key的名字必须为masterBiz， 类型为String


### 配置value的值
下面介绍配置的形式，动态配置采用状态声明指令，SOFAArk 收到配置后，会根据状态描述解析出具体的指令（包括 install，uninstall, switch），指令格式如下：

`bizName:bizVersion:bizState?k1=v1&k2=v2`

多条指令使用 `;` 隔开，单条指令主要由 biz 名称，biz 版本，biz 预期状态及参数组成。简单记住一点，状态配置是描述指令推送之后，所有非宿主 Biz 的状态；

例如当前 SOFAArk 容器部署了两个应用 A，B，版本均为 1.0，其中 A 应用为宿主应用，因为宿主应用不可卸载，因此不需要考虑宿主应用，可以简单认为当前容器的 Biz 状态声明为：

> `B:1.0:Activated`

如果此时你希望安装 C 应用，版本为 1.0，文件流地址为 urlC，那么推送指令应为：

> `B:1.0:Activated;C:1.0:Activated?bizUrl=urlC`

操作继续，如果你又希望安装安装 B 应用，版本为 2.0，文件流地址为 urlB，且希望 2.0 版本处于激活状态，那么你推送的指令应为：

> `B:1.0:Deactivated;B:2.0:Actaivated?bizUrl=urlB;C:1.0:Activated`

> 解释下为什么是这样配置指令，因为 SOFAArk 只允许应用一个版本处于激活状态，如果存在其他版本，则应处于非激活状态；所以当希望激活 B 应用 2.0 版本时，B 应用 1.0 版本应该声明为非激活状态。另外你可能注意到了 C 应用参数 urlC 不用声明了，原因是目前只有当安装新 Biz 时，才有可能需要配置参数 bizUrl，用于指定 biz 文件流地址，其他场景下，参数的解析没有意义。

操作继续，如果你希望卸载 B 应用 2.0 版本，激活 B 应用 1.0 版本，卸载 C 应用，那么推送的指令声明为：

> `B:1.0:Activated`


从上面的操作描述看，在推送动态配置时，只需要声明期望的 Biz 状态即可，SOFAArk 会根据状态声明推断具体的执行指令，并尽可能保持服务的连续性，以上面最后一步操作为例，SOFAArk 推断的执行指令顺序如下：
+ 执行 switch 指令，激活 B 应用 1.0 版本，钝化 B 应用 2.0 版本，保证服务连续性
+ 执行 uninstall 指令，卸载 B 应用 2.0 版本
+ 执行 uninstall 指令，卸载 C 应用 1.0 版本


