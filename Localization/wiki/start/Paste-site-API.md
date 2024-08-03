
<a name="paste-site-api"/>

# Paste site API

<a name="endpoints"/>

## Endpoints
* [Download](#download)
* [Connect](#connect)
* [Authenticate](#authenticate)
* [List](#list)
* [Upload](#upload)
* [Delete](#delete)
* [Update](#update)
* [Browser Login](#browser-login)
* [App Login](#app-login)
* [App Check](#app-check)
* [App Token Request](#app-token-request)
* [Error Messages](#error-messages)

API url: `https://paste.tom5454.com/`  
Full implenetation in Java: [PasteClient in CPM](https://github.com/tom5454/CustomPlayerModels/blob/master/CustomPlayerModels/src/shared/java/com/tom/cpm/shared/paste/PasteClient.java)  


<a name="download"/>

### Download
URL: `raw/<paste_id>`


<a name="connect"/>

### Connect
URL: `api/connect?name=<username>`  
Response: (JSON)  
 * id: Session id, required in future requests  
 * key: Minecraft Join Server key  

Run minecraft's join server api. 
This is the same authentication that Minecraft uses when you join a Minecraft server.  
URL: `https://sessionserver.mojang.com/session/minecraft/join`  
POST request required  
Post body: (JSON object)
 * accessToken: Minecraft account access token
 * selectedProfile: Minecraft account uuid as string (without `-` characters)
 * serverId: server id calculated with `key`

Calculating the serverId:  
SHA-1 hash:
`tom5454-paste`, `<session id>`, and `<key>` as ascii string
then toString the result as hex number.
Java example code:  
```java
	byte[] mojKey = Base64.getDecoder().decode(key);//key string from api
	//SHA-1 digest all of the data
	byte[] mojangKey = digestData("tom5454-paste".getBytes(), session.getBytes(), mojKey);
	String serverId = new BigInteger(mojangKey).toString(16);
```

Call [Authenticate](#authenticate) to finish setting up the session.


<a name="authenticate"/>

### Authenticate
URL: `api/session`  
HTTP headers:  
 * Session: <session id from connect>  

Response: (JSON)  
	Empty JSON on success


<a name="list"/>

### List
URL: `api/list`  
HTTP headers:  
 * Session: <session id from connect>  

Auth required  
Response: (JSON)  
 * files (Array of Objects)  
   (each object)  
   * id: paste id  
   * name: Paste name  
   * time: upload time as string in ms UTC time  
 * maxSize  
 * maxFiles  


<a name="upload"/>

### Upload
URL: `api/upload`  
HTTP headers:  
 * Session: <session id from connect>  
 * Content-Length: Length of file  
 * File-Name: Paste name  

Auth required  
POST request required  
Post body: paste content  
Response: (JSON)  
 * id: new random paste id for the paste


<a name="delete"/>

### Delete
URL: `api/delete?file=<paste_id>`  
HTTP headers:  
 * Session: <session id from connect>  

Auth required  
Response: (JSON)  
	Empty JSON on success 


<a name="update"/>

### Update
URL: `api/update?file=<paste_id>`  
HTTP headers:  
 * Session: <session id from connect>  
 * Content-Length: Length of file  

Auth required  
POST request required  
Post body: paste content  
Response: (JSON)  
	Empty JSON on success
	

<a name="browser-login"/>

### Browser Login
URL: `api/browser_login`
HTTP headers:  
 * Session: <session id from connect>  
 
Auth required  
Response: (JSON)  
 * id: browser login token valid for 5 minutes, usable with `/login.html?id=<id>` site


<a name="app-login"/>

### App Login
URL: `api/app_login`
HTTP headers:  
 * AppID: <application id>  
 
Response: (JSON)  
 * id: application login token valid for 5 minutes, usable with `/auth.html?id=<id>` site
 

<a name="app-check"/>

### App Check
URL: `api/app_check`
HTTP headers:  
 * AppID: <application id> 
 * Session: <id from app login>
 
Response: (JSON)  
 * id: paste application token
or
 * empty JSON if not authorized yet
 

<a name="app-token-request"/>

### App Token Request
URL: `api/app_token_req`
HTTP headers:  
 * AppID: <application id> 
 * Session: <id from app login>
 
Response: (JSON)  
 * id: paste session token usable with [List](#list), [Upload](#upload), [Delete](#delete), [Update](#update)


<a name="error-messages"/>

### Error messages
Respose: (JSON)  
 * error: Human readable error message
 * errorMessage: Translation key for error message
