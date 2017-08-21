package yzm.txl.filter;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import com.shadansou.sds.app.auth.accessfrequency.AccessCenter;
import com.shadansou.sds.app.auth.client.AuthClient;
import com.shadansou.sds.common.errorcode.ErrorCode;
import com.shadansou.sds.common.util.SdsRestResultUtils;
import com.shadansou.sds.commoninner.cache.dto.LoginError;
import com.shadansou.sds.commoninner.cache.dto.UserInfoCacheDto;
import com.shadansou.sds.commoninner.constants.CacheUniformName;
import com.shadansou.sds.hazelcast.api.IHazelcastOperation;
import com.shadansou.sds.hazelcast.api.imp.HazelcastOperationImpl;
import com.shadansou.sds.mongodb.api.IMongodbOperation;
import com.shadansou.sds.mongodb.api.impl.MongodbOperationImpl;

public class AuthorTokenFilter implements RequestHandler {

	public String domain;
	public int port;
	public String protocol;
	public String dbUrlList;
	public String cacheurl;

	public String cacheusername;
	public String cachepwd;
	public static IMongodbOperation imogo;
	public static IHazelcastOperation hazel;

	public static final int PWD_ERROR_TIMES = 3; // 密码出错3次将会被锁
	public static final int USERNAME_ERROR_TIMES = 5; // 用户名出错5次将会被锁、防止用户撞库
	public static final long ACCOUNT_LOCKED_TIMES = 60 * 60 * 1000;// 60分钟
	public static final String PWD_ERRTIME_TIMES_KEY = "pwderrortimes";
	public static final String PWD_ERRTIME_LASTTIME_KEY = "pwdlasterrortime";

	private static final Log LOGGER = LogFactory.getLog(AuthorTokenFilter.class);

	public void start() {
		imogo = new MongodbOperationImpl("", "", dbUrlList, "security_oauth_v2");
		System.out.println("cacheusername=" + cacheusername + ";pwd=" + cachepwd + ";url=" + cacheurl);
		hazel = new HazelcastOperationImpl(cacheusername, cachepwd, cacheurl);
		AccessCenter.start(hazel);
	}

	public void destroy() {
		AccessCenter.destroy();
	}

