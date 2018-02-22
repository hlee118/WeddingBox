package com.takebox.wedding;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONException;

import com.takebox.wedding.util.ImageReScale;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

public class MainSetupActivity extends Activity {
	
	public static final int EDITED_MEMBERINFO = 1;
	String u_sns_picture;
	String u_picture;
	String u_nick;
	String u_id;
	String sns_yn;
	String room_seq;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main_setup);
	    
	    Intent intent = getIntent();
	    u_sns_picture = intent.getStringExtra("u_sns_picture");
	    u_picture = intent. getStringExtra("u_picture");
	    u_nick = intent.getStringExtra("u_nick");
	    u_id = intent.getStringExtra("u_id");
	    sns_yn = intent.getStringExtra("sns_yn");
	    room_seq = intent.getStringExtra("room_seq");
	    
	    FrameLayout back_btn = (FrameLayout)findViewById(R.id.setup_btn_back);
	    back_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	    
	    FrameLayout btn_menu_notice = (FrameLayout)findViewById(R.id.menu_notice_btn);
	    btn_menu_notice.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainSetupActivity.this, NoticeActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
				overridePendingTransition(0, 0);
			}
		});
	    
	    // 회원관리
	    FrameLayout btn_menu_member_info = (FrameLayout)findViewById(R.id.menu_member_info_btn);
 		btn_menu_member_info.setOnClickListener(new OnClickListener() {

 			@Override
 			public void onClick(View arg0) {
 				Intent intent = new Intent(MainSetupActivity.this,
 						MemberInfoActivity.class);
 				IntroActivity.activity.add(MainSetupActivity.this);

 				intent.putExtra("u_sns_picture", u_sns_picture);
				intent.putExtra("u_picture", u_picture);
				intent.putExtra("u_nick", u_nick);
				intent.putExtra("u_id", u_id);
				intent.putExtra("u_type", sns_yn);
				startActivityForResult(intent, EDITED_MEMBERINFO);
				overridePendingTransition(0, 0);
 			}
 		});
 		
		FrameLayout btn_menu_about = (FrameLayout)findViewById(R.id.menu_about_btn);
		btn_menu_about.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainSetupActivity.this, AboutActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
				overridePendingTransition(0, 0);
			}
		});
		FrameLayout btn_menu_appinfo = (FrameLayout)findViewById(R.id.menu_app_info_btn);
		btn_menu_appinfo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainSetupActivity.this, AppInfoActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
				overridePendingTransition(0, 0);
			}
		});
		FrameLayout btn_menu_recomm = (FrameLayout)findViewById(R.id.menu_recomm_btn);
		btn_menu_recomm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainSetupActivity.this, RecommendAppActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
				overridePendingTransition(0, 0);
			}
		});
		FrameLayout menu_room_del_btn = (FrameLayout)findViewById(R.id.menu_room_del_btn);
		menu_room_del_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainSetupActivity.this, DeleteRoomActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				intent.putExtra("room_seq", room_seq);
				startActivity(intent);
				overridePendingTransition(0, 0);
			}
		});
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
		
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode != RESULT_OK){
			return;
		}
		
		switch(requestCode){

		case EDITED_MEMBERINFO:
			
			u_picture = data.getStringExtra("u_picture");
			u_nick = data.getStringExtra("u_nick");
			sns_yn = data.getStringExtra("u_type");
			
			break;
		}
		
	}

}
