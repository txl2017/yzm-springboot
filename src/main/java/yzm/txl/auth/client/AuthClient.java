/**
 *  Copyright (c) SHADANSOU 2014 All Rights Reserved
 *
 */
package yzm.txl.auth.client;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 鉴权客户端.
 * <p>
 * 
 * create 2014-8-7<br>
 * 
 * @author XZH<br>
 * @version Revision 2014-8-7
 * @since 1.0
 */
public class AuthClient {
	public static final int PASSWORD_MIN_LEN = 4;
	
	// 登陆验证： desc：用户名，密码，应用ID，应用秘钥登录
	public static String login(String domain, int port, String protocol, String user_name, String password,
			String client_id, String client_secret, String user_ip) {
		Map<String, String> paraMap = new HashMap<String, String>();

		// 密码截取
		if (null != password && password.length() > PASSWORD_MIN_LEN) {
			password = password.substring(0, password.length() - PASSWORD_MIN_LEN);
		}
		paraMap.put("user_name", user_name);
		paraMap.put("password", password);
		paraMap.put("client_id", client_id);
		paraMap.put("client_secret", client_secret);
		paraMap.put("user_ip", user_ip);
		return AuthRestUtil.postAuthServer(domain, port, protocol, "/auth/login/login", paraMap);

	}

	// 登出验证： access_token ip
	public static String logout(String domain, int port, String protocol,String access_token,String user_ip) {
		Map<String, String> paraMap = new HashMap<String, String>();

		paraMap.put("access_token", access_token);
		paraMap.put("user_ip", user_ip);
		return AuthRestUtil.postAuthServer(domain, port, protocol, "/auth/login/logout", paraMap);

	}

	/**
	 * 验证token access_token token值 remote_addr 请求的来源服务器地址 rest_id 请求的rest地址
	 */
	public static String verfiyToken(String domain, int port, String protocol, String access_token, String remote_addr,
			String rest_id,String rest_params,String user_ip) {
		Map<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("access_token", access_token);
		paraMap.put("remote_addr", remote_addr);
		paraMap.put("rest_id", rest_id);
		paraMap.put("user_ip", user_ip);
		paraMap.put("rest_params", rest_params);
		return AuthRestUtil.postAuthServer(domain, port, protocol, "/auth/token/verify_token", paraMap);

	}

	/**
	 * 获取或者刷新token client_id 应用ID client_secret 应用secret grant_type
	 * client_credentials,refresh_token refresh_token 刷新token时使用
	 */
	public static String accessToken(String domain, int port, String protocol, String client_id, String client_secret,
			String grant_type, String refresh_token) {
		Map<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("client_id", client_id);
		paraMap.put("client_secret", client_secret);
		paraMap.put("grant_type", grant_type);
		if (null != refresh_token) {
			paraMap.put("refresh_token", refresh_token);
		}
		return AuthRestUtil.postAuthServer(domain, port, protocol, "/auth/token/access_token", paraMap);

	}

	// for test
	public static void main(String[] args) {

		String result = AuthClient.login("172.16.110.101", 8080, "http", "country", "9218965eb72c92a549dd5a3301125OF6",
				"3E6570bKX4okbBT10TANVols", "349lUwapX54MbjA0wM5q0ZiS", "192.168.2.1");
		System.out.println(result);
		
// 		String result2 = AuthClient.logout("172.16.110.101", 8080, "http", "0dbN6MffL4ib8Bl089934vvQ", "127.0.0.1");
//		System.out.println(result2);

		// String result = AuthClient.verfiyToken("172.19.108.10", 8080,"http",
		// "1JBp9T4wv4k7at30hIj7lQWT","10.1.1.1","xx");
		// System.out.println(result);

//		 String result3 = AuthClient.accessToken("172.19.110.101", 8080,"http",
//		 "3E6570bKX4okbBT10TANVols","349lUwapX54MbjA0wM5q0ZiS","refresh_token","4l0grDbz64JmaBr0h1kXlag9");
//		 System.out.println("result3="+result3);
 	}

}
