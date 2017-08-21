/**
 *  Copyright (c) SHADANSOU 2014 All Rights Reserved
 *
 */
package yzm.txl.auth.accessfrequency;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hazelcast.core.HazelcastInstance;

import yzm.txl.bean.CacheUniformName;
import yzm.txl.util.SdsDateUtil;

/**
 * <p>频率访问中心.<p>
 *
 * create  2015-1-23<br>
 * @author  XZH<br>
 * @version Revision   2015-1-23
 * @since   1.0
 */
public class AccessCenter {

	private static final Log LOGGER = LogFactory.getLog(AccessCenter.class);
	public static Map<String,IPAccess> ACCESSMAP = new ConcurrentHashMap<String,IPAccess>();
	static FileWriter fw = null;
	/**
	 * 密码错误次数map
	 */
	public static Map<String,Integer> PWDERRORCOUNTMAP = new ConcurrentHashMap<String,Integer>();
	private static Thread checkThread;
	
	//频率访问中心启动
	public static void start(HazelcastInstance hazel){
		AccessThread at = new AccessThread(hazel);
		checkThread = new Thread(at);
		checkThread.start();
		String filename = SdsDateUtil.dateToStr(new Date(), SdsDateUtil.DATE_FORMAT_YYYY_MM_DD);
		try {
			fw = new FileWriter(new File("/home/sds/data/ipforbit__"+filename+".log"),true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//频率访问中心销毁
	public static void destroy(){
		if(null != checkThread){
			checkThread.interrupt();
		}
	}
	
	public static boolean isWhiteIp(String ip) {
		// 工作环境网段顾虑
		if (ip.trim().equals("113.247.222.18")
				|| ip.trim().equals("113.247.222.19")
				|| ip.trim().equals("127.0.0.1")
				|| ip.trim().equals("113.247.222.20")
				|| ip.trim().equals("113.247.222.21")
				|| ip.trim().equals("113.247.222.22")) {
			return true;
		} else {
			return false;
		}
	}
	//IP 访问限制
	public static boolean access(String ip, HazelcastInstance hazel){
		//System.out.println("access "+ip);
	
		if(ip == null){
			LOGGER.error("access: ip is null!  ");
			return false;
		}
		//工作环境网段顾虑
		if(isWhiteIp(ip)){
			return true;
		}
		IPAccess ipAccess = ACCESSMAP.get(ip);
		Map<String, String> limitMap = hazel.getMap(CacheUniformName.ACCESS_LIMIT); 
		long time = new Date().getTime();
		if(ipAccess == null){
			limitMap.clear();
			IPAccess newIpAccess = new IPAccess();
			newIpAccess.setCount(1);
			newIpAccess.setRefreshtime(time); 
			newIpAccess.setLastconcattime(time);
			newIpAccess.setIp(ip);
			newIpAccess.setStatus(IPAccess.WHITE_STATUS);
			ACCESSMAP.put(ip, newIpAccess);
			limitMap.put(ip, "{\"status\":\"unlimit\"}");
			return true;
		} else if(ipAccess.getStatus() == IPAccess.WHITE_STATUS ){
			ipAccess.setCount(ipAccess.getCount() + 1);
			ipAccess.setLastconcattime(time);
			if(ipAccess.getCount() >= AccessThread.MAXACCESSTIME){
				ipAccess.setStatus(IPAccess.BLACK_STATUS);
				ipAccess.setForbidtime(time);
				limitMap.put(ip, "{\"status\":\"limit\"}");
				try {
					fw.write("\n"+ipAccess.getIp()+" ("+ipAccess.getCount()+") has forbit "+new Date(ipAccess.getForbidtime()).toString());
					fw.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
				LOGGER.info(ipAccess.getIp()+" ("+ipAccess.getCount()+")has forbit "+new Date(ipAccess.getForbidtime()).toString());
				return false;
			}
			return true;
		} else {
			if (!limitMap.containsKey(ip)) {
				ACCESSMAP.remove(ip);
				return true;
			}
			ipAccess.setLastconcattime(time);
			LOGGER.info(ipAccess.getIp()+" ("+ipAccess.getCount()+")has forbit "+new Date(ipAccess.getForbidtime()).toString());
			try {
				fw.write("\n"+ipAccess.getIp()+" ("+ipAccess.getCount()+") has forbit "+new Date(ipAccess.getForbidtime()).toString());
				fw.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
			return false;
		}
	}
	
}
