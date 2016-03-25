package mobi.zty.sdk.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.sdk.game.Constants;
import mobi.zty.sdk.game.GameSDK;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import dalvik.system.DexClassLoader;

public class Helper {

	private static boolean isDeBuge = false;//默认 为false
	public static boolean isAppOnForeground(Context ctx) {
		ActivityManager activityManager = (ActivityManager) ctx
				.getApplicationContext()//
				.getSystemService(Context.ACTIVITY_SERVICE);
		String packageName = ctx.getApplicationContext().getPackageName();

		List<RunningAppProcessInfo> appProcesses = activityManager
				.getRunningAppProcesses();
		if (appProcesses == null) {
			return false;
		}

		for (RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.processName.equals(packageName)
					&& appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				return true;
			}
		}
		return false;
	}

	public static String getBaseVersion(Context context) {

		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(),
					0);
			String version = info.versionName;
			return version;
		} catch (Exception e) {
			e.printStackTrace();

		}
		return "0.0.0";
	}

	/**
	  * 获取现在时间
	  * 
	  * @return 返回时间类型 yyyyMMddHHmmss
	  */
	public static String getNowTime(String matchStr) {
	  Date currentTime = new Date();
	  SimpleDateFormat formatter = new SimpleDateFormat(matchStr);
	  String dateString = formatter.format(currentTime);
	  return dateString;
	}
	public static int getVersionCode(Context context) {
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(),
					0);
			int version = info.versionCode;
			return version;
		} catch (Exception e) {
			e.printStackTrace();

		}
		return 1;
	}

	public static int getScreenDensity(Context paramContext) {
		try {
			DisplayMetrics localDisplayMetrics = new DisplayMetrics();
			((WindowManager) paramContext.getSystemService("window"))
					.getDefaultDisplay().getMetrics(localDisplayMetrics);
			int density = localDisplayMetrics.densityDpi;
			return density;
		} catch (Exception localException) {
			localException.printStackTrace();
		}
		return 0;
	}

	/**
	 * context 杩欎釜涓�瀹氶挜鍖欎富activity涓殑涓婁笅鏂� (this)
	 * 
	 * @param context
	 * @return
	 */
	public static String getMainActivityName(Context context) {
		String classname = context.getClass().getName();
		return classname;
	}
	public static String getCarrierName(Context paramContext) {
		String str = "";
		try {
			str = ((TelephonyManager) paramContext.getSystemService("phone"))
					.getNetworkOperatorName();
			if (TextUtils.isEmpty(str)) {
				str = "";
			}
		} catch (Exception localException) {
			str = "";
		}
		return str;
	}

	/**
	 * 
	 * @param context
	 * @return联网方式： 0 代表非wifi(可能是gprs) 1代表wifi
	 */
	public static int getAccessPoint(Context context) {
		int type = 0;
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager != null) {
			NetworkInfo networkInfo = connectivityManager
					.getActiveNetworkInfo();
			if (networkInfo != null) {
				if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
					type = 1;
					return type;
				}
//				if (networkInfo.getExtraInfo() != null) {
//					return networkInfo.getExtraInfo().trim();
//				}
			}

		}
		return type;
	}

	public static boolean isDebugEnv() {
		return isDeBuge;
	}
	public static void setDebug(Boolean var){
		isDeBuge = var;
	}

	public static String getChannel(Activity activity, String name)//
	{

		String ret = "";
		InputStream is = activity.getClass().getResourceAsStream("/mmiap.xml");
		if (Helper.isDebugEnv() && is == null) {
			return "2200166013";
		}
		if (is != null) {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = null;

			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Document doc = null;

			try {
				doc = builder.parse(is);
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			doc.normalize();
			ret = doc.getElementsByTagName(name).item(0).getFirstChild()
					.getNodeValue();
		}
		return ret;
	}

	public static String getVesrionSdk() {
		return Build.VERSION.SDK;
	}

	public static String getMODEL() {
		return Build.MODEL;
	}

	/**
	 * 获取运营商类型 
	 * @param imsi
	 * @return 1移动 5联通10电信15支付宝
	 */
	public static int getSIMType(Context context)// mmpay_tcpay
	{
	    String simOp = getIMSIStart(context);
		int cardType = 0;
		if ((simOp != null) ) {
			if ((simOp.indexOf("46000") >= 0) || (simOp.indexOf("46002") >= 0) || 
					(simOp.indexOf("46007") >= 0)||(simOp.indexOf("46020") >= 0) ) {
				cardType = PayConfig.CMCC_MOBLE;
			} else if (simOp.equals("46001")||(simOp.indexOf("46006") >= 0) ) {
				cardType = PayConfig.UNICOM_MOBLE;

			} else if (simOp.equals("46003") || simOp.equals("46005")
					|| simOp.equals("46011"))// "46003", "46005", "46011"
			{
				cardType = PayConfig.TIANYI_MOBLE;
			} else {
				cardType = PayConfig.NO_THIS_PAY;
			}
		}else{
			cardType = PayConfig.NO_THIS_PAY;
		}
		return cardType;

	}

	public static String getReleaseVersion() {
		return Build.VERSION.RELEASE;
	}

	public static String getIMEI(Context context)// getIMEI
	{
		String ret = ((TelephonyManager) context.getSystemService("phone"))
				.getDeviceId();
		return ret;
	}

	/**
	 * 获取imsi卡 集成电路唯一标识
	 * @param context
	 * @return
	 */
	public static String getICCID(Context context)
	{
		String ret = ((TelephonyManager) context.getSystemService("phone"))
				.getSimSerialNumber();
		return ret;
	}
	public static String getIMSI(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getSubscriberId();
	}
	
	/**
	 * 获取imsi卡的 类型标识
	 * @param context
	 * @return
	 */
	public static String getIMSIStart(Context context){
		TelephonyManager telmanager = (TelephonyManager)context.getSystemService("phone");
	    String simOp = telmanager.getSimOperator();
	    return simOp;
	}
	
	
	public static  boolean isNetworkConnected(Context context) {  
	  if (context != null) {  
		  ConnectivityManager mConnectivityManager = (ConnectivityManager) context  
	                 .getSystemService(Context.CONNECTIVITY_SERVICE);  
	        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();  
		        if (mNetworkInfo != null) {  
	            return mNetworkInfo.isAvailable();  
	       }     
	  }  
	  return false;  
	}  
	public static String getIpAddress(Context context){
		if (getAccessPoint(context)==1) {//wifi
			return getWifiIpAddress(context);
		}else{
			return getGprsIpAddress();
		}
	}
	public static  String getGprsIpAddress() {   
        try {   
            for (Enumeration<NetworkInterface> en = NetworkInterface   
                    .getNetworkInterfaces(); en.hasMoreElements();) {   
                NetworkInterface intf = en.nextElement();   
                for (Enumeration<InetAddress> enumIpAddr = intf   
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {   
                    InetAddress inetAddress = enumIpAddr.nextElement();   
                    if (!inetAddress.isLoopbackAddress()) {   
                        return inetAddress.getHostAddress().toString();   
                    }   
                }   
            }   
        } catch (SocketException ex) {   
            Log.e("WifiPreference IpAddress", ex.toString());   
        }   
        return "";   
    }   
	
	 private static String intToIp(int i) {
		 return (i & 0xFF ) + "." +((i >> 8 ) & 0xFF) + "." + ((i >> 16 ) & 0xFF) + "." +( i >> 24 & 0xFF) ;
	 }
	 
	 public static String getWifiIpAddress(Context context) {   
	        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);   
	        WifiInfo info = wifi.getConnectionInfo();   
	        int ipAddress = info.getIpAddress(); 
	        
	        return intToIp(ipAddress);   
	    }
	
	/**
	 * sha1加密
	 * @param value
	 * @param key
	 * @param signtype
	 * @return
	 */
	public static byte[] hmac_sha1(String value, String key,String signtype) {
		try {
			byte[] keyBytes = key.getBytes();
			SecretKeySpec signingKey = new SecretKeySpec(keyBytes, signtype);

			Mac mac = Mac.getInstance(signtype);
			mac.init(signingKey);
			byte[] rawHmac = mac.doFinal(value.getBytes());
			return rawHmac;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * md5加密
	 * @param string
	 * @return
	 */
	public static String md5(String s) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			byte[] btInput = s.getBytes();
			// 获得MD5摘要算法的 MessageDigest 对象
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			// 使用指定的字节更新摘要
			mdInst.update(btInput);
			// 获得密文
			byte[] md = mdInst.digest();
			// 把密文转换成十六进制的字符串形式
			char[] str = encodeHex(md, hexDigits);

			return new String(str);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 将消息摘要转换成十六进制表示
	 * 
	 * @param data
	 * @param toDigits
	 * @return
	 */
	protected static char[] encodeHex(byte[] data, char[] toDigits) {
		int l = data.length;
		char[] out = new char[l << 1];
		// two characters form the hex value.
		for (int i = 0, j = 0; i < l; i++) {
			out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
			out[j++] = toDigits[0x0F & data[i]];
		}
		return out;
	}
	public static String getApplicationName(Context context) {
		PackageManager packageManager = null;
		ApplicationInfo applicationInfo = null;
		try {
			packageManager = context.getApplicationContext()
					.getPackageManager();
			applicationInfo = packageManager.getApplicationInfo(
					context.getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e) {
			applicationInfo = null;
		}
		String applicationName = (String) packageManager
				.getApplicationLabel(applicationInfo);
		return applicationName;
	}
	
	public static interface Callback {
		public void onResult(String info);
	}
	private static Map<String, Integer> orderStep = new HashMap<String, Integer>();
	
	private static void cleanOredStep(){
		if (orderStep.size()>20) {
			orderStep.clear();
		}
	}
	private synchronized static int getOrderStep(String orderId){
		if (orderStep.containsKey(orderId)) {
			orderStep.put(orderId,orderStep.get(orderId)+1);
		}else{
			cleanOredStep();
			orderStep.put(orderId, 1);
		}
		return orderStep.get(orderId);
	}
	public static synchronized void sendPayMessageToServer(final int mk,final String dec,int feeIndex){
		try {
			if (!(GameSDK.getInstance().getOrderId(feeIndex)!=null
					&&GameSDK.getInstance().mkMap.containsKey(mk)
					&&GameSDK.getInstance().mkMap.get(mk).needCount==1
					)) {
				return;
			}
			final String orderId = GameSDK.getInstance().getOrderId(feeIndex);
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						String url =  Constants.COUNT_URL+ "?mk=" + mk + "&step="
								+ getOrderStep(orderId) +"&dec=" + URLEncoder.encode(dec, "UTF-8") + 
								"&game_id="+ GameSDK.getInstance().gameId+
								"&parck_id="+ GameSDK.getInstance().packetId+
								"&order_id="+ orderId;
						String body = HttpRequestt.get(url).body();
						if (body==null) {
							
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	/**
	 * @param url
	 * @param imsi
	 * @return
	 */
	public static void httpGetPhonNum(CharSequence url, String imsi,final Callback cb) {
		final String urlstr = url+ "imsi=" + imsi;
		new Thread() {
			@Override
			public void run() {
				String result = null;
				HttpGet httpGet = new HttpGet(urlstr);
				BasicHttpParams httpParams = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParams,
						10 * 60 * 1000);
				HttpConnectionParams.setSoTimeout(httpParams, 10 * 60 * 1000);
				int step = 0;
				String mobile_num ="";
				while (mobile_num.equals("")&&step<20){
					try {
						Log.e("E支付的URL", urlstr);
						HttpClient client = new DefaultHttpClient(httpParams);
						HttpResponse httpResp = client.execute(httpGet);
						if (httpResp.getStatusLine().getStatusCode() == 200) {
							result = EntityUtils.toString(httpResp.getEntity(),
									"UTF-8");
							if(!(result.contains("DOCTYPE HTML")||result.contains("doctype html"))){
								
								try {
									JSONObject resutJs = new JSONObject(result);
									mobile_num = resutJs.getString("mobile_num");
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						} 
					//	what = httpResp.getStatusLine().getStatusCode();
						Log.e("httpResp", httpResp.getStatusLine()
								.getStatusCode() + "");

					} catch (ConnectTimeoutException e) {
						result = e.getMessage();

					} catch (ClientProtocolException e) {
						e.printStackTrace();
						result = e.getMessage();
					} catch (IOException e) {
						e.printStackTrace();
						result = e.getMessage();
					}
					try {
						Thread.sleep(1000);
						step++;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				cb.onResult(result);
			}
		}.start();
	}
	
	
	
	/**
	 * @param url
	 * @param imsi
	 * @return
	 */
//	public synchronized static void requestPayResult(CharSequence url, String orderID,final Callback cb) {
//		final String urlstr = url+ "?order=" + orderID;
//		new Thread() {
//			@Override
//			public void run() {
//				String result = null;
//				HttpGet httpGet = new HttpGet(urlstr);
//				BasicHttpParams httpParams = new BasicHttpParams();
//				HttpConnectionParams.setConnectionTimeout(httpParams,
//						10 * 60 * 1000);
//				HttpConnectionParams.setSoTimeout(httpParams, 10 * 60 * 1000);
//				int step = 0;
//				//1代表成 0代表失败-1代表后台还未收到结果,2代表客户端认为超时默认做成功处理
//				int ret =-1;
//				while (ret<0&&step<20&&GameSDK.overRequestPayResult){
//					try {
//						Util_G.debugE("E支付的URL", urlstr);
//						HttpClient client = new DefaultHttpClient(httpParams);
//						HttpResponse httpResp = client.execute(httpGet);
//						if (httpResp.getStatusLine().getStatusCode() == 200) {
//							result = EntityUtils.toString(httpResp.getEntity(),
//									"UTF-8");
//							if(!(result.contains("DOCTYPE HTML")||result.contains("doctype html"))){
//								
//								try {
//									JSONObject resutJs = new JSONObject(result);
//									ret = resutJs.getInt("ret");
//								} catch (Exception e) {
//									// TODO Auto-generated catch block
//									e.printStackTrace();
//								}
//							}
//						} 
//					//	what = httpResp.getStatusLine().getStatusCode();
//
//					} catch (ConnectTimeoutException e) {
//						result = e.getMessage();
//
//					} catch (ClientProtocolException e) {
//						e.printStackTrace();
//						result = e.getMessage();
//					} catch (IOException e) {
//						e.printStackTrace();
//						result = e.getMessage();
//					}
//					try {
//						Thread.sleep(3000);
//						step++;
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//				
//				if (ret <0) {
//					result = "{ret:2}";
//					Util_G.debugE("ALLPAY", "超时算作了成功!");
//				}
//				if (GameSDK.overRequestPayResult) {//如果是客户端直接支付就失败了 不进入回调 false是客户端直接失败
//					Util_G.debugE("服务端通知的结果", result);
//					cb.onResult(result);
//				}
//				
//				
//			}
//		}.start();
//	}
	
	/**
	 * 读取manifest.xml中application标签下的配置项，如果不存在，则返回空字符串
	 * 
	 * @param key
	 *            键名
	 * @return 返回字符串
	 */
	public static String getConfigString(Context context, String key) {
		String val = "";
		try {
			ApplicationInfo appInfo = context.getPackageManager()
					.getApplicationInfo(context.getPackageName(),
							PackageManager.GET_META_DATA);
			val = appInfo.metaData.getString(key);
			if (val == null) {
				Util_G.debugE("Helper", "please set config value for " + key
						+ " in manifest.xml first");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return val;
	}
	
	public static int getJsonInt(JSONObject json, String key) {
		int result = 0;
		try {
			result = json.getInt(key);
		} catch (Exception e) {
		}
		return result;
	}

	public static boolean getJsonBoolean(JSONObject json, String key) {
		boolean result = false;
		try {
			result = json.getBoolean(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static long getJsonLong(JSONObject json, String key) {
		long result = 0;
		try {
			result = json.getLong(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String getJsonString(JSONObject json, String key) {
		String result = null;
		try {
			result = json.getString(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String getJsonString(JSONObject json, String key, String def) {
		String result = null;
		try {
			result = json.getString(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return StringUtil.isEmpty(result) ? def : result;
	}

	public static void putJsonValue(JSONObject json, String key, Object value) {
		try {
			json.put(key, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static JSONObject getJSONObject(JSONObject pack, String key) {
		JSONObject json = null;
		try {
			json = pack.getJSONObject(key);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
	public static JSONObject getJSONObject(JSONArray array, int index) {
		JSONObject json = null;
		try {
			json = array.getJSONObject(index);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	public static JSONObject getJSONObject(String str) {
		JSONObject json = null;
		try {
			json = new JSONObject(str);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	public static float getJsonFloat(JSONObject json, String key) {
		float result = 0.0f;
		try {
			result = (float) json.getDouble(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static JSONArray getJsonArray(JSONObject json, String key) {
		JSONArray result = null;
		try {
			result = json.getJSONArray(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 判断 内存卡是否存在 可用的支付类型 jar包
	 */
	public static boolean existFile(String path,String fileName){
		File filePath = new File(path);
		if (!filePath.isDirectory()) {
			filePath.mkdirs();
		}
		File file = new File(fileName);
		if (!file.exists()||!file.isFile()) {
			return false;
		}
		return true;
	}
	public static void deleteFile(String filePath){
		File file = new File(filePath);
		if (file.exists()&&file.isFile()) {
			file.delete();
		}
	}
	public static Class<?> loadClass(Context context, String jarPath,
			String className) {
		String dexOutputDirs = context.getDir("dex", 0).getAbsolutePath();
		Class<?> libProviderClazz = null;
		DexClassLoader dexClassLoader = new DexClassLoader(jarPath,
				dexOutputDirs, null, context.getClassLoader());
		try {
			libProviderClazz = dexClassLoader.loadClass(className);
		} catch (Throwable e) { 
			e.printStackTrace();
			return null;
		}

		return libProviderClazz;
	}
	
	/**
	 * 获取外置SD卡路径
	 * 
	 * @return
	 */
	public static String getSDCardPath() {
		return Environment.getExternalStorageDirectory().getPath();
	}  

	/*
	 * 返回SD卡可用容量 --#
	 */
	public static long getUsableStorage() {
		String sDcString = android.os.Environment.getExternalStorageState();
		if (sDcString.equals(android.os.Environment.MEDIA_MOUNTED)) {
			File pathFile = android.os.Environment
					.getExternalStorageDirectory();
			android.os.StatFs statfs = new android.os.StatFs(pathFile.getPath());

			// 获取可供程序使用的Block的数量
			long nAvailaBlock = statfs.getAvailableBlocks();

			long nBlocSize = statfs.getBlockSize();

			// 计算 SDCard 剩余大小MB
			return nAvailaBlock * nBlocSize / 1024 / 1024;
		} else {
			return -1;
		}
	}
}
