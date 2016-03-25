package mobi.zty.pay;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;


public class Utils {
	/**
	 * 
	 * @description:获取系统版本号
	 * @return
	 * @return int
	 * @throws
	 */
	public static int getVersionCode(Context context) {
		int verCode = 0;
		try {
			verCode = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
		}
		return verCode;
	}
	
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
			val =  appInfo.metaData.getString(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return val==null?"":val.trim();
	}

}
