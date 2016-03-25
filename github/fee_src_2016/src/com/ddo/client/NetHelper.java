package com.ddo.client;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;

public class NetHelper{

	public WifiManager wifiManager;

	public ConnectivityManager connMgr;

	public Boolean appCloseWifi;
	
	public static final Uri CURRENT_APN_URI = Uri.parse("content://telephony/carriers/preferapn"); 
	public static final Uri APN_LIST_URI = Uri.parse("content://telephony/carriers"); 

	public NetHelper(Activity activity) {
		wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
		connMgr = (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		appCloseWifi = false;
	}

	/**
	 * 关闭wifi
	 */
	public void closeWifi() {
		if (!wifiManager.isWifiEnabled()) {
			return;
		}

		wifiManager.setWifiEnabled(false);
		appCloseWifi = true;

		NetworkInfo networkInfo = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		for (int n = 0; n < 20; n++) {
			//wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
			if (wifiManager.isWifiEnabled()) {
				wifiManager.setWifiEnabled(false);
			}
			if (networkInfo.isConnected()) {
				break;
			}
			try {
				Thread.sleep(1000l);
			} catch (Exception e) {
				e.printStackTrace();
			}
			networkInfo = connMgr
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		}
	}

	
	public void restoreNet() {
		// 如果是刚才关闭wifi的，则重新打开wifi
		if (appCloseWifi && !wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(true);
		}
	}
	 
}
