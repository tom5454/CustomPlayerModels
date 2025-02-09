
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

API 地址：`https://paste.tom5454.com/` 
Java中的完整实现：[CPM中的粘贴客户端](https://github.com/tom5454/CustomPlayerModels/blob/master/CustomPlayerModels/src/shared/java/com/tom/cpm/shared/paste/PasteClient.java)


<a name="download"/>

### 下载
URL：`raw/<粘贴ID>`


<a name="connect"/>

### 连接
URL：`api/connect?name=<玩家名称>`  
响应：（JSON）
 * id：会话id，未来的请求中需要
 * key: Minecraft 加入服务器密钥

运行 minecraft 的加入服务器 api。
这与您加入 Minecraft 服务器时 Minecraft 使用的身份验证相同。  
URL：`https://sessionserver.mojang.com/session/minecraft/join`  
需要 POST 请求
Post body：(JSON数据)
 * accessToken：Minecraft 帐户访问令牌
 * selectedProfile：Minecraft 帐户 uuid 作为字符串（不带 `-` 字符）
 * serverId: 使用服务器ID计算出的`key`

服务器ID
SHA-1 哈希值:
`tom5454-paste`、`<会话ID>` 和 `<key>` 作为 ASCII 字符串
然后将结果字符串化为十六进制数。
Java示例代码：
```java
	byte[] mojKey = Base64.getDecoder().decode(key); // 来自 API 的密钥字符串
	// SHA-1 散列所有数据
	byte[] mojangKey = digestData("tom5454-paste".getBytes(), session.getBytes(), mojKey);
	String serverId = new BigInteger(mojangKey).toString(16);
```

调用 [认证](#authenticate) 完成会话设置。


<a name="authenticate"/>

### 认证
URL：`api/session`  
HTTP 报头：
 * Session：<连接的会话 ID>

响应：（JSON）
	成功时返回空 JSON


<a name="list"/>

### 列表
URL：`api/list`  
HTTP 报头：
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
URL: `api/upload`  
HTTP 报头：
 * Session：<连接的会话 ID>
 * Content-Length: 文件长度  
 * File-Name: 粘贴名称

需要身份验证
需要 POST 请求
Post body：粘贴内容 
响应：（JSON）
 * id：粘贴的新随机粘贴 ID


<a name="delete"/>

### 删除
URL: `api/delete?file=<paste_id>`  
HTTP 报头：
 * Session：<连接的会话 ID>

需要身份验证
响应：（JSON）
	成功时返回空 JSON


<a name="update"/>

### 更新
URL: `api/update?file=<paste_id>`  
HTTP 报头：
 * Session：<连接的会话 ID>
 * Content-Length: 文件长度   

需要身份验证
需要 POST 请求
Post body：粘贴内容 
响应：（JSON）
	成功时返回空 JSON
	

<a name="browser-login"/>

### 浏览器登录
URL: `api/browser_login`
HTTP 报头：
 * Session：<连接的会话 ID>
 
需要身份验证
响应：（JSON）
 * id：浏览器登录令牌，有效期 5 分钟，可与`/login.html?id=<id>`站点一起使用


<a name="app-login"/>

### 应用程序登录
HTTP 报头：
 * AppID：<应用程序 ID>
 
响应：（JSON）
 * id：应用程序登录令牌有效期为 5 分钟，可与`/auth.html?id=<id>`站点一起使用
 

<a name="app-check"/>

### 应用程序检查
URL: `api/app_check`
HTTP 报头：
 * AppID：<应用程序 ID>
 * Session：<应用程序登录的 ID>
 
响应：（JSON）
 * id：粘贴应用程序令牌
或者
 * 如果尚未授权，则为空 JSON
 

<a name="app-token-request"/>

### 应用程序令牌请求
URL: `api/app_token_req`
HTTP 报头：
 * AppID：<应用程序 ID>
 * Session：<应用程序登录的 ID>
 
响应：（JSON） 
 * id：粘贴可用于[列表](#list)、[上传](#upload)、[删除](#delete)、[更新](#update)的会话令牌


<a name="error-messages"/>

### 错误信息
响应：（JSON） 
 * error：人类可读的错误消息
 * errorMessage：错误消息的翻译键
