package mobi.zty.sdk.http;

import mobi.zty.sdk.game.Constants;
import mobi.zty.sdk.util.DeviceInfoUtil;
import mobi.zty.sdk.util.Helper;
import mobi.zty.sdk.util.Util_G;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.os.AsyncTask;

public class HttpRequest<T> extends AsyncTask<String, Integer, T> {

    private Context context;
    private ResponseParser<T> responseParser;
    private HttpCallback<T> callBack;

    private int errorCode;
    private String errorMessage;
    private boolean running;
    
   
    
    public HttpRequest(Context context, ResponseParser<T> responseParser, HttpCallback<T> callBack) {
        this.context = context;
        this.responseParser = responseParser;
        this.callBack = callBack;
    }

    @Override
    protected void onPreExecute() {
    	running = true;
    }

    @Override
    protected T doInBackground(String... strings) {

        if (strings.length == 0) {
            errorCode = Constants.ERROR_CODE_SYS;
            errorMessage = "参数错误！";
            return null;
        }

        try {

            String url = strings[0];
            String postData = strings.length == 2 ? strings[1] : null;

            HttpClient client = new DefaultHttpClient();
            setProxyIfNecessary(context, client);
            HttpParams localHttpParams = client.getParams();
            HttpConnectionParams.setConnectionTimeout(localHttpParams, 30000);
            HttpConnectionParams.setSoTimeout(localHttpParams, 30000);
            HttpClientParams.setRedirecting(localHttpParams, false);

            HttpPost post = new HttpPost(url);
            post.setHeader("Accept", "*/*");
            post.setHeader("Accept-Charset", "UTF-8,*;q=0.5");
            post.setHeader("Accept-Encoding", "gzip,deflate");
            post.setHeader("Accept-Language", "zh-CN");
            post.setHeader("User-Agent", "ZTYSDK");
            //设置post的数据
            if (postData != null) {
                HttpEntity postEntify = new ByteArrayEntity(postData.getBytes("UTF-8"));
                post.setEntity(postEntify);
            }
            Util_G.debugE(Constants.TAG, url);
            Util_G.debugE(Constants.TAG, postData);
            HttpResponse response = client.execute(post);
            int ret = response.getStatusLine().getStatusCode();
            
            if (ret == HttpStatus.SC_OK) {

                HttpEntity entity = response.getEntity();
                final Header encoding = entity.getContentEncoding();
                if (encoding != null) {
                    for (HeaderElement element : encoding.getElements()) {
                        if (element.getName().equalsIgnoreCase("gzip")) {
                            entity = new InflatingEntity(response.getEntity());
                            break;
                        }
                    }
                }

                String content = EntityUtils.toString(entity);
                Util_G.debug_i(Constants.TAG, content);
                if (!Helper.isEmpty(content)&&responseParser!=null) {
                    return responseParser.getResponse(content);
                } else {
                    return null;
                }

            }

        } catch (Exception ex) {
        	if(ex != null)
        	Util_G.debug_i("test", ex.toString());
            errorCode = Constants.ERROR_CODE_SYS;
            errorMessage = "网络错误，网络不给力";
            
            boolean info = DeviceInfoUtil.is_network(context);
            if(!info)
            {
            	errorCode = Constants.ERROR_CODE_NET;
                errorMessage = "网络没打开，请先打开网络";
            }
        }
        return null;

    }

    public void cancelTask()
    {
    	running = false;
    }
    
    @Override
    protected void onPostExecute(T response) {
        if(running)
        {
	        if (response != null) {
	        	if(callBack != null)
	            callBack.onSuccess(response);
	        } else {
	        	if(callBack != null)
	            callBack.onFailure(errorCode, errorMessage);
	        }
        }
    }

    private static void setProxyIfNecessary(Context context, HttpClient client) {

        NetworkInfo localNetworkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if ((localNetworkInfo != null) &&
                (localNetworkInfo.isAvailable()) &&
                (localNetworkInfo.getType() == 0)) {
            String host = Proxy.getDefaultHost();
            int port = Proxy.getDefaultPort();
            if ((host != null) && (port >= 0)) {
                HttpHost httpHost = new HttpHost(host, port);
                client.getParams().setParameter("http.route.default-proxy", httpHost);
            }
        }

    }


}
