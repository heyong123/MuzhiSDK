package com.ddo.client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
/**
 * 发送Http 请求
 * 
 * @author felix
 * @date 2016年1月21日 下午3:06:30
 */
public class HttpUtil {
	
	public static HttpResult get(String url,String... urlParams) {
		return request(url, "GET", "", urlParams);
	}
	
	
	public static HttpResult post(String url,String requestBody) {
		return request(url, "POST", requestBody);
	}
	
	
	/**
	 * 
	 * @author felix  @date 2015-6-16 下午7:43:39
	 * @param url
	 * @param method
	 * @param requestBody
	 * @param urlParams
	 * url 替换规则   urlParams ： aaa,bbb,ccc 三个参数
	 * http://www.baidu.com/{company}/{app}/{company}
	 * @return
	 */
	private  static HttpResult request(String url, String method,String requestBody,String... urlParams) {
		StringBuffer bufferRes = new StringBuffer();
		HttpURLConnection conn = null;
		try {
			URL realUrl = new URL(replaceURL(url, urlParams));
			conn = (HttpURLConnection) realUrl.openConnection();
			// 连接超时
			conn.setConnectTimeout(25000);
			// 读取超时 --服务器响应比较慢,增大时间
			conn.setReadTimeout(25000);
			HttpURLConnection.setFollowRedirects(true);
			conn.setInstanceFollowRedirects(false);
			// 请求方式
			conn.setRequestMethod(method);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:21.0) Gecko/20100101 Firefox/21.0");
			conn.connect();
			// 获取URLConnection对象对应的输出流
			OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
			//发送请求参数
			out.write(requestBody);
			out.flush();
			out.close();
			InputStream in = conn.getInputStream();
			BufferedReader read = new BufferedReader(new InputStreamReader(in,"UTF-8"));
			String valueString = null;
			while ((valueString = read.readLine()) != null) {
				bufferRes.append(valueString);
			}
			in.close();
			
			HttpResult result = new HttpResult(getResponseHeader(conn), bufferRes.toString());
			
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return null;
	}
	
	
    /**
     * 
     * 
     * @author felix  @date 2016年1月21日 下午2:38:07
     * @param http
     * @return
     */
    private static Map<String, String> getResponseHeader(HttpURLConnection http)  {
        Map<String, String> header = new HashMap<String, String>();
        for (int i = 0;; i++) {
            String mine = http.getHeaderField(i);
            if (mine == null)
                break;
            header.put(http.getHeaderFieldKey(i), mine);
        }
        return header;
    }
	
	
	/**
	 * 更换url
	 * @param url
	 * @param urlParams
	 * @return
	 */
	public static String replaceURL(String url ,String... urlParams){
		if(urlParams == null){
			return url;
		}
		for(String param : urlParams){
			url = url.replaceFirst("\\{.*?\\}", param);
		}
		return url;
	}
	
	
	/**
	 * 
	 * 
	 * @author felix
	 * @date 2016年1月21日 下午2:34:35
	 */
	public static class HttpResult{
		
		private Map<String,String> header;
		private String body;
		
		
		
		public HttpResult(Map<String,String> header, String body){
			this.header = header;
			this.body = body;
		}
		
		public Map<String, String> getHeader() {
			return header;
		}
		public void setHeader(Map<String, String> header) {
			this.header = header;
		}
		public String getBody() {
			return body;
		}
		public void setBody(String body) {
			this.body = body;
		}
		
		@Override
		public String toString(){
			StringBuilder sb  = new StringBuilder();
			for (Map.Entry<String, String> entry : header.entrySet()) {
				String key = entry.getKey() != null ? entry.getKey() + ":" : "";
	            sb.append(key  + entry.getValue()+"\n");
	        }
			sb.append("\n\n");
			sb.append(body);
			return sb.toString();
		}
		
	}

}


