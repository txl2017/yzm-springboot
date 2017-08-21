package yzm.txl.bean;

import org.springframework.stereotype.Component;

@Component
public class ResponseDto {

	private int result;
	private Object errorInfo;
	private Object entity;
	
	public int getResult() {
		return result;
	}
	public void setResult(int result) {
		this.result = result;
	}
	public Object getErrorInfo() {
		return errorInfo;
	}
	public void setErrorInfo(Object errorInfo) {
		this.errorInfo = errorInfo;
	}
	public Object getEntity() {
		return entity;
	}
	public void setEntity(Object entity) {
		this.entity = entity;
	}
	
}
