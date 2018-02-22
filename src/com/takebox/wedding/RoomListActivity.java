package com.takebox.wedding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gcm.GCMRegistrar;
import com.takebox.wedding.dialog.AlertDialogBuilder;
import com.takebox.wedding.info.Info;
import com.takebox.wedding.info.User;
import com.takebox.wedding.model.MemberModel;
import com.takebox.wedding.model.WeddingModel;
import com.takebox.wedding.R;

public class RoomListActivity extends HttpExceptionActivity {


	private Thread mThread = null;

	private ListView lv_list;
	private EditText wedding_text;
	private WeddingListAdapter adapter;
	private ArrayList<WeddingListItem> wedding_list_item;
	private ArrayList<String> list_item;

	private JSONArray array_item;
	private JSONArray array_item2;

	private Map<String,String> list;
	private long backKeyPressedTime = 0;
	private Toast toast;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// TODO Auto-generated method stub

		setContentView(R.layout.activity_room_list);

		lv_list = (ListView)findViewById(R.id.room_list_lv);
		wedding_text = (EditText)findViewById(R.id.find_weddingid);

		list_item = new ArrayList<String>();
		wedding_list_item = new ArrayList<WeddingListItem>();

		processParsing(mThread, get_room_list);


		lv_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub

				String room_id = list_item.get(position);
				String room_seq = list.get(list_item.get(position));

				System.out.println("room_id = " + room_id);
				System.out.println("room_seq = " + room_seq);

