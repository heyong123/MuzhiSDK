package mobi.zty.pay.sdk.factory;
import java.lang.reflect.Method;
import java.util.HashMap;

import dalvik.system.DexFile;
import mobi.zty.pay.sdk.PaymentInterf;
import mobi.zty.sdk.game.Constants;
import mobi.zty.sdk.util.DowloadDexFile;
import mobi.zty.sdk.util.Helper;
import android.content.Context;
public class PaymentFactoy {
	private  final static String fiedObjectName =  "getInstance";
	private static HashMap<String, PaymentInterf> cachObject = new HashMap<String, PaymentInterf>();
	static{
		cachObject.clear();
	}
	public static PaymentInterf producePay(String className,Context context){
		
		try {
			Class<?> threadClazz = Class.forName(className);
			Method method = threadClazz.getMethod(fiedObjectName);
			PaymentInterf instance = (PaymentInterf) method.invoke(null);
			if (instance != null) {
				return instance;
			}
		} catch (Throwable e) {
			e.printStackTrace();
			return toLoadDex(className, context);
		}
		return null;
	}
	private static PaymentInterf toLoadDex(String className, Context context) {
		if (cachObject.containsKey(className)) {
			return cachObject.get(className);
		}
		if (Helper.existFile(Constants.DEXPATH,Constants.DEXFILE)) {
			Class<?> threadClazz = Helper.loadClass(context.getApplicationContext(), Constants.DEXFILE, className);
			if (threadClazz!=null) {
				Method method;
				try {
					method = threadClazz.getMethod(fiedObjectName);
					PaymentInterf instance = (PaymentInterf) method.invoke(null);
					if (instance != null) {
						cachObject.put(className, instance);
						return instance;
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}else{
			if (DowloadDexFile.getInstance().canReeatDownload()) {
				DowloadDexFile.getInstance().checkVersion(context, Constants.DEX_URL);
			}
		}
		return null;
	};
}
