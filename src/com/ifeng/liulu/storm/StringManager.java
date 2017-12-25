package com.ifeng.liulu.storm;

public class StringManager {
	public static String getContentId(String value,String action_type)
	{//id=cmpp_010230042995221_3$ref=sy$type=pic$reftype=editor$rnum=125$showtype=hdpic
		if(action_type=="page")
		{
			String art_id = "";
			try{
				int b = value.indexOf("id=");
				if (b >=0 ) {
					int e = value.indexOf("$", b);
					if(e > b){
						art_id = value.substring(b+3,e);
					}else{
						art_id = value.substring(b+3);
					}
				}
			
				if(art_id.matches(".*_.*_.*")) {  //有分�?
					b = art_id.indexOf("_", 6);
					art_id = art_id.substring(0, b);
				}
				
				if(art_id.startsWith("video_"))
					art_id = art_id.substring(6);
			}catch (StringIndexOutOfBoundsException e){
				return art_id;  //存在如：i_crc_1878966724
			}
			return art_id;
		}
		else {
			String art_id = "";
			try{
				int b = value.indexOf("pinfo=");
				if (b >=0 ) {
					int e = value.indexOf(":", b);
					if(e > b){
						art_id = value.substring(b+6,e);
					}else{
						art_id = value.substring(b+6);
					}
				}
			
				if(art_id.matches(".*_.*_.*")) {  //有分�?
					b = art_id.indexOf("_", 6);
					art_id = art_id.substring(0, b);
				}
				
				if(art_id.startsWith("video_"))
					art_id = art_id.substring(6);
			}catch (StringIndexOutOfBoundsException e){
				return art_id;  //存在如：i_crc_1878966724
			}
			return art_id;
		}
	}
	public static void main(String[] args)
	{
		String content="id=cmpp_030240050913400$ref=sy$type=pic$simid=clusterId_19101308$reftyp";
		System.out.println(getContentId(content,"page"));
	
	}
	public static String getAbs(String floatNumber)
	{
		if(floatNumber.contains("-"))//值是负表示经常出�?
		{
			return floatNumber.substring(1);
		}
		return floatNumber;
	}
}