	@Override
	public Response handleRequest(Message message, ClassResourceInfo resourceClass) {

		HttpServletRequest request = (HttpServletRequest) message.get("HTTP.REQUEST");
		String client_info = request.getHeader("client-info");
		String user_ip = "";
		if (null != client_info) {
			try {
				user_ip = new JSONObject(((HttpServletRequest) message.get("HTTP.REQUEST")).getHeader("client-info"))
						.get("user-ip").toString();
//				System.out.println("user_ip = "+user_ip);
				//前端会有送2个IP的情况，取第一个
				if(user_ip.indexOf(",")!=-1){
					user_ip = user_ip.split(",")[0];
				}
				request.setAttribute("user-ip", user_ip);
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}
		if (message != null && resourceClass != null) {
			String requestUri = (String) message.get(Message.REQUEST_URI);
			if (requestUri.endsWith("search") || requestUri.endsWith("login") || requestUri.endsWith("activateUser")
					|| requestUri.endsWith("registerUser") || requestUri.endsWith("modifyPassword")
					|| requestUri.endsWith("checkRegisterUser") || requestUri.endsWith("checkUser")) {
				try {//做访问请求控制
//					if (!AccessCenter.access(
//							new JSONObject(((HttpServletRequest) message.get("HTTP.REQUEST")).getHeader("client-info"))
//									.get("user-ip").toString(), hazel)) {// 控制访问频率
					if (!AccessCenter.access(user_ip, hazel)) {// 控制访问频率
						System.out.println(user_ip + " limit!!"); 
						return Response.status(Status.INTERNAL_SERVER_ERROR)
								.entity(SdsRestResultUtils.getErrorResult(ErrorCode.IP_ACCESS_FREQUENTLY, null))
								.build();
					}
				} catch (Exception e) {// 匿名用户，没有client-info信息，属于正常情况
					// e.printStackTrace();
				}
			}
			String method = request.getMethod().toLowerCase();
			// 刷新token
			if (requestUri.endsWith("access_token")) {
				System.out.println("begin fresh access_token");
				return refreshToken(request, message, method);
			}

			// 首页地图展示
			/*
			 * registerUser checkRegisterUser checkUser activateUser sendMail
			 * checkUrlValid activateUser modifyPassword
			 */

			if (requestUri.endsWith("mapinfo") || requestUri.endsWith("accountStatus")
					|| requestUri.contains("registerUser") || requestUri.contains("checkRegisterUser")
					|| requestUri.contains("checkUser") || requestUri.contains("activateUser")
					|| requestUri.contains("sendMail") || requestUri.contains("checkUrlValid")
					|| requestUri.contains("modifyPassword") || requestUri.endsWith("getUnReadNotice")) {
				return null;
			}
			// 登陆处理
			if (requestUri.endsWith("login")) {
				return handleLogin(request, message, method, user_ip);
			}
			// 登出处理
			if (requestUri.endsWith("logout")) {
				return handleLogout(request, message, method, user_ip);
			}
			// 验证token处理
			String tokenValue = request.getParameter("access_token");
			if (StringUtils.isNotBlank(tokenValue)) {
				return handleToken(request, tokenValue, requestUri, user_ip);
			}

			// 头部带过来token验证
			String authz = request.getHeader("Authorization");

			// 认为是匿名用户
			if (authz == null || authz.equals("")) {
				return null;
			}
			String[] authzs = authz.split(" ");
			if ((authzs.length == 2) && authzs[0].toLowerCase().equals("oauth2")) {
				tokenValue = authzs[1];

				return handleToken(request, tokenValue, requestUri, user_ip);
				// return handleToken(request, tokenValue);
			} else {
				String json = SdsRestResultUtils.getErrorResult(ErrorCode.HEADER_IS_MISSING, null);
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity(json).build();
			}
		}

		String json = SdsRestResultUtils.getErrorResult(ErrorCode.PARAMS_IS_NULL, null);
		return Response.status(Status.INTERNAL_SERVER_ERROR).entity(json).build();
	}

	/*
	 * function：刷新token client_id 应用ID client_secret 应用secret grant_type
	 * client_credentials,refresh_token refresh_token 刷新token时使用
	 */
	private Response refreshToken(HttpServletRequest request, Message message, String method) {

		// TODO Auto-generated method stub
		String client_id = "";
		String client_secret = "";
		String grant_type = "";
		String refresh_token = "";

		if (method.equals("get")) {
			client_id = request.getParameter("client_id");
			client_secret = request.getParameter("client_secret");
			grant_type = request.getParameter("grant_type");
			refresh_token = request.getParameter("refresh_token");
		} else if (method.equals("post")) {
			InputStream in = message.getContent(InputStream.class);
			String paramStr = "";
			try {
				paramStr = IOUtils.toString(in);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String[] params = paramStr.split("&");
			for (String param : params) {
				String[] strs = param.split("=");
				if (strs[0].trim().equals("client_id")) {
					client_id = new String(strs[1]);
				} else if (strs[0].trim().equals("client_secret")) {
					client_secret = new String(strs[1]);
				} else if (strs[0].trim().equals("grant_type")) {
					grant_type = new String(strs[1]);
				} else if (strs[0].trim().equals("refresh_token")) {
					refresh_token = new String(strs[1]);
				}
			}
		}
		String result = AuthClient.accessToken(domain, port, protocol, client_id, client_secret, grant_type,
				refresh_token);

		if (result.contains("errorCode")) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(result).build();
		}
		return Response.status(Status.OK).entity(result).build();
	}

	/*
	 * function：登陆处理 user_name 用户名 password 用户密码 client_id 应用ID client_secret
	 * 应用secret
	 */
	private Response handleLogin(HttpServletRequest request, Message message, String method, String user_ip) {
		System.out.println("begin validate loging:"+user_ip);
		String user_name = "";
		String password = "";
		String client_id = "";
		String client_secret = "";

		if (method.equals("get")) {
			user_name = request.getParameter("username");
			if(user_name==null||user_name.equals("")){
				//user_name是原来的
				user_name = request.getParameter("user_name");
			}
			password = request.getParameter("password");
			client_id = request.getParameter("client_id");
			client_secret = request.getParameter("client_secret");

		} else if (method.equals("post")) {
			InputStream in = message.getContent(InputStream.class);
			String paramStr = "";
			try {
				paramStr = IOUtils.toString(in);
			} catch (IOException e) {
				e.printStackTrace();
			}
			String[] params = paramStr.split("&");
			for (String param : params) {
				String[] strs = param.split("=");
				if (strs[0].trim().equals("username")||strs[0].trim().equals("user_name")) {
					try {
						user_name = URLDecoder.decode(new String(strs[1]), "utf-8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (strs[0].trim().equals("password")) {
					password = new String(strs[1]);
				} else if (strs[0].trim().equals("client_id")) {
					client_id = new String(strs[1]);
				} else if (strs[0].trim().equals("client_secret")) {
					client_secret = new String(strs[1]);
				}
			}
		}
		System.out.println("username="+user_name+",password="+password);
		Map<String, UserInfoCacheDto> appCacheMap = hazel.getMap(CacheUniformName.APPCACHE);
		initCacheMap(appCacheMap, user_name);
		initCacheMap(appCacheMap, user_ip);
		// TODO 将登陆用户用户名都做小写转换
		user_name = user_name.toLowerCase();
		if (!checkUserExist(user_name)) {
			if (!AccessCenter.isWhiteIp(user_ip)) {
				usernameErrorProcess(appCacheMap, user_ip);
				String result = pwdErrorProcess(appCacheMap, user_name, "");
				String json = SdsRestResultUtils.getErrorResult(ErrorCode.USER_OR_PASSWORD_ERROR, null);
				LOGGER.error("login error:json = " + json);
				LOGGER.error("login error:result = " + result);
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity(json).build();
			}else{// 白名单IP，不做控制
				//pwdErrorProcess(appCacheMap, user_name, "");
				System.out.println("is white ip");
				 String result = pwdErrorProcess(appCacheMap, user_name, "");
				 LOGGER.error("login error:result = " + result);
			}
		}
		if (!preProcessLogin(user_name)) {
			String json = SdsRestResultUtils.getErrorResult(ErrorCode.USER_NOT_ACTIVATED, null);
			LOGGER.error("login error:json = " + json);
			 String result = pwdErrorProcess(appCacheMap, user_name, "");
			 LOGGER.error("login error:result == " + result);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(json).build();
		}

		String result = AuthClient.login(domain, port, protocol, user_name, password, client_id, client_secret,
				user_ip);
		System.out.println("result = " + result);
		
		UserInfoCacheDto userDto = appCacheMap.get(user_name);
		UserInfoCacheDto ipDto = appCacheMap.get(user_ip);
		if (result.contains("errorCode") || result.trim().length() == 0) {// 临时把||
																			// result.trim().length()==0这个条件加上
			//不管什么错误，都保存错误状态，以便于前端出验证码  lkp add 2017-02-10
			result = pwdErrorProcess(appCacheMap, user_name, result);
//			if ((result.contains("\"errorCode\":\"11041\"") || result.trim().length() == 0)) {
//				result = pwdErrorProcess(appCacheMap, user_name, result);
//			}
			LOGGER.error("login error:result = " + result);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(result).build();
		} else {// 清除该用户的密码错误记录
			System.out.println("begin clean " + user_name + " record");
			LoginError error = userDto.getLoginError();
			error.setPwderrortimes(0);
			error.setPwdlasterrortime(new Date());
			userDto.setLoginError(error);
			appCacheMap.put(user_name, userDto);
			System.out.println("begin clean " + user_ip + " record");
			error = ipDto.getLoginError();
			error.setPwderrortimes(0);
			error.setPwdlasterrortime(new Date());
			userDto.setLoginError(error);
			appCacheMap.put(user_ip, userDto);
		}
		try {
			JSONObject retJson = new JSONObject(result);
			JSONObject userJson = (JSONObject) retJson.get("user");
			setRequestAttribute(request, userJson.getString("user_id"));
			request.setAttribute("result", result);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			LOGGER.error(e.getMessage(), e);
			String json = SdsRestResultUtils.getErrorResult(ErrorCode.CONNECT_TIMEOUT, null);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(json).build();
		}
		return null;
	}

	/**
	 * 
	 * @Title: preProcessLogin @Description: 在登录之前先判下状态 @param
	 *         user_name @return @return boolean 返回类型 @throws
	 */
	public boolean preProcessLogin(String user_name) {
		Document query = new Document();
		query.put("username", user_name);
		List<Document> list = imogo.find("user_info", query);
		if (null != list && !list.isEmpty()) {
			Document obj = list.get(0);
			if (null != obj.get("status")) {
				String status = obj.get("status").toString();
				if (status.equals("1")) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 判断用户是否存在
	 * 
	 * @param user_name
	 *            用户名
	 * @return
	 */
	public boolean checkUserExist(String user_name) {
		Document query = new Document();
		query.put("username", user_name);
		long count = imogo.getCount("user_info", query);
		if (count > 0) {
			return true;
		}
		return false;
	}

	/*
	 * function：登出处理 user_name 用户名 password 用户密码 client_id 应用ID client_secret
	 * 应用secret
	 */
	private Response handleLogout(HttpServletRequest request, Message message, String method, String user_ip) {
		System.out.println("in handlelogout");
		String access_token = "";
		if (method.equals("get")) {
			access_token = request.getParameter("access_token");
			if(user_ip==null||user_ip.equals("")){
				user_ip = request.getParameter("user_ip");
			}
		} else if (method.equals("post")) {
			InputStream in = message.getContent(InputStream.class);
			String paramStr = "";
			try {
				paramStr = IOUtils.toString(in);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(" paramStr=" + paramStr);
			String[] params = paramStr.split("&");
			for (String param : params) {
				String[] strs = param.split("=");
				if (strs[0].trim().equals("access_token")) {
					access_token = new String(strs[1]);
				}
				if (user_ip.equals("")&&strs[0].trim().equals("user_ip")) {
					user_ip = URLDecoder.decode(new String(strs[1]));
				}
			}
		}
		System.out.println("access_token: " + access_token + " user_ip: " + user_ip);
		String result = AuthClient.logout(domain, port, protocol, access_token, user_ip);
		System.out.println("logout result=" + result);
		if (result.contains("errorCode")) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(result).build();
		}

		return Response.status(Status.OK).entity(result).build();
	}

	// 验证token
	private Response handleToken(HttpServletRequest request, String tokenValue, String requestUrl, String user_ip) {
		String clientIp = request.getRemoteAddr();
		// System.out.println(clientIp);
		String rest_params = request.getQueryString();
		// System.out.println("rest_params: "+rest_params);

		// 解决中文乱码问题
		if (null != rest_params && rest_params.length() > 0) {
			try {
				// System.out.println("before rest_params "+rest_params);
				rest_params = URLDecoder.decode(new String(rest_params.replaceAll("%", "%25")), "utf-8");
				// System.out.println("after rest_params "+rest_params);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LOGGER.debug(e.getMessage(), e);
			}
		}

		String result = AuthClient.verfiyToken(domain, port, protocol, tokenValue, clientIp, requestUrl, rest_params,
				user_ip);
		if (null == result || result.contains("errorCode")) {
			return Response.status(Status.BAD_REQUEST).entity(result).build();
		}
		try {
			JSONObject retJson = new JSONObject(result);
			setRequestAttribute(request, retJson.getString("user_id"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
		}

		return null;
	}

	/**
	 * 密码失败特别处理
	 * 
	 * @return
	 */
	private String pwdErrorProcess(Map<String, UserInfoCacheDto> cacheMap, String user_name, String result) {
		int count = 1;
		UserInfoCacheDto userDto = cacheMap.get(user_name);
		LoginError loginError = userDto.getLoginError();
		Date last = loginError.getPwdlasterrortime();
		Date now = new Date();
		if (now.getTime() - last.getTime() > ACCOUNT_LOCKED_TIMES) {
			count = 1;
		} else {
			count = loginError.getPwderrortimes();
			count++;
		}
		System.out.println(user_name + " error count = " + count);
		loginError.setPwderrortimes(count);
		loginError.setPwdlasterrortime(new Date());
		userDto.setLoginError(loginError);
		cacheMap.put(user_name, userDto);

		if (count >= PWD_ERROR_TIMES) {
			System.out.println("user:" + user_name + " has input wrong pwd " + PWD_ERROR_TIMES + " times");
			result = result.replaceAll("\"errorCode\":\"11041\"", "\"errorCode\":\"11042\"");
			result = result.replaceAll("\"description\":\"用户名或密码错误\"",
					"\"description\":\"密码输错达到" + count + "次, 需要输入验证码\"");
			System.out.println("result = " + result);
		} else {
			String error_desc = "\"description\":\"密码错误，请重新输入\"";
			result = result.replaceAll("\"description\":\"用户名或密码错误\"", error_desc);
		}
		return result;

	}

	/**
	 * 密码失败特别处理
	 * 
	 * @return
	 */
	private String usernameErrorProcess(Map<String, UserInfoCacheDto> cacheMap, String user_ip) {
		int count = 1;
		String result = "";
		UserInfoCacheDto userDto = cacheMap.get(user_ip);
		LoginError loginError = userDto.getLoginError();
		Date last = loginError.getPwdlasterrortime();
		Date now = new Date();
		if (now.getTime() - last.getTime() > ACCOUNT_LOCKED_TIMES) {
			count = 1;
		} else {
			count = loginError.getPwderrortimes();
			count++;
		}
		System.out.println(user_ip + " error count = " + count);
		loginError.setPwderrortimes(count);
		loginError.setPwdlasterrortime(new Date());
		userDto.setLoginError(loginError);
		cacheMap.put(user_ip, userDto);

		if (count >= USERNAME_ERROR_TIMES) {
			System.out.println("userip :" + user_ip + " has input wrong pwd " + USERNAME_ERROR_TIMES + " times");
			result = result.replaceAll("\"errorCode\":\"11041\"", "\"errorCode\":\"11042\"");
			result = result.replaceAll("\"description\":\"用户名或密码错误\"",
					"\"description\":\"密码输错达到" + count + "次, 需要输入验证码\"");
			System.out.println("result = " + result);
		} else {
			String error_desc = "\"description\":\"密码错误，请重新输入\"";
			result = result.replaceAll("\"description\":\"用户名或密码错误\"", error_desc);
		}
		return result;

	}

	// 设置request的属性
	private void setRequestAttribute(HttpServletRequest request, String user_id) {
		request.setAttribute("user_id", user_id);
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getDbUrlList() {
		return dbUrlList;
	}

	public void setDbUrlList(String dbUrlList) {
		this.dbUrlList = dbUrlList;
	}

	public String getCacheurl() {
		return cacheurl;
	}

	public void setCacheurl(String cacheurl) {
		this.cacheurl = cacheurl;
	}

	public String getCacheusername() {
		return cacheusername;
	}

	public void setCacheusername(String cacheusername) {
		this.cacheusername = cacheusername;
	}

	public String getCachepwd() {
		return cachepwd;
	}

	public void setCachepwd(String cachepwd) {
		this.cachepwd = cachepwd;
	}

	private void initCacheMap(Map<String, UserInfoCacheDto> appCacheMap, String userName) {
		if (!appCacheMap.containsKey(userName)) {
			UserInfoCacheDto cacheDto = new UserInfoCacheDto();
			LoginError error = new LoginError();
			error.setPwderrortimes(5);
			error.setPwdlasterrortime(new Date());
			cacheDto.setLoginError(error);
			appCacheMap.put(userName, cacheDto);
		}
	}

}
