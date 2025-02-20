import { useI18n } from '@/hooks/useI18n';

import type { CommonScriptMenu } from '@/models/projectManagement/commonScript';

const { t } = useI18n();

export type Languages = 'groovy' | 'python' | 'beanshell' | 'nashornScript' | 'rhinoScript' | 'javascript';

export const SCRIPT_MENU: CommonScriptMenu[] = [
  {
    title: t('project.code_segment.importApiTest'),
    value: 'api_definition',
    command: 'api_definition',
  },
  {
    title: t('project.code_segment.newApiTest'),
    value: 'new_api_request',
    command: 'new_api_request',
  },
  {
    title: t('project.processor.codeTemplateGetVariable'),
    value: 'vars.get("variable_name")',
  },
  {
    title: t('project.processor.codeTemplateSetVariable'),
    value: 'vars.put("variable_name", "variable_value")',
  },
  {
    title: t('project.processor.codeTemplateGetResponseHeader'),
    value: 'prev.getResponseHeaders()',
  },
  {
    title: t('project.processor.codeTemplateGetResponseCode'),
    value: 'prev.getResponseCode()',
  },
  {
    title: t('project.processor.codeTemplateGetResponseResult'),
    value: 'prev.getResponseDataAsString()',
  },
  {
    title: t('project.processor.paramEnvironmentSetGlobalVariable'),
    value: `vars.put(\${__metersphere_env_id}+"key","value"); 
        vars.put("key","value")`,
  },
  {
    title: t('project.processor.insertPublicScript'),
    value: 'custom_function',
    command: 'custom_function',
  },
  {
    title: t('project.processor.terminationTest'),
    value: 'ctx.getEngine().stopThreadNow(ctx.getThread().getThreadName());',
  },
];

// 处理groovyCode 请求头
function getGroovyHeaders(requestHeaders) {
  let headers = '[';
  let index = 1;
  // for (const [k, v] of requestHeaders) {
  //   if (index !== 1) {
  //     headers += ',';
  //   }
  //   // 拼装
  //   headers += `'${k}':'${v}'`;
  //   index++;
  // }
  requestHeaders.forEach(([k, v]) => {
    if (index !== 1) {
      headers += ',';
    }
    headers += `'${k}':'${v}'`;
    index++;
  });
  headers += ']';
  return headers;
}
// 解析请求url
function getRequestPath(requestArgs, requestPath) {
  if (requestArgs.size > 0) {
    requestPath += '?';
    let index = 1;
    requestArgs.forEach(([k, v]) => {
      if (index !== 1) {
        requestPath += '&';
      }
      requestPath = `${requestPath + k}=${v}`;
      index++;
    });
  }
  return requestPath;
}
// 处理mockPath
function getMockPath(domain, port, socket) {
  if (domain === socket || !port) {
    return '';
  }
  const str = `${domain}:${port}`;
  // 获取socket之后的路径
  return socket.substring(str.length);
}

// 处理请求参数
function replaceRestParams(path, restMap) {
  if (!path) {
    return path;
  }
  let arr = path.match(/{([\w]+)}/g);
  if (Array.isArray(arr) && arr.length > 0) {
    arr = Array.from(new Set(arr));
    arr.forEach((str) => {
      try {
        const temp = str.substr(1);
        const param = temp.substring(0, temp.length - 1);
        if (str && restMap.has(param)) {
          path = path.replace(new RegExp(str, 'g'), restMap.get(param));
        }
      } catch (e) {
        // nothing
      }
    });
  }
  return path;
}

