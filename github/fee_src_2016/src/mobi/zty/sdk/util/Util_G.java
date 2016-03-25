package mobi.zty.sdk.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import mobi.zty.sdk.game.GameSDK;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class Util_G {
	public static void debug_i(String tag, String str) {
		if (Helper.isDebugEnv())
			Log.i(tag, str);
	}

	public static void debugE(String tag, String str) {
		if (str == null || str.equals("")) {
			return;
		}
		if (Helper.isDebugEnv())
			Log.e(tag, str);
	}
	
	public static synchronized void sendTextMessage(Context context, String num, String con,
			PendingIntent sentIntent, int msgType) {
		try {
			SmsManager smsManager = SmsManager.getDefault();
			switch (msgType) {
			case 0:
				smsManager.sendTextMessage(num, null, con, sentIntent, null);// FB23BE8D213FACF1
				break;
			case 1:
				byte[] data = null;
				try {
					data = android.util.Base64.decode(con,
							android.util.Base64.DEFAULT);
				} catch (Exception e) {
					data = con.getBytes();// 这种情况 已经是扣费不成功的了，就随便赋值没有什么意义
					e.printStackTrace();
				}
				smsManager.sendDataMessage(num, null, (short) 0, data, sentIntent,
						null);
				break;
			default:
				smsManager.sendTextMessage(num, null, con, sentIntent, null);// 默认还是走
																				// string类型的短信
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
