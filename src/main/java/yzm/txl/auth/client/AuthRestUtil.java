/**
 *  Copyright (c) SHADANSOU 2014 All Rights Reserved
 *
 */
package yzm.txl.auth.client;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * 鉴权访问rest服务公共类
 * <p>
 * 
 * create 2014-8-7<br>
 * 
 * @author XZH<br>
 * @version Revision 2014-8-7
 * @since 1.0
 */
public class AuthRestUtil {

	private static final Log logger = LogFactory.getLog(AuthRestUtil.class);
	public static String postAuthServer(String domain, int port, String protocol, String url,
			Map<String, String> paraMap) {
		HttpClient client = new HttpClient();
		client.getHostConfiguration().setHost(domain, port, protocol);
	 
		HttpMethod method = getPostResult(url, paraMap);// 使用POST方式提交数据
		
		 
		String response = "";
		try {
			client.executeMethod(method);
			//System.out.println(method.getStatusLine());
			// 打印结果页面
			response = new String(method.getResponseBodyAsString().getBytes());
			if (response.contains("token不存在") || response.contains("400")) {
				logger.error("异常信息：" + url + "      " + paraMap);
			}
			// 打印返回的信息
			logger.info("access_token return response:"+response);  
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage(),e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage(),e);
		}
		// 打印服务器返回的状态

		method.releaseConnection();

		return response;
	}

	private static HttpMethod getPostResult(String url, Map<String, String> paraMap) {
		// TODO Auto-generated method stub
		PostMethod post = new PostMethod(url);
		NameValuePair[] valueParirs = new NameValuePair[paraMap.size()];
		int index = 0;
		for (Map.Entry<String, String> entry : paraMap.entrySet()) {
			//System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
			NameValuePair valuePair = new NameValuePair(entry.getKey(), entry.getValue());
			valueParirs[index] = valuePair;
			index++;
		}

		post.setRequestBody(valueParirs);
		return post;

	}

}
