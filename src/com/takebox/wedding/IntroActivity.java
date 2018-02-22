package com.takebox.wedding;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.Session.StatusCallback;
import com.facebook.model.GraphObject;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gcm.GCMRegistrar;
import com.takebox.wedding.info.Info;
import com.takebox.wedding.info.User;
import com.takebox.wedding.info.Version;
import com.takebox.wedding.model.MemberModel;
import com.takebox.wedding.model.WeddingModel;
import com.takebox.wedding.util.CustomTextFont;


/*
 * 로딩 페이지(인트로 페이지)
 */
public class IntroActivity extends HttpExceptionActivity {


	private static final int DELAY_TIME = 1000;
	private Thread mThread = null;

	public static ArrayList<Activity> activity;
	public int room_cnt = 0;
	public String room_no = "0";
	public String room_id = "";
	public String request_ids = "";
	public boolean app_linked_flag = false;
	public String force_yn = "N";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);
		
		facebook_init(savedInstanceState);
		
		activity = new ArrayList<Activity>();
		activity.add(this);
		
		//폰트등록
		CustomTextFont.hunsomsatangR = Typeface.createFromAsset(getAssets(),"hunsomsatangR.ttf");
		CustomTextFont.NanumGothic = Typeface.createFromAsset(getAssets(), "NanumGothic.ttf");
		CustomTextFont.NanumGothicBold = Typeface.createFromAsset(getAssets(), "NanumGothicBold_0.ttf");
		
		Uri data = getIntent().getData();
		if(data != null){
			request_ids = data.getQueryParameter("request_ids");
			if(request_ids!="" && request_ids!=null){		//페이스북일경우 값받아오기
				String array[] = request_ids.split(",");
				getRequestData(array[array.length-1]);
			}
				
			room_no = data.getQueryParameter("room_no");
			room_id = data.getQueryParameter("room_id");
			Log.i("TAG", request_ids + ", " + room_no+", "+room_id);
		}
		
		//버전등록
		PackageInfo i=null;
		try {
			i = IntroActivity.this.getPackageManager().getPackageInfo(IntroActivity.this.getPackageName(), 0);
		} catch (NameNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Log.i("PackgeInfo", i+"");
		Version.setVersionInfo("VERSION", i.versionName, IntroActivity.this);
		
		//키해시 확인
		try {
           PackageInfo info = getPackageManager().getPackageInfo(
                   "com.takebox.wedding", 
                   PackageManager.GET_SIGNATURES);
           for (Signature signature : info.signatures) {
	           MessageDigest md = MessageDigest.getInstance("SHA");
	           md.update(signature.toByteArray());
	           Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
           }
       } catch (NameNotFoundException e) {

       } catch (NoSuchAlgorithmException e) {

       }

		CookieSyncManager.createInstance(IntroActivity.this);
		//CookieManager.getInstance().removeAllCookie();
		CookieSyncManager.getInstance().startSync();


		Info.JSESSIONID = User.getUserInfo("JSESSIONID", IntroActivity.this);
		processParsing(mThread, version_check);

		
		if(GCMRegistrar.getRegistrationId(this).equals(null)){
			registerGcm();
		}
	}
	
	
	protected void login() {
		// TODO Auto-generated method stub
		Map<String, String> data = new HashMap<String,String>();

		String email = User.getUserInfo("ID", IntroActivity.this);
		String pwd =  User.getUserInfo("PW", IntroActivity.this);


		data.put("j_username",email);
		data.put("j_password", pwd);


		//GCM
		gcmCodeCheck();
		data.put("device_id", Info.GCM_CODE);



		//data.put("email", "");

		JSONObject obj = MemberModel.procMemberLogin(data);
		if(!isHttpWorthCheck(obj)){
			if(pd.isShowing())
				pd.dismiss();
			return;
		}else{
			try {
				if(obj.getString("info").equals("session-out")){
					reLogin();
					return;
				}
			}catch(JSONException e) {
				e.printStackTrace();
			}
		}

		try {
			String info = obj.getString("info");
			
			//에러코드 확인
			if(info.equals("ok")){
				//성공
				room_cnt = obj.getInt("room_cnt");
				handler.sendEmptyMessage(3);
			}else if(info.equals("fail")){
				//실패
				handler.sendEmptyMessage(4);
			}
			return;

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void registerGcm() {
		 
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		 
		final String regId = GCMRegistrar.getRegistrationId(this);
		 
		if (regId.equals("")) {
			GCMRegistrar.register(this, Info.GCM_REG_ID );
		} else {
			Info.GCM_REG_ID = regId;
			Log.e("id", regId);
		}
	}
	
	public void complete_version_check(){
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				//최초 어플 시작 이거나 로그인 상태가 아니면 startActivity로 이동
				if(Info.JSESSIONID.equals("")){

					go_intro_login();

				} else {
					
					processParsing(mThread, login);

				}
			}
		}, DELAY_TIME);
	}
	
	//IntroLoginActivity 로 이동
	protected void go_intro_login() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(IntroActivity.this, IntroLoginActivity.class);
		intent.putExtra("room_no", room_no);
		intent.putExtra("room_id", room_id);
		startActivity(intent);
		finish();
		overridePendingTransition(0, 0);
	} 
	
	
	
	
	//시작페이지로 이동
	protected void go_start() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(IntroActivity.this, StartActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
		finish();
		overridePendingTransition(0, 0);
	} 
	
	/**
	 * roomlist 로 이동
	 */
	protected void go_roomlist() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(IntroActivity.this, RoomListActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
		finish();
		overridePendingTransition(0, 0);
	}
	
	/**
	 * mainActivity 로 이동
	 */
	protected void go_main() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(IntroActivity.this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra("ROOM_ID", room_id);
		intent.putExtra("ROOM_SEQ", room_no);
		startActivity(intent);
		finish();
		overridePendingTransition(0, 0);
	}
	
	private final Runnable version_check = new Runnable() {
		@Override
		public void run() {
			version_check();
		}
	};
	
	private final Runnable login = new Runnable() {
		@Override
		public void run() {
			login();
		}
	};
	
	private void version_check(){
		// TODO Auto-generated method stub
		Map<String, String> data = new HashMap<String,String>();
		
		
		String version = Version.getVersionInfo("VERSION", IntroActivity.this);
		data.put("ver", version);
		
		JSONObject obj;
		if(Info.JSESSIONID.equals("")){
			obj = WeddingModel.VersionCheck(data);
			Info.JSESSIONID = "";
		} else {
			obj = WeddingModel.VersionCheck(data);
		}
		
		if(!isHttpWorthCheck(obj)){
			if(pd.isShowing())
				pd.dismiss();
			return;
		}
		
		try {
			String info = obj.getString("info");
			force_yn = obj.getString("force_yn");
			System.out.println(info.toString());
			
			//에러코드 확인
			if(info.equals("update")){
				//업데이트 필요
				handler.sendEmptyMessage(1);
			}else{
				//최신 업데이트 상태
				handler.sendEmptyMessage(2);
			}
			return;

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(final Message msg) {

			switch(msg.what) {

			case 1:

		//		if(pd!=null) pd.cancel();
				//등록 성공
				
				update_version();
				
				break;
			case 2:
				if(pd!=null) pd.cancel();
				//등록 실패
				
				complete_version_check();
				
				break;
				
			case 3:

				if(pd!=null) pd.cancel();
				//등록 성공
				
				if(room_id!=null){
					if(!room_id.equals("")){
						go_main();	//초대받고 들어온 이용자일때
					} else
						if(room_cnt==0)	//웨딩룸이 하나도 없을때
							go_start();
						else			//웨딩룸리스트로 이동
							go_roomlist();
				} else
					if(room_cnt==0)	//웨딩룸이 하나도 없을때
						go_start();
					else			//웨딩룸리스트로 이동
						go_roomlist();
				
				break;
			case 4:
				if(pd!=null) pd.cancel();
				//등록 실패
				go_intro_login();
				break;

			}
		}
	};
	
	public void update_version(){
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
	    alt_bld.setMessage("버전이 맞지 않습니다. 최신업데이트를 하러 가시겠습니까?").setCancelable(
	        false).setNegativeButton("No",
	    	        new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int id) {
		            // Action for 'NO' Button
		            dialog.cancel();
		            if(force_yn.equals("Y"))
		            finish();
		            else
		            complete_version_check();
		        }
	        }).setPositiveButton("Yes",
	    	        new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int id) {
//		            Toast.makeText(IntroActivity.this, "업데이트를 하러 갑니다.", Toast.LENGTH_SHORT).show();
		            Intent intent = new Intent(Intent.ACTION_VIEW);
		            intent.setData(Uri.parse(Info.MARKET_URL));
		            startActivity(intent);
		            finish();
		        }
	        });
	    AlertDialog alert = alt_bld.create();
	    // Title for AlertDialog
	    alert.setTitle("업데이트");
	    alert.show();
	}
	
	
	//페이스북 값 받아오기
	private void getRequestData(final String inRequestId) {
	    // Create a new request for an HTTP GET with the
	    // request ID as the Graph path.
	    Request request = new Request(Session.getActiveSession(), 
	            inRequestId, null, HttpMethod.GET, new Request.Callback() {

	                @Override
	                public void onCompleted(Response response) {
	                    // Process the returned response
	                    GraphObject graphObject = response.getGraphObject();
	                    FacebookRequestError error = response.getError();
	                    // Default message
	                    String message = "Incoming request";
	                    if (graphObject != null) {
	                        // Check if there is extra data
	                        if (graphObject.getProperty("data") != null) {
	                            try {
	                                // Get the data, parse info to get the key/value info
	                                JSONObject dataObject = 
	                                new JSONObject((String)graphObject.getProperty("data"));
	                                // Get the value for the key - badge_of_awesomeness
	                                room_no = dataObject.getString("room_no");
	                                room_id = dataObject.getString("room_id");
	                                Log.i("TAG2", room_no+", "+room_id);

	                            } catch (JSONException e) {
	                                message = "Error getting request info";
	                            }
	                        } else if (error != null) {
	                            message = "Error getting request info";
	                        }
	                    }
//	                    Toast.makeText(IntroActivity.this.getApplicationContext(),
//	                            message,
//	                            Toast.LENGTH_LONG).show();
	                }
	        });
	    // Execute the request asynchronously.
	    Request.executeBatchAsync(request);
	}
	
	private void facebook_init(Bundle savedInstanceState) {
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

		Session session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(this, null,
						new StatusCallback() {

							@Override
							public void call(Session session,
									SessionState state, Exception exception) {
								// TODO Auto-generated method stub

							}
						}, savedInstanceState);
			}
			if (session == null) {
				session = new Session(this);
			}
			Session.setActiveSession(session);
			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
				session.openForRead(new Session.OpenRequest(this)
						.setCallback(new StatusCallback() {

							@Override
							public void call(Session session,
									SessionState state, Exception exception) {
								// TODO Auto-generated method stub

							}
						}));
			}
		}
	}



	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}


	
	@Override
	public void onStart() {
		super.onStart();
		//analytics 분석도구 
		EasyTracker.getInstance(this).activityStart(this);  // Add this method.
	}

	@Override
	public void onStop() {
		super.onStop();
		//analytics 분석도구 
		EasyTracker.getInstance(this).activityStop(this);  // Add this method.

	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
	}
	
}