// 返回最终groovyCode 代码模板片段
function _groovyCodeTemplate(obj) {
  const { requestUrl, requestMethod, headers, body } = obj;
  const params = `[
                'url': '${requestUrl}',
                'method': '${requestMethod}', // POST/GET
                'headers': ${headers}, // 请求headers 例：{'Content-type':'application/json'}
                'data': ${body} // 参数
                ]`;
  return `import groovy.json.JsonOutput
import groovy.json.JsonSlurper

def params = ${params}
def headers = params['headers']
// json数据
def data = params['data']
def conn = new URL(params['url']).openConnection()
conn.setRequestMethod(params['method'])
if (headers) {
    headers.each {
      k,v -> conn.setRequestProperty(k, v);
    }
}
if (data) {
    // 输出请求参数
    log.info(data)
    conn.doOutput = true
    def writer = new OutputStreamWriter(conn.outputStream)
    writer.write(data)
    writer.flush()
    writer.close()
}
log.info(conn.content.text)
`;
}

// 处理groovyCode语言
function groovyCode(requestObj) {
  const {
    requestHeaders = new Map(),
    requestBody = '',
    domain = '',
    port = '',
    requestMethod = '',
    host = '',
    protocol = '',
    requestArguments = new Map(),
    requestRest = new Map(),
    requestBodyKvs = new Map(),
    bodyType,
  } = requestObj;

  let { requestPath = '' } = requestObj;
  let requestUrl = '';
  if (requestMethod.toLowerCase() === 'get' && requestBodyKvs) {
    // 如果是get方法要将kv值加入argument中
    requestBodyKvs.forEach(([k, v]) => {
      requestArguments.set(k, v);
    });
  }
  requestPath = getRequestPath(requestArguments, requestPath);
  const path = getMockPath(domain, port, host);
  requestPath = path + replaceRestParams(requestPath, requestRest);
  if (protocol && host && requestPath) {
    requestUrl = `${protocol}://${domain}${port ? `:${port}` : ''}${requestPath}`;
  }
  let body = JSON.stringify(requestBody);
  if (requestMethod === 'POST' && bodyType === 'kvs') {
    body = '"';

    requestBodyKvs.forEach(([k, v]) => {
      if (body !== '"') {
        body += '&';
      }
      body += `${k}=${v}`;
    });
    body += '"';
  }

  if (bodyType && bodyType.toUpperCase() === 'RAW') {
    requestHeaders.set('Content-type', 'text/plain');
  }
  const headers = getGroovyHeaders(requestHeaders);
  const obj = { requestUrl, requestMethod, headers, body };
  return _groovyCodeTemplate(obj);
}

// 获取请求头
function getHeaders(requestHeaders) {
  let headers = '{';
  let index = 1;
  requestHeaders.forEach(([k, v]) => {
    if (index !== 1) {
      headers += ',';
    }
    // 拼装
    headers += `'${k}':'${v}'`;
    index++;
  });
  headers += '}';
  return headers;
}
// 获取pythonCode 模板
function _pythonCodeTemplate(obj) {
  const { requestBody, requestBodyKvs, bodyType, requestPath, requestMethod, connType, domain, port } = obj;
  let { headers } = obj;
  let reqBody = obj.requestBody;
  if (requestMethod.toLowerCase() === 'post' && obj.bodyType === 'kvs' && obj.requestBodyKvs) {
    reqBody = 'urllib.urlencode({';
    requestBodyKvs.forEach(([k, v]) => {
      reqBody += `'${k}':'${v}'`;
    });
    reqBody += `})`;
    if (headers === '{}') {
      headers = "{'Content-type': 'application/x-www-form-urlencoded', 'Accept': 'text/plain'}";
    }
  }

  const host = domain + (port ? `:${port}` : '');

  return `import httplib,urllib
params = ${reqBody} #例 {'username':'test'}
headers = ${headers} #例 {'Content-Type':'application/json'} 或 {'Content-type': 'application/x-www-form-urlencoded', 'Accept': 'text/plain'}
host = '${host}'
path = '${requestPath}'
method = '${requestMethod}' # POST/GET

conn = httplib.${connType}(host)
conn.request(method, path, params, headers)
res = conn.getresponse()
data = unicode(res.read(), 'utf-8')
log.info(data)
`;
}