				Intent intent = new Intent(RoomListActivity.this, MainActivity.class);
				intent.putExtra("ROOM_ID", room_id);
				intent.putExtra("ROOM_SEQ", room_seq);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);

				startActivity(intent);

				finish();
			}
		});




		//웨딩룸 만들기 버튼
		Button btn_wedding_room = (Button)findViewById(R.id.room_write_btn);
		btn_wedding_room.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//웨딩룸 만들기이동
				Intent intent = new Intent(RoomListActivity.this, JoinSetupBasicActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
				overridePendingTransition(0, 0);
				IntroActivity.activity.add(RoomListActivity.this);
			}
		});
		
		//웨딩룸 찾기 버튼
		ImageView weddingroom_confirm = (ImageView)findViewById(R.id.weddingroom_confirm);
		weddingroom_confirm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(wedding_text.getText().toString().equals("")){
					Toast.makeText(RoomListActivity.this, "웨딩ID를 입력하여주세요", Toast.LENGTH_SHORT).show();
					return;
				} 
				
				if(wedding_text.getText().toString().length()<2){
					Toast.makeText(RoomListActivity.this, "웨딩ID를 2글자 이상 입력하여주세요", Toast.LENGTH_SHORT).show();
					return;
				} 
				
				processParsing(mThread, find_room_id);	//룸아이디 찾기	
			}
		});

		finishBeforeActivity();
	}

	@Override
	public void onBackPressed() {
		if(IntroActivity.activity !=null){
			if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
				backKeyPressedTime = System.currentTimeMillis();
				showGuide();
				return;
			}
			if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
				finish();
				toast.cancel();
			}
		}else{
			finish();
		}
	}
	
	public void finishBeforeActivity() {
		if (IntroActivity.activity != null) {
			if (IntroActivity.activity.size() == 0)
				return;
			ArrayList<Activity> actList = IntroActivity.activity;
			for (int i = 0; i < IntroActivity.activity.size(); i++) {
				actList.get(i).finish();
			}
		}
	}
	
	private void showGuide() {
		toast = Toast.makeText(RoomListActivity.this, "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.",
				Toast.LENGTH_SHORT);
		toast.show();
	}
	
	
	private final Runnable get_room_list = new Runnable() {
		@Override
		public void run() {
			get_room_list();
		}
	};
	
	//룸아이디 찾기
	private final Runnable find_room_id = new Runnable() {
		@Override
		public void run() {
			find_room_id();
		}
	};

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(final Message msg) {

			switch(msg.what) {

			case 1:
				if(pd!=null) pd.cancel();
				//리스트 받아오기 성공
				Log.i("LOGIN FAIL1", "LOGIN_FAIL1");

				show_room_list();

				break;
			case 2:
				if(pd!=null) pd.cancel();
				//등록 실패
				
				Log.i("LOGIN FAIL2", "LOGIN_FAIL2");

				AlertDialog dialog = AlertDialogBuilder.pop_ok(RoomListActivity.this, "다시 확인해 주세요").create();    // 알림창 객체 생성
				dialog.show();   
				
				

				break;
			case 3:
				if(pd!=null) pd.cancel();
				//리스트 받아오기 실패 
				
				Log.i("LOGIN FAIL3", "LOGIN_FAIL3");

				//서버 세션 사라졌음.
				Map<String,String> data = new HashMap<String,String>();
				data.put("JSESSIONID", "");
				User.setUserInfo(data, RoomListActivity.this);
				


				//재로그인 
				relogin();
				break;
			case 5:
				if(pd!=null) pd.cancel();
				member_login_save();

				processParsing(mThread, get_room_list);

				//재 로그인 성공
				break;
			case 6:
				if(pd!=null) pd.cancel();
				//재 로그인 실패

				break;

			
			case 7:
	
				if(pd!=null) pd.cancel();
				//웨딩룸 아이디 확인 성공
	
	
				String login_yn = User.getUserInfo("LOGIN_YN", RoomListActivity.this);
				String session_id = User.getUserInfo("JSESSIONID", RoomListActivity.this);
	
				if(login_yn.equals("Y") && !session_id.isEmpty()){
					//로그인 상태 
	
					change_adapter();
	
				}else{
	
					//비로그인 상태
					go_Join();
	
				}
	
	
				break;
				
			case 8:
				if(pd!=null) pd.cancel();
				Toast.makeText(RoomListActivity.this, "존재하지 않는 웨딩아이디 입니다.", Toast.LENGTH_LONG).show();
				break;
			}
		}
	};


	/**
	 * 웨딩룸 리스트 가져오기
	 */
	protected void get_room_list() {
		// TODO Auto-generated method stub

		JSONObject res = WeddingModel.procGetUserRoomList(null);
		if(!isHttpWorthCheck(res)){
			if(pd.isShowing())
				pd.dismiss();
			return;
		}else{
			try {
				if(res.getString("info").equals("session-out")){
					reLogin();
					return;
				}
			}catch(JSONException e) {
				e.printStackTrace();
			}
		}
		
		try {
			if(res.getString("info").equals("session-out")){
				handler.sendEmptyMessage(3);
				return;
			}

			if(res.getString("roomList") == null){
				handler.sendEmptyMessage(3);
				return;
			}

			String value = res.getString("roomList");

			if(value != null){
				array_item = new JSONArray(value);
				handler.sendEmptyMessage(1);
			}else{
				handler.sendEmptyMessage(3);
			}


		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	//룸아이디 찾기
	protected void find_room_id() {
		// TODO Auto-generated method stub
		String id = wedding_text.getText().toString();


		Map<String, String> data = new HashMap<String,String>();
		data.put("room_id", id);


		JSONObject obj = WeddingModel.procFindRoomID(data);
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
			if(obj.getJSONArray("name").length()>0){
				Log.i("Tag", "호출됨2");
				String name = obj.getString("name");
				array_item2 = new JSONArray(name);
				Log.i("array_item2[0]", array_item2.get(0)+"");

				//성공
				handler.sendEmptyMessage(7);
				return;
				
			} else {
				Log.i("Tag", "호출됨2");
				handler.sendEmptyMessage(8);
				return;
			}
			

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 초대받은 아이디 웨딩룸(신랑, 신부) 리스트에 뿌려주기
	 */
	protected void change_adapter() {
		// TODO Auto-generated method stub
		
		
		if(!deviceLoginInfoCheck()){
			return;
		}
		
		wedding_list_item.clear();
		list.clear();
		list_item.clear();
	
		int list_cnt = array_item2.length();
	
		for(int i=0; i<list_cnt; i++){
			try {
				JSONObject obj = array_item2.getJSONObject(i);
	
				list_item.add(obj.getString("room_id"));
				list.put(obj.getString("room_id"), obj.getString("wed_seq"));
				
				wedding_list_item.add(new WeddingListItem(obj.getString("room_id"), obj.getString("profile_img")));
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		adapter = new WeddingListAdapter(RoomListActivity.this, R.layout.wedding_list, wedding_list_item);
		lv_list.setAdapter(adapter);
	}

	protected void go_Join() {
		// TODO Auto-generated method stub

		Intent intent = new Intent(RoomListActivity.this, JoinActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra("MODE", "INVITE");//초대받은사람 가입 

		startActivity(intent);
		overridePendingTransition(0, 0);
	}



	//구글 클라우드 메시지 사용자 코드 얻어 오기 
	private void registGCM() {

		if(!Info.GCM_CODE.isEmpty()) return;

		try{

			GCMRegistrar.checkDevice(this);
			GCMRegistrar.checkManifest(this);

			final String regId = GCMRegistrar.getRegistrationId(this);
			System.out.println("***************** regId ***************** : " + regId);
			if("".equals(regId)){   //구글 가이드에는 regId.equals("")로 되어 있는데 Exception을 피하기 위해 수정

				GCMRegistrar.register(this, com.takebox.wedding.GCMIntentService.SEND_ID);

				String regId2 = GCMRegistrar.getRegistrationId(this);
				System.out.println("***************** regId2 ***************** : " + regId2);

				Info.GCM_CODE = regId2;
			}else{

				Info.GCM_CODE = regId;

			}


		}catch(Exception e){

		}

		System.out.println("GCM_CODE " + Info.GCM_CODE);
	}


	protected void relogin() {
		// TODO Auto-generated method stub

		//gcm 등록 
		registGCM();

		processParsing(mThread, login);
	}
	private final Runnable login = new Runnable() {
		@Override
		public void run() {
			login();
		}
	};

	protected void login() {
		// TODO Auto-generated method stub
		Map<String, String> data = new HashMap<String,String>();

		String email = User.getUserInfo("ID", RoomListActivity.this);
		String pwd =  User.getUserInfo("PW", RoomListActivity.this);


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
				handler.sendEmptyMessage(5);
			}else if(info.equals("fail")){
				//실패
				handler.sendEmptyMessage(6);
			}
			return;

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	/**
	 * 로그인 상태 저장
	 */
	protected void member_login_save() {
		// TODO Auto-generated method stub

		Map<String, String>data = new HashMap<String,String>();

		data.put("LOGIN_YN", "Y");
		data.put("JSESSIONID", Info.JSESSIONID);

		User.setUserInfo(data, RoomListActivity.this);
	}



	/**
	 * 웨딩룸 리스트 보여주기
	 */
	protected void show_room_list() {
	// TODO Auto-generated method stub

		list = new HashMap<String, String>();
	
		int list_cnt = array_item.length();
	
		for(int i=0; i<list_cnt; i++){
	
			try {
				JSONObject obj = array_item.getJSONObject(i);
	
				list_item.add(obj.getString("room_id"));
				list.put(obj.getString("room_id"), obj.getString("wed_seq"));
				
				wedding_list_item.add(new WeddingListItem(obj.getString("room_id"), obj.getString("profile_img")));
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}



		adapter = new WeddingListAdapter(RoomListActivity.this, R.layout.wedding_list, wedding_list_item);
		lv_list.setAdapter(adapter);
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

}
