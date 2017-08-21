/**
 *  Copyright (c) SHADANSOU 2014 All Rights Reserved
 *
 */
package yzm.txl.auth.accessfrequency;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import com.hazelcast.core.HazelcastInstance;

import yzm.txl.bean.CacheUniformName;

/**
 * <p>class function description.<p>
 *
 * create  2015-1-23<br>
 * @author  XZH<br>
 * @version Revision   2015-1-23
 * @since   1.0
 */
public class AccessThread implements Runnable {

	public static final        int      MAXACCESSTIME = 200; //每分钟最大允许访问次数
	private static final       long     FORBITTIME    = 1000*60*60; //该IP被封禁的最大时长
	private static final       long     EXPIRETIME    = 1000*60*60; //僵死IP的最长IP
	private static final       long     REFRESHTTIME  = 1000*60; //刷新频率
	private static final       long     CHECKFRQUENCY = 1000; //检查频率
	private HazelcastInstance hazel;
	
	public AccessThread(HazelcastInstance hazel) {
		this.hazel = hazel;
	}
	
	@Override
	public void run() {
		while(true){
			//每隔1秒钟去轮询一次
			try {
				Thread.sleep(CHECKFRQUENCY);
				cycleIPAddress();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	
	
	/**
	 * 轮循处理IPmap 
	 */
	private void cycleIPAddress(){
		Iterator<String> it = AccessCenter.ACCESSMAP.keySet().iterator();
		Map<String, String> limitMap = hazel.getMap(CacheUniformName.ACCESS_LIMIT); 
		
        while(it.hasNext()){
        	String ip = it.next();
        	IPAccess ipAccess = AccessCenter.ACCESSMAP.get(ip);
        	long nowTime = new Date().getTime();
        	if(null == ipAccess){
        		continue;
        	}
        	//System.out.println(ipAccess.getStatus());
        	//如果处于白名单模式
        	if(ipAccess.getStatus() == IPAccess.WHITE_STATUS){
				// 重新统计，数据清0
				if (nowTime - ipAccess.getRefreshtime() > REFRESHTTIME) {
					ipAccess.setRefreshtime(nowTime);
					ipAccess.setCount(0);
				}

				if (nowTime - ipAccess.getLastconcattime() > EXPIRETIME) {
					System.out.println(ip + " has removed");
					it.remove();
					if (limitMap.containsKey(ip)) {
						limitMap.remove(ip);
					}
				}
        	} else{//如果处于黑名单模式
        		if(nowTime - ipAccess.getForbidtime() > FORBITTIME ){
        		    System.out.println(ip+"ss has removed");
        			it.remove();
        			if (limitMap.containsKey(ip)) {
						limitMap.remove(ip);
					}
    			}
        	}
        }
	}
	
	 
}
