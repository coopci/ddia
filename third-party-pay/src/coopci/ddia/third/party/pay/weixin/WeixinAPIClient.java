package coopci.ddia.third.party.pay.weixin;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.concurrent.ThreadLocalRandom;

public class WeixinAPIClient {

	
	
	static public String syncGet(String url) throws ClientProtocolException, IOException {
		HttpGet httpGet = new HttpGet(url);
		CloseableHttpResponse response = null;
		CloseableHttpClient httpclient = HttpClients.createDefault();
        
		response = httpclient.execute(httpGet);
		HttpEntity entity = response.getEntity();
		//for (Header h : response.getAllHeaders()) {
		//	System.out.println(h.getName() + ": " + h.getValue());
		//}
		String resBody = EntityUtils.toString(entity, "utf-8");
		httpclient.close();
		return resBody;
		
	}
	
	static public String syncPost(String url, String content, String contentType) throws ClientProtocolException, IOException {
		HttpPost httpPost = new HttpPost(url);
		
		httpPost.setEntity(new ByteArrayEntity(content.getBytes()));
		
		if (contentType !=null) {
			httpPost.setHeader("Content-Type", contentType);
		}
		
		CloseableHttpResponse response = null;
		CloseableHttpClient httpclient = HttpClients.createDefault();
        
		response = httpclient.execute(httpPost);
		HttpEntity entity = response.getEntity();
		//for (Header h : response.getAllHeaders()) {
		//	System.out.println(h.getName() + ": " + h.getValue());
		//}
		String resBody = EntityUtils.toString(entity, "utf-8");
		httpclient.close();
		return resBody;
		
	}
	
	/**
	 * https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1419317851&token=&lang=zh_CN
	 * //访问  https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
		//
		//		"access_token":"ACCESS_TOKEN", 
		//		"expires_in":7200, 
		//		"refresh_token":"REFRESH_TOKEN",
		//		"openid":"OPENID", 
		//		"scope":"SCOPE" access_token
		//	
	 * */
	static public String syncGetAccessToken(String code, String appid, String appsecret) {
		String urlPrefix = "https://api.weixin.qq.com/sns/oauth2/access_token?grant_type=authorization_code";
		
		StringBuilder sb = new StringBuilder(urlPrefix);
		sb.append("&code=");
		sb.append(code);
		sb.append("&appid=");
		sb.append(appid);
		sb.append("&secret=");
		sb.append(appsecret);
		String url = sb.toString();
        try {
			String resBody = syncGet(url);
			// System.out.println(resBody);
			return resBody;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	/**
	 * https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1419317853&token=&lang=zh_CN
	 * 
		// 访问  https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID
		//
//		{ 
//			"openid":"OPENID",
//			"nickname":"NICKNAME",
//			"sex":1,
//			"province":"PROVINCE",
//			"city":"CITY",
//			"country":"COUNTRY",
//			"headimgurl": "http://wx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/0",
//			"privilege":[
//			"PRIVILEGE1", 
//			"PRIVILEGE2"
//			],
//			"unionid": " o6_bmasdasdsad6_2sgVt7hMZOPfL"
//
//			}
	 * */
	static public String syncGetUserInfo(String accessToken, String openid) {
		String urlPrefix = "https://api.weixin.qq.com/sns/userinfo?";
		StringBuilder sb = new StringBuilder(urlPrefix);
		sb.append("access_token=");
		sb.append(accessToken);
		sb.append("&openid=");
		sb.append(openid);
		String url = sb.toString();
        try {
			String resBody = syncGet(url);
			// System.out.println(resBody);
			return resBody;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	
	
	/**
	 * 
	 * https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=4_3
	 * https://pay.weixin.qq.com/wiki/tools/signverify/
	 * @param key  key设置路径：微信商户平(pay.weixin.qq.com)-->账户设置-->API安全-->密钥设置 
	 * @throws NoSuchAlgorithmException 
	 * 
	 * */
	static public String getSign(HashMap<String, String> params, String key) throws NoSuchAlgorithmException {
		String sign = "";
		
		ArrayList<String> paramNames = new ArrayList<String>(params.keySet());
		java.util.Collections.sort(paramNames);
		StringBuilder sb = new StringBuilder();
		for (String paraName : paramNames) {
			sb.append(paraName);
			sb.append("=");
			sb.append(params.get(paraName));
			sb.append("&");
		}
		sb.append("key=");
		sb.append(key);
		
		String stringSignTemp = sb.toString();
		
		
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		
		byte[] thedigest = md5.digest(stringSignTemp.getBytes());
		sign = coopci.ddia.util.Funcs.toHexString(thedigest).toUpperCase();
		return sign;	
	}
	
	
	
	static public String genReqXML(HashMap<String, String> params, String key) throws NoSuchAlgorithmException {
		StringBuilder sb = new StringBuilder();
		String sign = getSign(params, key);
		
		sb.append("<xml>\n");
		
		ArrayList<String> paramNames = new ArrayList<String>(params.keySet());
		java.util.Collections.sort(paramNames);
		for (String paraName : paramNames) {
			sb.append("<");
			sb.append(paraName);
			sb.append(">");
			sb.append(params.get(paraName));
			sb.append("</");
			sb.append(paraName);
			sb.append(">\n");
		}
		sb.append("<sign>");
		sb.append(sign);
		sb.append("</sign>\n");
		
		sb.append("</xml>");
		
		return sb.toString();	
	}
	
	
	
	static ThreadLocal<SimpleDateFormat> dt = new ThreadLocal<SimpleDateFormat>() {
	    @Override protected SimpleDateFormat initialValue() {
	        return new SimpleDateFormat("yyyyMMddHHmmss"); 
	    }
	};    
	static public String genOutTradeNo() {
		Date now = new Date();
		return dt.get().format(now);
	}
	
	final static String nonceAlphabet = "0123456789qwertyuuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";
	
	static public String genNonceStr(int length) {
		
		char[] text = new char[length];
	    for (int i = 0; i < length; i++)
	    {
	        text[i] = nonceAlphabet.charAt(ThreadLocalRandom.current().nextInt(nonceAlphabet.length()));
	    }
	    return new String(text);
		
	}
	
	
}
