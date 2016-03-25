package mobi.zty.sdk.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class DeviceInfoUtil {

    public static final String ZTY_PACKAGE_ID = "1";//SHOUMENG_PACKAGE_ID//lsl

    
   	public static String getIMEI(Context context)
   	{
   		 TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
   		 return telephonyManager.getDeviceId();
   	}
   	public static String getIMSI(Context context)
   	{
   		 TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
   		 return telephonyManager.getSubscriberId();
   	}
   	
    public static boolean is_network(Context context)
    {
    	boolean ret = false;
    	
	    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    
	    if (connectivityManager != null) {
	        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();//获取网络的连接情�?
	        if (networkInfo != null) {
	          ret = true;  
	        }
	        
	    }
	    return ret;
    }
    
	 
    public static DeviceInfo getDeviceInfo(Context context) {

        if (context == null)
            return null;

        try {

            DeviceInfo info = new DeviceInfo();

            //lsl
            info.setPackageId(ZTY_PACKAGE_ID);//info.setPackageId(MetaDataUtil.getInt(context,SHOUMENG_PACKAGE_ID,0));
            info.setPlatform(1); // 1 for Android

            WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = (wifiMgr == null ? null : wifiMgr.getConnectionInfo());
            if (wifiInfo != null) {
                info.setMac(wifiInfo.getMacAddress());
            }
            
            String ip = "";
            
            
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                info.setImei(telephonyManager.getDeviceId());
                String imsi2 = telephonyManager.getSimOperator();
                String imsi = telephonyManager.getSubscriberId();
                info.setImsi(Helper.isEmpty(imsi)?imsi2:imsi);
                switch (telephonyManager.getNetworkType()) {
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                    case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                        info.setNetworkType(2);
                        break;
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                        info.setNetworkType(3);
                        break;
                    default:
                        info.setNetworkType(5);
                }
                String proxyHost = android.net.Proxy.getDefaultHost();
                if (proxyHost != null) info.setNetworkType(4); //Wap上网
            }

            info.setModel(Build.MODEL);
            info.setOsVersion(Build.VERSION.SDK_INT);
            ip = Helper.getIpAddress(context);
//            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//            if (connectivityManager != null) {
//                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();//获取网络的连接情�?
//                if (networkInfo != null) {
//                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
//                        info.setNetworkType(1);
//                        ip = Helper.getWifiIpAddress(context);                         
//                    }
//                }
//                
//            }
//
//            if(info.getNetworkType() != 1)
//            {
//            	ip = Helper.getGprsIpAddress();
//            }
            info.setIp(ip);
           
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null) {
                Display display = windowManager.getDefaultDisplay();
                info.setScreenWidth(display.getWidth());
                info.setScreenHeight(display.getHeight());
            }

            return info;

        } catch (Exception ex) {
            return null;
        }

    }

}
