package com.ifeng.kedm.transform;

import java.io.UnsupportedEncodingException;
/**
 * url转码、解�??
 *
 * @author lifq 
 * @date 2015-3-17 下午04:09:35
 */
public class UrlDecode {
    private final static String ENCODE = "UTF-8";
    /**
     * URL 解码
     *
     * @return String
     * @author lifq
     * @date 2015-3-17 下午04:09:51
     */
    public static String getURLDecoderString(String str) {
        String result = "";
        if (null == str) {
            return "";
        }
        try {
            result = java.net.URLDecoder.decode(str, ENCODE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }
    /**
     * URL 转码
     *
     * @return String
     * @author lifq
     * @date 2015-3-17 下午04:10:28
     */
    public static String getURLEncoderString(String str) {
        String result = "";
        if (null == str) {
            return "";
        }
        try {
            result = java.net.URLEncoder.encode(str, ENCODE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 
     * @return void
     * @author lifq
     * @date 2015-3-17 下午04:09:16
     */
    public static void main(String[] args) {
        String str = "keywd%24sw%3D%E5%8D%8E%E5%9B%BD%E9%94%8B%402016-12-17%2B13%3A57%3A08%23";
//        System.out.println(getURLEncoderString(str));
        System.out.println(getURLDecoderString(str));
        
    }

}
