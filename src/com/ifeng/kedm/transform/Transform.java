package com.ifeng.kedm.transform;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Transform {
	
	 public List<String>  appList = new ArrayList<String>();
	
	 public static void main(String[] args) throws Exception {
		
	 Transform test1 = new Transform();
         String strs2 = "\"171.82.155.121\",-,[11/Jan/2017:11:07:51 +0800],\"POST /appsta.js HTTP/1.1?session=2017-01-11%2B11%3A07%3A11%23pageinfo%23pinfo%3Dimcp_crc_338046424%3A163%3Aeditor%3Asy%24rToken%3D%402017-01-11%2B11%3A07%3A16%23pageinfo%23pinfo%3Dimcp_117678610%3A164%3Aai%3Asy%24rToken%3D14841039234019274%402017-01-11%2B11%3A07%3A16%23pageinfo%23pinfo%3Dimcp_117693834%3A165%3Aai%3Asy%24rToken%3D14841039234011815%402017-01-11%2B11%3A07%3A18%23pageinfo%23pinfo%3Dimcp_117672604%3A166%3Aai%3Asy%24rToken%3D14841039234018604%402017-01-11%2B11%3A07%3A19%23pageinfo%23pinfo%3Dimcp_117700031%3A167%3Aai%3Asy%24rToken%3D14841039234012506%402017-01-11%2B11%3A07%3A21%23pageinfo%23pinfo%3Dimcp_117692036%3A168%3Aai%3Asy%24rToken%3D14841039234012087%402017-01-11%2B11%3A07%3A23%23pageinfo%23pinfo%3Dimcp_117129018%3A169%3Aai%3Asy%24rToken%3D14841039234017312%402017-01-11%2B11%3A07%3A23%23pageinfo%23pinfo%3Dimcp_117472057%3A170%3Aai%3Asy%24rToken%3D14841039234018068%402017-01-11%2B11%3A07%3A32%23pageinfo%23pinfo%3Dcmpp_050980015134269%3A171%3Aai%3Asy%24rToken%3D14841040513378445%402017-01-11%2B11%3A07%3A32%23pageinfo%23pinfo%3Dimcp_117665136%3A172%3Aai%3Asy%24rToken%3D14841040513376779%402017-01-11%2B11%3A07%3A32%23pageinfo%23pinfo%3Dimcp_117654572%3A173%3Aai%3Asy%24rToken%3D14841040513377457%402017-01-11%2B11%3A07%3A33%23pageinfo%23pinfo%3Dimcp_117665172%3A174%3Aai%3Asy%24rToken%3D14841040513373035%402017-01-11%2B11%3A07%3A33%23pageinfo%23pinfo%3Dimcp_117638210%3A175%3Aai%3Asy%24rToken%3D14841040513371629%402017-01-11%2B11%3A07%3A42%23pageinfo%23pinfo%3Dimcp_crc_4005031379%3A176%3Aeditor%3Asy%24rToken%3D%402017-01-11%2B11%3A07%3A44%23pageinfo%23pinfo%3Dcmpp_040710044528424%3A177%3Aai%3Asy%24rToken%3D14841040513375050%402017-01-11%2B11%3A07%3A44%23pageinfo%23pinfo%3Dimcp_116090871%3A178%3Aai%3Asy%24rToken%3D14841040513377639%402017-01-11%2B11%3A07%3A45%23pageinfo%23pinfo%3Dimcp_116504195%3A179%3Aai%3Asy%24rToken%3D14841040513371013%402017-01-11%2B11%3A07%3A47%23pageinfo%23pinfo%3Dcmpp_030200050551265%3A180%3Aai%3Asy%24rToken%3D14841040674087624%402017-01-11%2B11%3A07%3A47%23pageinfo%23pinfo%3Dimcp_crc_1135132217%3A181%3Aeditor%3Asy%24rToken%3D&datatype=newsapp&idfa=f0ba1a520642407fa85ac4fb8885a88b&isupdate=1&logintime=1434199470&mos=iphone_9.3.5&publishid=4002&softversion=5.4.1&ua=iphone7_2&uname=hui%E7%9A%84%20iPhone&userkey=42b6836bb7fca81c7aff9d492dd4581655e1b93b&net=4g\"200 0,\"-\",\"IfengNews/5.4.1 (iPhone; iOS 9.3.5; Scale/2.00)\" 10.90.2.152";
	 String strs = "223.104.3.224,-,[21/Dec/2016:20:37:41 +0800],\"GET /appsta.js?session=2016-12-21%2B20%3A37%3A41%23action%23type=cdrtt%24id=sy%24pty=ch&datatype=newsapp&mos=iphone_10.2&softversion=5.4.0&publishid=4002&userkey=fb60742f3511417f9c9714e5c5ffb9c2&ua=iphone7_1&logintime=1380771474&isupdate=1&uname=neiliphone&idfa=5ff3690bda304e91b216b6a10bdcc37f&net=4g HTTP/1.1\",204 0,\"-\",\"%E5%87%A4%E5%87%B0%E6%96%B0%E9%97%BB/5.4.0.2 CFNetwork/808.2.16 Darwin/16.3.0\" \"-\"";
	String str1 = "110.229.96.130,-,[23/Nov/2016:17:55:01 +0800],\"GET /appsta.js?datatype=newsapp&mos=android_5.1.1&softversion=5.3.2&publishid=6101&userkey=863042035041050&ua=vivo_v3max_a&logintime=1479644962&isupdate=0&md5=9D:9"
		+"5:E8:91:FA:60:7E:92:68:B9:1A:8C:3D:D5:07:2B&sha1=F8:68:B2:50:9E:DA:DF:BE:7E:C0:24:20:5A:DE:6A:CA:61:8B:23:B9&session=2016-11-23%2B17%3A40%3A07%23action%23type%3Dupscreen%24psnum%3D1%24id%3Dcmd%402016-11-23%2B17"
		+"%3A40%3A02%23action%23type%3Dupscreen%24psnum%3D1%24id%3Dsy%402016-11-23%2B17%3A40%3A02%23action%23type%3Dupscreen%24psnum%3D1%24id%3Drcmd&sver=V1&sig=4bc46kdbo1vc4n983f0ck3t24ij44kgh3bt733f2ob0442r HTTP/1.1\",2"
		+"04 0,\"-\",\"Dalvik/2.1.0 (Linux; U; Android 5.1.1; vivo V3Max A Build/LMY47V)\" \"-\"";
		
		try {
			
//			while(true){
//				String listToStr = test1.listToStr(str1);
//				System.out.println(listToStr);
//				System.out.println(listToStr.length());
//				Thread.sleep(10);
//			}
//
            long s = System.currentTimeMillis();
            String tests = test1.listToStr(strs2);
            System.out.println(tests);
            System.out.println(System.currentTimeMillis()-s);
			File file = new File("D:\\test_1124\\test_1124_peter2.log");
			File file1 = new File("D:\\test_1124\\jiexi_test_1124_peter2.log");
			BufferedReader bfr = new BufferedReader(new FileReader(file));
			BufferedWriter bfw = new BufferedWriter(new FileWriter(file1));
			String line = null;
			while((line=bfr.readLine())!=null){
				String listToStr = test1.listToStr(line.toString());
				System.out.println(listToStr);
				bfw.write(listToStr);
				bfw.flush();
				bfw.newLine();
			}
			
			bfr.close();
			bfw.close();
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}


	 /**
	  * 入口方法
	  * @param strs 输入原始日志字符
	  * @return 返回解析后的日志字符
	  * @throws Throwable 抛解析异�?
	  */

	public String listToStr(String strs) throws Throwable {
		
		StringBuilder totalBuildr =  new StringBuilder();
		List<String> appStr = getAppStr(strs);
		for (int i = 0; i < appStr.size(); i++) {
			if (i != appStr.size()-1) {
				totalBuildr.append(appStr.get(i)+"\n");
			}else {
				totalBuildr.append(appStr.get(i));
			}
		}
		
		String totalStr = totalBuildr.toString();
		return totalStr; 
	}

	
	
	/**
	 * 
	 * @param str1 传入原始日志字符�?
	 * @return	返回解析后的日志list
	 * @throws Throwable
	 */
	public List<String> getAppStr(String str1) throws Throwable {
		List<String> appTempList = new ArrayList<String>();
		List<String> wrongList = new ArrayList<String>();
		List<String> tupleList = new ArrayList<String>();
		List<String> resultList = new ArrayList<String>();
		StringBuilder resultBuildr =  new StringBuilder();
		
		String ip = "";
		String req = "";
		String content = "";
		String valueStr = "";
		String stm = "";
		String sid = "";
		String stxt = "";
		Map<String, String> splitUrl;
		
		
//		对body字符串进行第1次切�?
		
		String[] bodySplit = str1.split(",");
		
		if (bodySplit.length < 6) {
			return null;
		}

        ip = bodySplit[0];
        req = bodySplit[3];
        /*if(str1.contains("POST")){
            String[] tempReq = bodySplit[5].split("\" \"");
            ip = tempReq[1].replace("\"","");
        }*/
		splitUrl = splitUrl(req);
		addElements();
		for (Map.Entry<String,String> bodyMap1 : splitUrl.entrySet()) {
			if (!appList.contains(bodyMap1.getKey())) {
				appTempList.add(bodyMap1.getKey()+"="+bodyMap1.getValue());
			}
		}
		for (int i  = 0; i < appTempList.size(); i++) {
			if (i!=appTempList.size()-1) {
				resultBuildr.append(appTempList.get(i)+"&");
			}else {
				resultBuildr.append(appTempList.get(i));
			}
		}
		 
		if ((splitUrl.get("datatype").equals("videoapp") && splitUrl.get("mos").contains("android") && compareStrs(splitUrl.get("softversion"), "6.6.1"))
			|| (splitUrl.get("datatype").equals("newsapp") && splitUrl.get("mos").contains("android") && compareStrs(splitUrl.get("softversion"), "4.1.1")) 
			|| (splitUrl.get("datatype").equals("fmapp") && splitUrl.get("mos").contains("android") && compareStrs(splitUrl.get("softversion"), "5.3.1"))) {
			
			if (str1.contains("GET") && ((!splitUrl.containsKey("sig")) || (!splitUrl.containsKey("sver"))) ) {
				wrongList.add("wrong");
				return wrongList;
			}
			
			String sigString = splitUrl.get("sig");
			
			String[] sigDecodeByte = new String[100];
			String concatSSS = null;
			
			try {
				//Base32解密
				byte[] sigDecode = Base32.decode(sigString);
				String sigDecodeStr = new String(sigDecode);
				
				sigDecodeByte = sigDecodeStr.toString().split("_");
				
				if (req.contains("?")) {
					content = req.toString().split("\\?")[1].split("&sver")[0];
				}else {
					content = req.split("&sver")[0];
				}
				concatSSS = content.concat(sigDecodeByte[0]);
				
				//Sha1加密
				Charset cs = Charset.forName("UTF-8");
				byte[] data = concatSSS.getBytes(cs);
				byte[] hash = Sha1.encode(data);
				String hex = Sha1.byteArrayToString(hash,hash.length);
				
				if (!hex.equals(sigDecodeByte[1])) {
					wrongList.add("wrong");
					return wrongList;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		if (resultBuildr.length() > 0) {
			splitUrl.put("etc", resultBuildr.toString());
		}
		
		splitUrl.put("ip", ip);
		String sessionStr = splitUrl.get("session");
		String urlDecoderStr = UrlDecode.getURLDecoderString(sessionStr).replaceAll("$#chid", "$chid");
		splitUrl.put("session", urlDecoderStr);
		
		for (String appStr : appList) {
			if (appStr!= "sver" && appStr != "sig") {
				if (splitUrl.containsKey(appStr)) {
					valueStr = splitUrl.get(appStr);
				}else {
					valueStr = "#";
				}
				
				if (appStr == "ua") {
					valueStr = UrlDecode.getURLDecoderString(valueStr);
				}
				
				if (appStr != "session") {
					tupleList.add(valueStr);
				}
			}
		}
		String baseinfo = "";
        for(String s : tupleList){
            baseinfo = baseinfo + s + "\t";
        }
		appList.clear();
		String[] sessionSplit = urlDecoderStr.split("@");
		
		
		for (String se : sessionSplit) {
			String[] seSplit = se.split("#");
			
			if (seSplit.length >= 3) {
				stm = seSplit[0];
				sid = seSplit[1];
				stxt = seSplit[2];
			}
			
			if (stxt.length() == 0) {
				stxt = "#";
			}
			String item = baseinfo + stm + "\t" + sid + "\t" + stxt;
			resultList.add(item);
		}
		
		return resultList;
	}



	/**
	 * 切割url，得到对应的键�?�对
	 * @return 切割后的键�?�对
	 */
	
	public  Map<String, String> splitUrl(String req) {
		Map<String, String> bodyMap = new HashMap<String, String>();
		int i = 0;
        req = req.replace("\"","");
        if(req.contains("?")){
            req = req.split("\\?")[1];
        }
        String[] temp = req.split("&");
        for(String info : temp){
            try{
                if(info.contains("session")){
                    String v = info.replace("session=","");
                    bodyMap.put("session",v);
                }else{
                    String[] kv = info.split("=");
                    if(null == kv[1]){
                        bodyMap.put(kv[0],"#");
                    }else if(kv[1].split(" ").length > 1){  //兼容格式异常
                        bodyMap.put(kv[0],kv[1].split(" ")[0]);
                        if(kv[0].equals("net") && kv[1].indexOf("200") > 0){
                            bodyMap.put(kv[0],kv[1].substring(0,kv[1].indexOf("200")));
                        }
                    }else if(kv[0].equals("net") && kv[1].indexOf("200") > 0){ //兼容格式异常
                        bodyMap.put(kv[0],kv[1].substring(0,kv[1].indexOf("200")));
                    }else{
                        bodyMap.put(kv[0],kv[1]);
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }

        }
        /*String[] content = new String[50];
		String strNew = req.replaceAll("&", "&&");
        Pattern pattern = Pattern.compile("[&?]+(.*?)(?=&|\\s|$)");
        Matcher m = pattern.matcher(strNew);
        while(m.find()){
            content[i]=m.group(1);
            i=i+1;           
        }
        
		for (String context : content) {
			if (context!=null) {
				String[] mapStrs = context.split("=");
				try {
					if (null == mapStrs[1]) {
						bodyMap.put(mapStrs[0], "#");
					}else {
						bodyMap.put(mapStrs[0], mapStrs[1]);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}*/

		return bodyMap;
	}
	
	
	/**
	 * 给appList添加元素
	 */
	public  void addElements() {
		appList.add("datatype");
		appList.add("ip");
		appList.add("mos");
		appList.add("softversion");
		appList.add("publishid");
		appList.add("userkey");
		appList.add("ua");
		appList.add("net");
		appList.add("logintime");
		appList.add("session");
		appList.add("etc");
		appList.add("sig");
		appList.add("sver");
	}

	
	/**
	 * 比较两个数�?�型字符串大�?
	 * @param str1
	 * @param str2
	 * @return
	 */
	public boolean compareStrs(String str1,String str2){
		if (str1.compareTo(str2)>=0) {
			return true;
		}
		return false;
	}
}
