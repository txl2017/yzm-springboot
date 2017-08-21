/**
 *  Copyright (c) SHADANSOU 2014 All Rights Reserved
 *
 */
package yzm.txl.auth.msg;

/**
 * <p>class function description.<p>
 *
 * create  2014-8-7<br>
 * @author  XZH<br>
 * @version Revision   2014-8-7
 * @since   1.0
 */
public class ErrorMsg {

	private int errorCode;
	private String errorDesc;
	private String requestUrl;
	
	public ErrorMsg(int errorCode,String errorDesc, String requestUrl){
		this.errorCode = errorCode;
		this.errorDesc = errorDesc;
		this.requestUrl = requestUrl;
	}
	
	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorDesc() {
		return errorDesc;
	}

	public void setErrorDesc(String errorDesc) {
		this.errorDesc = errorDesc;
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

}