// 处理pythonCode语言
function pythonCode(requestObj) {
  const {
    requestHeaders = new Map(),
    requestMethod = '',
    host = '',
    domain = '',
    port = '',
    protocol = 'http',
    requestArguments = new Map(),
    requestBodyKvs = new Map(),
    bodyType,
    requestRest = new Map(),
  } = requestObj;
  let { requestBody = '', requestPath = '/' } = requestObj;
  let connType = 'HTTPConnection';
  if (protocol === 'https') {
    connType = 'HTTPSConnection';
  }
  const headers = getHeaders(requestHeaders);
  requestBody = requestBody ? JSON.stringify(requestBody) : '{}';
  if (requestMethod.toLowerCase() === 'get' && requestBodyKvs) {
    requestBodyKvs.forEach(([k, v]) => {
      requestArguments.set(k, v);
    });
  }
  requestPath = getRequestPath(requestArguments, requestPath);
  const path = getMockPath(domain, port, host);
  requestPath = path + replaceRestParams(requestPath, requestRest);
  const obj = { requestBody, headers, requestPath, requestMethod, requestBodyKvs, bodyType, connType, domain, port };
  return _pythonCodeTemplate(obj);
}

// 获取javaBeanshell代码模版
function _beanshellTemplate(obj) {
  const {
    requestHeaders = new Map(),
    requestBodyKvs = new Map(),
    bodyType = '',
    requestMethod = 'GET',
    protocol = 'http',
    requestArguments = new Map(),
    domain = '',
    host = '',
    port = '',
    requestRest = new Map(),
  } = obj;
  let { requestPath = '/', requestBody = '' } = obj;

  const path = getMockPath(domain, port, host);
  requestPath = path + replaceRestParams(requestPath, requestRest);
  let uri = `new URIBuilder()
                .setScheme("${protocol}")
                .setHost("${domain}")
                .setPath("${requestPath}")
                `;
  // http 请求类型
  const method = requestMethod.toLowerCase().replace(/^\S/, (s) => s.toUpperCase());
  const httpMethodCode = `Http${method} request = new Http${method}(uri);`;
  // 设置参数
  requestArguments.forEach(([k, v]) => {
    uri += `.setParameter("${k}", "${v}")`;
  });
  if (method === 'Get' && requestBodyKvs) {
    requestBodyKvs.forEach(([k, v]) => {
      uri += `.setParameter("${k}", "${v}")`;
    });
  }

  let postKvsParam = '';
  if (method === 'Post') {
    // 设置post参数
    requestBodyKvs.forEach(([k, v]) => {
      postKvsParam += `nameValueList.add(new BasicNameValuePair("${k}", "${v}"));\r\n`;
    });
    if (postKvsParam !== '') {
      postKvsParam = `List nameValueList = new ArrayList();\r\n${postKvsParam}`;
    }
  }

  if (port) {
    uri += `.setPort(${port}) // int类型端口
    `;
    uri += `            .build();`;
  } else {
    uri += `// .setPort(${port}) // int类型端口
    `;
    uri += `            .build();`;
  }
  // 设置请求头
  let setHeader = '';
  requestHeaders.forEach(([k, v]) => {
    setHeader = `${setHeader}request.setHeader("${k}", "${v}");\n`;
  });
  try {
    requestBody = JSON.stringify(requestBody);
    if (!requestBody || requestBody === 'null') {
      requestBody = '';
    }
  } catch (e) {
    requestBody = '';
  }
  let postMethodCode = '';
  if (requestMethod === 'POST') {
    if (bodyType === 'kvs') {
      postMethodCode = `${postKvsParam}\r\n request.setEntity(new UrlEncodedFormEntity(nameValueList, "UTF-8"));`;
    } else {
      postMethodCode = `request.setEntity(new StringEntity(StringEscapeUtils.unescapeJava(payload)));`;
    }
  }

  return `import java.net.URI;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.*;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.StringEntity;

import java.util.*;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;

// 创建Httpclient对象
CloseableHttpClient httpclient = HttpClients.createDefault();
// 参数
String payload = ${requestBody};
// 定义请求的参数
URI uri = ${uri}
// 创建http请求
${httpMethodCode}
${setHeader}
${postMethodCode}
log.info(uri.toString());
//response 对象
CloseableHttpResponse response = null;

response = httpclient.execute(request);
// 判断返回状态是否为200
if (response.getStatusLine().getStatusCode() == 200) {
    String content = EntityUtils.toString(response.getEntity(), "UTF-8");
    log.info(content);
}`;
}

