
<a name="paste-site-api"/>

# 粘贴站点API

<a name="endpoints"/>

## Endpoints
* [下载](#download)
* [连接](#connect)
* [认证](#authenticate)
* [列表](#list)
* [上传](#upload)
* [删除](#delete)
* [更新](#update)
* [浏览器登录](#browser-login)
* [应用程序登录](#app-login)
* [应用程序检查](#app-check)
* [应用程序令牌请求](#app-token-request)
* [错误信息](#error-messages)

接口地址：`https://paste.tom5454.com/`  
Java中的完整实现：[CPM中的粘贴客户端](https://github.com/tom5454/CustomPlayerModels/blob/master/CustomPlayerModels/src/shared/java/com/tom/cpm/shared/paste/PasteClient.java)


<a name="download"/>

### 下载
接口路径：`raw/<粘贴ID>`


<a name="connect"/>

### 连接
接口路径：`api/connect?name=<玩家名称>`  
响应格式：（JSON）
* `id`：会话 ID，后续请求中需要使用  
* `key`：Minecraft 加入服务器所需的密钥

此接口会调用 Minecraft 的“加入服务器”API。该认证机制与 Minecraft 客户端加入服务器时所使用的机制相同。  

Minecraft 官方认证地址：`https://sessionserver.mojang.com/session/minecraft/join`，需要发送 POST 请求。

请求体格式（JSON）：
* `accessToken`：Minecraft 帐号的访问令牌  
* `selectedProfile`：Minecraft 帐号的 UUID（字符串格式，**不包含 `-` 字符**）  
* `serverId`：基于服务器标识生成的字符串（通过 `key` 计算）  

服务器ID 计算方式：
SHA-1 哈希：
`tom5454-paste`、`<会话ID>` 和 `<key>` 作为 ASCII 字符串

将结果哈希值转换为十六进制字符串，即为 `serverId`。

Java示例代码：
```java
	byte[] mojKey = Base64.getDecoder().decode(key); // 来自 API 的密钥字符串
	// SHA-1 散列所有数据
	byte[] mojangKey = digestData("tom5454-paste".getBytes(), session.getBytes(), mojKey);
	String serverId = new BigInteger(mojangKey).toString(16);
```

完成会话设置前，请先调用 [认证](#authenticate) 接口。


<a name="authenticate"/>

### 认证
接口路径：`api/session`

HTTP headers：
* Session：<连接的会话 ID>

响应：（JSON）
	成功时返回空 JSON


<a name="list"/>

### 列表
接口路径：`api/list`

HTTP headers：
* Session：<连接的会话 ID>

需要身份验证

响应：（JSON）
* 文件（数组对象）
（每个对象）
  * id：粘贴id
  * name：粘贴名称
  * time： 上传时间（以毫秒为单位的 UTC 时间）
* maxSize  
* maxFiles


<a name="upload"/>

### 上传
接口路径；`api/upload`

HTTP headers：
* Session：<连接的会话 ID>
* Content-Length: 文件长度  
* File-Name: 粘贴名称

需要身份验证和POST 请求

请求体：粘贴内容

响应：（JSON）
* id：新生成的随机粘贴 ID


<a name="delete"/>

### 删除
接口路径：`api/delete?file=<paste_id>`

HTTP headers：  
* Session：<连接的会话 ID>

需要身份验证

响应：（JSON）
    成功时返回空 JSON


<a name="update"/>

### 更新
接口路径：`api/update?file=<paste_id>`

HTTP headers：  
* Session：<连接的会话 ID>  
* Content-Length：文件长度

需要身份验证和POST 请求

请求体：粘贴内容  
响应：（JSON）
    成功时返回空 JSON  

<a name="browser-login"/>

### 浏览器登录
接口路径：`api/browser_login`

HTTP headers：  
* Session：<连接的会话 ID>  

需要身份验证

响应：（JSON）
* id：浏览器登录令牌，有效期 5 分钟，可用于 `/login.html?id=<id>` 页面  


<a name="app-login"/>

### 应用程序登录

接口路径：`api/app_login`

HTTP headers：  
* AppID：<应用程序 ID>  

响应：（JSON）
* id：应用程序登录令牌，有效期 5 分钟，可用于 `/auth.html?id=<id>` 页面  


<a name="app-check"/>

### 应用程序检查
接口路径：`api/app_check`

HTTP headers：  
* AppID：<应用程序 ID>  
* Session：<应用程序登录的 ID>  

响应：（JSON）
* id：粘贴应用程序令牌  
或者  
* 空 JSON（如果尚未授权）

<a name="app-token-request"/>

### 应用程序令牌请求
接口路径：`api/app_token_req`

HTTP headers：  
* AppID：<应用程序 ID>  
* Session：<应用程序登录的 ID>  

响应：（JSON）
* id：粘贴会话令牌，可用于[列表](#list)、[上传](#upload)、[删除](#delete)、[更新](#update)操作  


<a name="error-messages"/>

### 错误信息
响应：（JSON）
* error：人类可读的错误消息  
* errorMessage：错误消息的翻译键
