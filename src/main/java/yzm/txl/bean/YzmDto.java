package yzm.txl.bean;

import org.springframework.stereotype.Component;

/**
 * 验证码Dto
 * @author txl
 *
 */
@Component
public class YzmDto {

	private String base64;
	private String code;
	
	public String getBase64() {
		return base64;
	}
	public void setBase64(String base64) {
		this.base64 = base64;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
}
