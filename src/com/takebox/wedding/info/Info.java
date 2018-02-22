package com.takebox.wedding.info;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Environment;
import android.webkit.CookieManager;



public class Info {

		public static String MASTER_URL = "http://app.takebox.co.kr";
		public static String MASTER_FILE_URL = "http://file.takebox.co.kr";
		public static String MASTER_UTIL_URL = "http://util.takebox.co.kr";
		public static String DEFAULT_IMG = "http://file.takebox.co.kr/default/profile.jpg";

		
//		public static String MASTER_URL = "http://192.168.10.10:8080/takebox";
//		public static String MASTER_FILE_URL = "http://192.168.10.10";
//		public static String MASTER_UTIL_URL = "http://192.168.10.10/util";
//		public static String DEFAULT_IMG = "http://192.168.10.10/default/profile.jpg";
		
		public static String filePath = Environment.getExternalStorageDirectory()+"/WeddingBox";
		
		public static String MARKET_URL = "market://details?id=com.takebox.wedding";
		public static String PC_MARKET_URL = "https://goo.gl/FS0tnd";
		
		public static HttpClient httpclient = new DefaultHttpClient();

		public static CookieManager cookieManager = CookieManager.getInstance();
		
		public static String JSESSIONID;
		
		public static String GCM_CODE = "";
		
		public static String GCM_REG_ID;
		
		public static String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApitYggTtDlb12+2kPiLOmXMtJS19ZeUwl4ZafZIIuk84wepUeLNa6lk87DZ1tZvjeU2UKZ/85MPbOWtGWeFpvvyARdBFAWbOIJeqT488wE0Ka4dXQXMzuYgL6H1ojgaEYZyETzJQSrl4D5NXyMC00fHArY1EPbkBdxeZyn4mJT2Kfn4/4vna6GKZ6gXt1qk1aTXjvQSgUkHE+eT5pyGCdSpoC+/B+ppCMqMMSw8nkn+xDn38DXv4Jz3zmkMalr5iwVU7ZsBYkH4A+2sPTj8JuDQIckSVeBHnU+jOg207yTWB4XXlkNZj4USF35LfnwrS1HggRn4o2xQyzfNTijUzrQIDAQAB";
		
		
		//초대받은아이디 정보 임시 저장
		public static JSONArray INVITE_ID_INFO;
		
		public static DefaultHttpClient getThreadSafeClient()  {

	        DefaultHttpClient client = new DefaultHttpClient();
	        ClientConnectionManager mgr = client.getConnectionManager();
	        HttpParams params = client.getParams();
	        client = new DefaultHttpClient(new ThreadSafeClientConnManager(params, 

	                mgr.getSchemeRegistry()), params);
	        return client;
		}
		
}