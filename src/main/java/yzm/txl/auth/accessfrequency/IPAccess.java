/**
 *  Copyright (c) SHADANSOU 2014 All Rights Reserved
 *
 */
package yzm.txl.auth.accessfrequency;

/**
 * <p>class function description.<p>
 *
 * create  2015-1-23<br>
 * @author  XZH<br>
 * @version Revision   2015-1-23
 * @since   1.0
 */
public class IPAccess {

	public final static int BLACK_STATUS = 1;
	public final static int WHITE_STATUS = 0;
	
	private String ip;
    private int count;
    private int status;
    private long refreshtime; // 检查刷新时间
    private long forbidtime;  //  封禁时间
    private long lastconcattime; //最近联系时间
    
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
    
	public long getRefreshtime() {
		return refreshtime;
	}
	public void setRefreshtime(long refreshtime) {
		this.refreshtime = refreshtime;
	}
	public long getForbidtime() {
		return forbidtime;
	}
	public void setForbidtime(long forbidtime) {
		this.forbidtime = forbidtime;
	}
	public long getLastconcattime() {
		return lastconcattime;
	}
	public void setLastconcattime(long lastconcattime) {
		this.lastconcattime = lastconcattime;
	}
	
}