// 处理java语言
function javaCode(requestObj) {
  return _beanshellTemplate(requestObj);
}

// 获取js语言代码模版
function _jsTemplate(obj) {
  const {
    requestHeaders = new Map(),
    requestMethod = 'GET',
    protocol = 'http',
    requestArguments = new Map(),
    host = '',
    domain = '',
    port = '',
    requestBodyKvs = new Map(),
    bodyType = '',
    requestRest = new Map(),
  } = obj;
  let url = '';
  let { requestBody = '', requestPath = '/' } = obj;
  requestPath = replaceRestParams(requestPath, requestRest);
  if (protocol && domain && port) {
    const path = getMockPath(domain, port, host);
    requestPath = path + requestPath;
    url = `${protocol}://${domain}${port ? `:${port}` : ''}${requestPath}`;
  } else if (protocol && domain) {
    url = `${protocol}://${domain}${requestPath}`;
  }
  if (requestMethod.toLowerCase() === 'get' && requestBodyKvs) {
    // 如果是get方法要将kv值加入argument中
    requestBodyKvs.forEach(([k, v]) => {
      requestArguments.set(k, v);
    });
  }
  url = getRequestPath(requestArguments, url);
  try {
    requestBody = JSON.stringify(requestBody);
  } catch (e) {
    requestBody = '';
  }

  let connStr = '';
  if (bodyType && bodyType.toUpperCase() === 'RAW') {
    requestHeaders.set('Content-type', 'text/plain');
  }
  requestHeaders.forEach(([k, v]) => {
    connStr += `conn.setRequestProperty("${k}","${v}");\n`;
  });

  if (requestMethod === 'POST' && bodyType === 'kvs') {
    requestBody = '"';
    requestBodyKvs.forEach(([k, v]) => {
      if (requestBody !== '"') {
        requestBody += '&';
      }
      requestBody += `${k}=${v}`;
    });
    requestBody += '"';
  }
  let postParamExecCode = '';
  if (requestBody && requestBody !== '' && requestBody !== '""') {
    postParamExecCode = `
var opt = new java.io.DataOutputStream(conn.getOutputStream());
var t = (new java.lang.String(parameterData)).getBytes("utf-8");
opt.write(t);
opt.flush();
opt.close();
    `;
  }

  return `var urlStr = "${url}"; // 请求地址
var requestMethod = "${requestMethod}"; // 请求类型
var parameterData = ${requestBody}; // 请求参数
var url = new java.net.URL(urlStr);
var conn = url.openConnection();
conn.setRequestMethod(requestMethod);
conn.setDoOutput(true);
${connStr}conn.connect();
${postParamExecCode}
var res = "";
var rspCode = conn.getResponseCode();
if (rspCode == 200) {
var ipt = conn.getInputStream();
var reader = new java.io.BufferedReader(new java.io.InputStreamReader(ipt, "UTF-8"));
var lines;
while((lines = reader.readLine()) !== null) {
  res += lines;
}
}
log.info(res);
  `;
}

// 处理js语言
function jsCode(requestObj) {
  return _jsTemplate(requestObj);
}

export function getCodeTemplate(language: Languages, requestObj: any) {
  switch (language) {
    case 'groovy':
      return groovyCode(requestObj);
    case 'python':
      return pythonCode(requestObj);
    case 'beanshell':
      return javaCode(requestObj);
    case 'nashornScript':
      return jsCode(requestObj);
    case 'rhinoScript':
      return jsCode(requestObj);
    case 'javascript':
      return jsCode(requestObj);
    default:
      return '';
  }
}

export default {};
