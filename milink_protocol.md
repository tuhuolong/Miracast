# MiLink Protocol 协议定义

## 自动发现

自动发现采用mdns，属性值如下：
* Name: 设备名称，由APP指定。
* Type：_milink._tcp
* Port: 服务端监听的消息端口，此端口可以随机。
* 其他属性值
    * version: 版本号。
    * deviceid: 设备ID，UUID字符串，可以随机，保证设备的唯一性。
    * features: 支持哪些特性。

### features

值              | 含义
----------------|----------------------
0x0001          | 支持Miracast镜像功能



## 消息通道

Client -> Server (TCP)

手机端作为Tcp Client端，盒子端作为Tcp Server，每一个请求，都必须有一个应答，消息格式如下： 

### 请求

    <iq type="set" id="0001">
        <query xmlns="http://www.xiaomi.com/milink/miracast" action="start">
            <param>
                xxxxxxx
            </param>
        </query>
    </iq>

### 应答

    <iq type="result" id="0001">
        <query xmlns="http://www.xiaomi.com/milink/miracast" action="start">
            <param>
                xxxxxxx
            </param>
        </query>
    </iq>

### 发布事件

盒子端作为Server，不定期发布自己的事件，消息格式如下： 

    <iq type="event" id="0001">
        <query xmlns="http://www.xiaomi.com/milink/miracast" event="loading">
            <param/>
        </query>
    </iq>
    
### 字段解释

* type

IQ 的类型，分为请求和应答。

type         | 含义
-------------|----------------------
set          | 设置数据（请求，需要应答）
get          | 获取数据（请求，需要应答）
result       | 结果（应答）
error        | 失败（应答）
event        | 事件消息（由服务端发往客户端，不需要应答）

* id

请求的标识符，一个请求，一个标识符，应答中的标识符，和对应的请求标识符一样。

* xmlns

名字空间

* action

行为，不同的名字空间，可以定义不同的操作行为。

* param

param标签内包含字符串，具体参数信息，由action决定。

## 定义

* 名字空间（可扩展）

xmlns                                  | 含义
---------------------------------------|--------------------------------------
http://www.xiaomi.com/milink/miracast  | 这是一个miracast名字空间

### xmlns="http://www.xiaomi.com/milink/miracast"

* 行为

action                                 | 含义
---------------------------------------|--------------------------------------
start                                  | 启动
stop                                   | 停止

参数：action="start"

    <root>
        <ip>10.0.1.1</ip>
        <port>5678</port>
    </root>

参数: action="stop"

    <root/>

* 事件

event                                  | 含义
---------------------------------------|--------------------------------------
loading                                | 正在加载
playing                                | 正在播放
stopped                                | 已经停止

参数: event="loading"

    <root/>

参数: event="playing"

    <root/>

参数: event="stopped"

    <root/>
