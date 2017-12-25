package com.ifeng.liulu.util;

import java.net.URLEncoder;

import com.ifeng.liulu.util.LoadConfig;



/**
 * 
 * <PRE>
 * 作用 : 该类作为程序内发送邮件的封装类，可供公共调用
 *   
 * 使用 : 使用运维平台公共发送邮件接口：http://rtd.ifeng.com:5001/rotdam
 * 	
 * 		curl -X POST --connect-timeout 10 --data "fn=CMSMail&args.ars=caoyang@ifeng.com&args.txt=%3Ch1%3Ejkahdjfalfdjsak%3C%2Fh1%3E&args.sub=ceshi" 
	 * "http://rtd.ifeng.com:5001/rotdam"
	 * 须传入发送人args_ars，发送内容args_txt，发送主题args_sub
	 * 发送多人 ：  “,”号urlEncode
 *   
 * 示例 :
 *   
 * 注意 :
 *   
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015-12-4        zhangyang6          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class SendWebMail {
	
	/**
	 * 程序内部向外发送邮件
	 * 注意:正文应当组装为html格式
	 * @param args_ars
	 * @param args_txt
	 * @param args_sub
	 */
	public static void sendMail(String args_ars,String args_txt,String args_sub,String logErrorAlarmURL){
		if(args_ars==null || args_txt==null || args_sub==null||logErrorAlarmURL==null){
			return;
		}
		try {
			String post="fn=CMSMail&args.ars="+args_ars.replace(",",URLEncoder.encode(","))+"&args.txt="+URLEncoder.encode(args_txt, "utf-8")
					+"&args.sub="+args_sub;
			HttpUtils.doPostDefault(logErrorAlarmURL, post, 100000, 100000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		//发送人
		String args_ars = "liulu5@ifeng.com";
		//发送正文
		String args_txt = "发送测试<br/>今天天气不错。";
		//发送主题
		String args_sub = "发送测试ing";
		
		String logErrorAlarmURL=LoadConfig.lookUpValueByKey("log_error_alarm_url");
		
		sendMail(args_ars, args_txt, args_sub,logErrorAlarmURL);
	}
}
