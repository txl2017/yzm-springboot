package yzm.txl.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import yzm.txl.bean.ResponseDto;
import yzm.txl.bean.YzmDto;
import yzm.txl.util.YzmUtil;

@Component
@Path("rest")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class YzmRest {
	
	private static final Logger logger = LoggerFactory.getLogger(YzmRest.class);
	@Autowired
	private YzmDto yzmDto;
	@Autowired
	private ResponseDto resDto;
	
	/**
	 * 服务初始化
	 */
	public void init() {
		logger.debug("验证码服务启动");
	}
	
	/**
	 * 服务注销
	 */
	public void destroy() {
		logger.debug("验证码服务注销");
	}
	
	/**
	 * say hello to the world
	 * 
	 * @param name
	 * @return
	 */
	@GET
	@Path("/hello")
	public ResponseDto sayHello(@QueryParam("name") String name) {
		String response = "hello " + (name != null ? name : "world");
		resDto.setEntity(response);
		resDto.setResult(0);
		resDto.setErrorInfo("");
		return resDto;
	}
	
	/**
	 * 生成验证码，返回png的base64码和md5加密后的验证码
	 * 
	 * @return
	 */
	@GET
	@Path("/getyzm")
	public ResponseDto getYzm() {
		String yzm = YzmUtil.genCaptcha(4);
		logger.debug("getYzm yzm = "+yzm);
		yzmDto.setCode(YzmUtil.md5Digest(yzm));
		try {
			yzm = YzmUtil.convertPng2Base64(YzmUtil.genCaptchaImg(yzm));
			yzmDto.setBase64(yzm);
			resDto.setEntity(yzmDto);
			resDto.setResult(0);
			resDto.setErrorInfo("");
			return resDto;
		} catch (Exception e) {
			e.printStackTrace();
			resDto.setErrorInfo("验证码转换出错");
			resDto.setEntity(yzmDto);
			resDto.setResult(-1);
			return resDto;
		}
	}
	
	/**
	 * 验证
	 * 
	 * @param yzm
	 * @param code
	 * @return
	 */
	@GET
	@Path("/validateyzm")
	public ResponseDto validateYzm(@QueryParam("yzm") String yzm, @QueryParam("code") String code) {
		logger.debug("validateYzm yzm = "+yzm);
		if(YzmUtil.md5Digest(yzm).equals(code)){
			resDto.setEntity("sucess");
			resDto.setResult(0);
			resDto.setErrorInfo("");
		}else{
			resDto.setEntity("fail");
			resDto.setResult(-1);
			resDto.setErrorInfo("验证码认证失败");
		}
		return resDto;
	}
	
}
