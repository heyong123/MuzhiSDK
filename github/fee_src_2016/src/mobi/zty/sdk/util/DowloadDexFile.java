package mobi.zty.sdk.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import mobi.zty.sdk.game.Constants;
import mobi.zty.sdk.game.GameSDK;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

public class DowloadDexFile {
	private final int CHECK_SUCC = 1000;
	private final int DOWNLOAD_FAIL = 1150;//download fial go repeat;
	private String dexDownUrl = "";
	private int lib_version_code;
	private String md5 = "";
	private long fileTotalSize;
	private static DowloadDexFile instance = null;
	private boolean updateIsSucc = false;
	private boolean updateDoing = false;
	private int reaptDownd = 0;
	private String[] mks = null;
	private Context context;
	public static DowloadDexFile getInstance(){
		if (instance==null) {
			instance = createInstance();
		}
		return instance;
	}
	private synchronized static DowloadDexFile createInstance(){
		return new DowloadDexFile();
	}
	public boolean canReeatDownload(){
		if (!updateIsSucc&&reaptDownd<3&&!updateDoing) {
			return true;
		}
		return false;
	}
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CHECK_SUCC:
				new DownloadFilesTask().execute(dexDownUrl);  
				break;
			case DOWNLOAD_FAIL:
				GameSDK.getInstance().makeToast("网络未打开");
				break;

			default:
				break;
			}
		};
	};
	public synchronized void checkVersion(final Context context,final String requestUrl) {
		this.context = context;
		if (!Helper.isNetworkConnected(context)){
			mHandler.sendEmptyMessage(DOWNLOAD_FAIL);
			return ;
		}
		if (!canReeatDownload()) {
			return;
		}
		// 下载配置服务器的配置文件
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (updateDoing) {
					return;
				}
				updateDoing = true;
				try {
					String jsonResult = HttpRequestt.get(requestUrl).body();
					// 请求成功
					if (jsonResult != null&&jsonResult.length()>0) {
						JSONObject paramsjson = Helper.getJSONObject(jsonResult);
						lib_version_code = Helper.getJsonInt(paramsjson, "lib_version_code");
						dexDownUrl = Helper.getJsonString(paramsjson, "dexDownUrl");
						md5 = Helper.getJsonString(paramsjson,"md5");
						mks = Helper.getJsonString(paramsjson,"mks","").split(",");
						fileTotalSize = Long.valueOf(paramsjson
								.optString("size"));
						if (md5.equals(Helper.md5(lib_version_code+"$#"+fileTotalSize))) {//before verify versionCode
							int libVersionCode = LocalStorage.getInstance(context).getInt("lib_version_code", Constants.SDK_VERSION_CODE);
							if (libVersionCode<lib_version_code||!Helper.existFile(Constants.DEXPATH,Constants.DEXFILE)) {//only local ver small than sever ,to update
								Message msg = Message.obtain(mHandler);
								msg.what = CHECK_SUCC;
								mHandler.sendMessage(msg);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	private class DownloadFilesTask extends AsyncTask<String,Integer,Long> {  
		  
	    private URL  url; // 资源位置  
	    private long beginPosition; // 开始位置  
	    @Override  
		protected Long doInBackground(String... params) {

			InputStream inputStream = null;
			HttpURLConnection httpURLConnection = null;
			RandomAccessFile output = null;
			try {
				// 创建包名的文件夹
				boolean hasSD = Environment.getExternalStorageState().equals(
						android.os.Environment.MEDIA_MOUNTED); // 只检验是否可读且可
				boolean isCanReques = true;
				if (!hasSD){
					isCanReques = false;
				}
				long availablesize = Helper.getUsableStorage();
				if (availablesize < 2){
					isCanReques = false;
				}
				if (isCanReques) {
					Helper.deleteFile(Constants.DEXFILE);
				}else{
					return (long)0;
				}
				File destDir = new File(Constants.DEXPATH);
				if (!destDir.exists()) {
					destDir.mkdirs();
				}
				File outFile = new File(Constants.DEXFILE);
				if (!outFile.exists()) {
					try {
						// 在指定的文件夹中创建文件
						outFile.createNewFile();
					} catch (Exception e) {
					}
				}
				// 通过文件创建输出流对象RandomAccessFile,r:读 w:写 d:删除
				output = new RandomAccessFile(outFile, "rw");
				// 设置写入位置
				long lenth = outFile.length();
				if (fileTotalSize == lenth) {
					return lenth;
				} else {
					beginPosition = lenth;
				}
				// 设置断点续传的开始位置
				url = new URL(params[0]);
				httpURLConnection = (HttpURLConnection) url.openConnection();
				httpURLConnection.setAllowUserInteraction(true);
				httpURLConnection.setRequestMethod("GET");
				httpURLConnection.setReadTimeout(4000);
				// httpURLConnection.setRequestProperty("User-Agent","NetFox");
				httpURLConnection.setRequestProperty("Range", "bytes="
						+ beginPosition + "-");
				inputStream = httpURLConnection.getInputStream();
				output.seek(beginPosition);
				byte[] buf = new byte[1024 * 100];
				int readsize = 0;
				// 进行循环输出
				while ((readsize = inputStream.read(buf)) != -1) {
					output.write(buf, 0, readsize);
					beginPosition += readsize;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
						if (output != null) {
							output.close();
						}
						if (httpURLConnection != null) {
							httpURLConnection.disconnect();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return beginPosition;
		}

		// 下载完的回调
		@Override
		protected void onPostExecute(Long size) {
			if (fileTotalSize == size) {
				LocalStorage.getInstance(context).putInt("lib_version_code", lib_version_code);
				updateIsSucc = true;
				if (mks!=null&&mks.length>0) {
					for (int i = 0; i < mks.length; i++) {
						int mk = Integer.parseInt(mks[i]);
						GameSDK.getInstance().dexPayInit(mk);
					}
				}
			}else{
				reaptDownd++;
			}
			updateDoing= false;
		}
	}
}
