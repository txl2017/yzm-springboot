/*
/*
 * Copyright (c) SHADANSOU 2014 All Rights Reserved
 *
 */

package yzm.txl.util;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>class function description.<p>
 *
 * create  2014-5-13<br>
 * @author  $Author$<br>
 * @version $Revision$ $Date$
 * @since   1.0
 */

public class SdsDateUtil {
	public SdsDateUtil() {// null
	}
	private static Log logger = LogFactory.getLog(SdsDateUtil.class.getName());
	// 日期格式，年份，例如：2004，2008
	public static final String DATE_FORMAT_YYYY = "yyyy";
	// 日期格式，年份和月份，例如：200707，200808
	public static final String DATE_FORMAT_YYYYMM = "yyyy-MM";
	// 日期格式，年月日，例如：20050630，20080808
	public static final String DATE_FORMAT_YYYYMMDD = "yyyyMMdd";
	// 日期格式，年月日，用横杠分开，例如：2006-12-25，2008-08-08
	public static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";
	// 日期格式，年月日时分秒，例如：20001230120000，20080808200808
	public static final String DATE_TIME_FORMAT_YYYYMMDDHHMISS = "yyyyMMddHHmmss";
	// 时间格式，时分秒，例如：12:00:00，20:08:08
	public static final String DATE_TIME_FORMAT_HHMISS = "HH:mm:ss";
	// 日期格式，年月日时分秒，年月日用横杠分开，时分秒用冒号分开，
	// 例如：2005-05-10 23：20：00，2008-08-08 20:08:08
	public static final String DATE_TIME_FORMAT_YYYY_MM_DD_HH_MI_SS = "yyyy-MM-dd HH:mm:ss";
	/**
	 * 字符串转换为日期
	 * 
	 * @param String
	 *            strDate：日期的字符串形式
	 * @param String
	 *            format：转换格式
	 * @return String
	 * @throws
	 */
	public static Date strToDate(String strDate, String format) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		Date date = null;
		try {
			date = dateFormat.parse(strDate);
		} catch (ParseException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			// //System.out.println(e.getMessage());
		}
		return date;
	}
	/**
	 * 字符串转换为日期时间
	 * 
	 * @param String
	 *            strDateTime：日期时间的字符串形式
	 * @param String
	 *            format：转换格式
	 * @return String
	 * @throws
	 */
	public static Date strToDateTime(String strDateTime, String fromat) {
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat(fromat);
		Date dateTime = null;
		try {
			dateTime = dateTimeFormat.parse(strDateTime);
		} catch (ParseException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			// //System.out.println(e.getMessage());
		}
		return dateTime;
	}
	/**
	 * 日期转换为字符串
	 * 
	 * @param Date
	 *            date：需要转换的日期
	 * @param String
	 *            format：转换格式
	 * @return String
	 * @throws
	 */
	public static String dateToStr(Date date, String format) {
		String _timeZone = System.getProperty("user.timezone");
		TimeZone timeZone = null;
		if(_timeZone == null || "".equals(_timeZone) || "UTC".equals(_timeZone)){
			timeZone = TimeZone.getDefault();
		}else{
			timeZone = TimeZone.getTimeZone("Asia/Shanghai");
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		dateFormat.setTimeZone(timeZone);
		return dateFormat.format(date);
	}
	/**
	 * 日期时间转换为字符串
	 * 
	 * @param Date
	 *            date：需要转换的日期
	 * @param String
	 *            format：转换格式
	 * @return String
	 * @throws
	 */
	public static String dateTimeToStr(Date date, String format) {
		String _timeZone = System.getProperty("user.timezone");
		TimeZone timeZone = null;
		if(_timeZone == null || "".equals(_timeZone) || "UTC".equals(_timeZone)){
			timeZone = TimeZone.getDefault();
		}else{
			timeZone = TimeZone.getTimeZone("Asia/Shanghai");
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		dateFormat.setTimeZone(timeZone);
		return dateFormat.format(date);
	}
	/**
	 * 得到当天的最后时间,today是字符串类型"yyyy-mm-dd", 返回是日期类型"yyyy-mm-dd 23:59:59"
	 * 
	 * @param String
	 *            today
	 * @return Date
	 * @throws
	 */
	public static Date getTodayLastTime(String today) {
		String todayLastTime = today + " 23:59:59";
		return strToDateTime(todayLastTime, SdsDateUtil.DATE_TIME_FORMAT_YYYY_MM_DD_HH_MI_SS);
	}
	/**
	 * dateStr是字符串类型"yyyymmddhhmiss", 返回是日期类型"yyyy年mm月dd日,hh时mi分ss秒"
	 * 
	 * @param String
	 *            dateStr
	 * @return String
	 * @throws
	 */
	public static String timeStrToTimeCNFormat(String dateStr) {
		String y = dateStr.substring(0, 4) + "年";
		String m = dateStr.substring(4, 6) + "月";
		String d = dateStr.substring(6, 8) + "日";
		String h = dateStr.substring(8, 10) + "时";
		String i = dateStr.substring(10, 12) + "分";
		String s = dateStr.substring(12, 14) + "秒";
		return (y + m + d + h + i + s);
	}
	/**
	 * dateStr是字符串类型"yyyymmddhhmiss", 返回是日期类型"yyyy年mm月dd日"
	 * 
	 * @param String
	 *            dateStr
	 * @return String
	 * @throws
	 */
	public static String timeStrToDateCNFormat(String dateStr) {
		String y = dateStr.substring(0, 4) + "年";
		String m = dateStr.substring(4, 6) + "月";
		String d = dateStr.substring(6, 8) + "日";
		return (y + m + d);
	}
	/**
	 * 两个时间之间的天数
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static float getDaysBy2Time(String date1, String date2, String format) {
		float day = 0;
		if (date1 == null || date1.equals("")) {
			return 0;
		}
		if (date2 == null || date2.equals("")) {
			return 0;
		}
		// 转换为标准时间
		SimpleDateFormat myFormatter = new SimpleDateFormat(format);
		java.util.Date date = null;
		java.util.Date mydate = null;
		try {
			date = myFormatter.parse(date1);
			mydate = myFormatter.parse(date2);
			day = (date.getTime() - mydate.getTime()) / (24 * 60 * 60 * 1000);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		return day;
	}
	/**
	 * 两个时间之间的秒数
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static Integer getSecondsBy2Time(String date1, String date2, String format) {
		Integer seconds = null;
		if (date1 == null || date1.equals("")) {
			return 0;
		}
		if (date2 == null || date2.equals("")) {
			return 0;
		}
		// 转换为标准时间
		SimpleDateFormat myFormatter = new SimpleDateFormat(format);
		java.util.Date date = null;
		java.util.Date mydate = null;
		try {
			date = myFormatter.parse(date1);
			mydate = myFormatter.parse(date2);
			seconds = (int) (date.getTime() - mydate.getTime()) / 1000;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		return seconds;
	}
	public static Date GMTStringToData(String strgmt) {
		if (strgmt != null && strgmt.length() > 0) {
			try {
				SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String oldtime = strgmt.replace('T', ' ').substring(0, strgmt.indexOf('+'));
				Date sourceDate = sf.parse(oldtime);
				return sourceDate;
			} catch (ParseException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return null;
	}
	public static String GMTStringToDataString(String strgmt) {
		if (strgmt != null && strgmt.length() > 0) {
			try {
				String oldtime = strgmt.replace('T', ' ').substring(0, strgmt.indexOf('+'));
				return oldtime;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return null;
	}
	public static String minusTime(Date startDate, Date endDate) {
		if (null == startDate || null == endDate) {
			return "0";
		}
		long startTime = startDate.getTime();
		long endTime = endDate.getTime();
		if (endTime < startTime) {
			return "0";
		}
		long between = endTime - startTime;
		//long dayTime = between / (24 * 60 * 60 * 1000);
		long hourTime = (between / (60 * 60 * 1000));
		long minuteTime = ((between / (60 * 1000)) -  hourTime * 60);
		long secondTime = (between / 1000 - hourTime * 60 * 60 - minuteTime * 60);
		String spendTime = formatTime(String.valueOf(hourTime)) + ":" + formatTime(String.valueOf(minuteTime)) + ":"
				+ formatTime(String.valueOf(secondTime));
		return spendTime;
	}
	public static String getIntervalIime(long between) {
		long dayTime = between / (24 * 60 * 60 * 1000);
		long hourTime = (between / (60 * 60 * 1000) - dayTime * 24);
		long minuteTime = ((between / (60 * 1000)) - dayTime * 24 * 60 - hourTime * 60);
		long secondTime = (between / 1000 - dayTime * 24 * 60 * 60 - hourTime * 60 * 60 - minuteTime * 60);
		String spendTime = String.valueOf(dayTime) + " 天" + formatTime(String.valueOf(hourTime)) + " 小时" + formatTime(String.valueOf(minuteTime)) + " 分钟"
				+ formatTime(String.valueOf(secondTime))+" 秒";
		return spendTime;
	}
	
	public static String getIntervalIimeByHour(long between) {
		long hourTime = (between / (60 * 60 * 1000));
		long minuteTime = ((between / (60 * 1000)) - hourTime * 60);
		long secondTime = (between / 1000 - hourTime * 60 * 60 - minuteTime * 60);
		String spendTime = formatTime(String.valueOf(hourTime)) + "小时" + formatTime(String.valueOf(minuteTime)) + "分钟"
				+ formatTime(String.valueOf(secondTime))+"秒";
		return spendTime;
	}
	
	public static int compare_date(String DATE1, String DATE2) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		if (null == DATE2) {
			return 3;
		}
		try {
			Date dt1 = df.parse(DATE1);
			Date dt2 = df.parse(DATE2);
			if (dt1.getTime() > dt2.getTime()) {
				// System.out.println("dt1 在dt2前");
				return 1;
			} else if (dt1.getTime() < dt2.getTime()) {
				// System.out.println("dt1在dt2后");
				return -1;
			} else {
				return 0;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			logger.error(exception.getMessage(), exception);
		}
		return 3;
	}
	
	public static int compare_date(Date dt1, Date dt2) {
		try {
			if (dt1.getTime() > dt2.getTime()) {
				// System.out.println("dt1 在dt2前");
				return 1;
			} else if (dt1.getTime() < dt2.getTime()) {
				// System.out.println("dt1在dt2后");
				return -1;
			} else {
				return 0;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			logger.error(exception.getMessage(), exception);
		}
		return 3;
	}
	
	
	private static String formatTime(String timeStr) {
		if (timeStr.length() == 1) {
			timeStr = "0" + timeStr;
		}
		return timeStr;
	}
	
	/** 
	  * @param String sourceTime 待转化的时间 
	  * @param String dataFormat 日期的组织形式 
	  * @return long 当前时间的长整型格式,如 1247771400000 
	  */  
	public static long string2long(String sourceTime,String dataFormat) {
	    long longTime = 0L;
	    DateFormat f = new SimpleDateFormat(dataFormat);
	    Date d = null;
	    try {
	        d = f.parse(sourceTime);
	    } catch (ParseException e) {
	        e.printStackTrace();
	    }
	    if (null == d)
	    	longTime = 0;
	    else
	    	longTime = d.getTime();
	    
	    return longTime;
	}
	
	/** 
	  * 长整型转换为日期类型
	  * @param long longTime 长整型时间
	  * @param String dataFormat 时间格式
	  * @return String 长整型对应的格式的时间
	  */
	public static String long2String(long longTime,String dataFormat) {
	     Date d = new Date(longTime);
	     SimpleDateFormat s = new SimpleDateFormat(dataFormat); 
	     String str = s.format(d);

	     return str;
	}
	
	
	/**
	 * 计算两个指定日期之间的所有日期
	 * @param date1 起始日期
	 * @param date2 结束日期
	 */
	public static List<String> selectDate(String date1, String date2) {
		List<String> list = new ArrayList<String>();
		if (date1.equals(date2)) {
			list.add(date1);
			logger.info("两个日期相等!");
			return list;
		}

		String tmp;
		if (date1.compareTo(date2) > 0) { // 确保 date1的日期不晚于date2
			logger.error(date1 + "大于" + date2 + "，请确保选择时间的有效性"); 
			return list;
		}
		
		list.add(date1);
		SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_YYYY_MM_DD);
		tmp = format.format(str2Date(date1).getTime() + 3600 * 24 * 1000);

		int num = 0;
		while (tmp.compareTo(date2) < 0) {
			list.add(tmp);
			num++;
			tmp = format.format(str2Date(tmp).getTime() + 3600 * 24 * 1000);
		}
		
		if (num == 0) {
			logger.info("两个日期相邻!");
			list.add(date1);
			list.add(date2);
		}
		list.add(date2);
		return list;
	}

	private static Date str2Date(String str) {
		SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_YYYY_MM_DD);
		if (str == null)
			return null;

		try {
			return format.parse(str);
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	/**
	 * 将HH:mm:ss格式的时间转换为毫秒
	 * @param time
	 * @return
	 */
	public static long timeToSeconds(String time){
		String[] strs = time.split(":");
		long seconds = Long.valueOf(strs[0]) * 60 * 60 * 1000 + Long.valueOf(strs[1]) * 60 * 1000 + Long.valueOf(strs[2]) * 1000; 
		return seconds;
	}

}
