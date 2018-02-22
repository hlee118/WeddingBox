package com.takebox.wedding;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.Theme;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.google.analytics.tracking.android.EasyTracker;
import com.luminous.pick.Action;
import com.luminous.pick.CustomGallery;
import com.takebox.wedding.custom.CustomCropImage;
import com.takebox.wedding.custom.CustomCropImageActivity;
import com.takebox.wedding.dialog.AlertDialogBuilder;
import com.takebox.wedding.dialog.PhotoAlbumDialog;
import com.takebox.wedding.info.Info;
import com.takebox.wedding.info.User;
import com.takebox.wedding.model.MemberModel;
import com.takebox.wedding.model.WeddingModel;
import com.takebox.wedding.util.AndroidUtil;
import com.takebox.wedding.util.CommonUtil;
import com.takebox.wedding.util.ImageReScale;
import com.takebox.wedding.R;

public class JoinSetupProfileActivity extends HttpExceptionActivity {


	//카메라 액티비티 리턴
	private static final int PICK_FROM_CAMERA = 0;
	private static final int PICK_FROM_ALBUM = 1;
	private static final int PICK_FROM_MULTI_ALBUM = 2;
	
	private static final int CROP_FROM_CAMERA = 5;
	private static final int REQUEST_ID = 3;
	private static final int SET_BIO = 4;
	private static final int CROP_FROM_ALBUM =6;
	public static final int TEMPLATE_UPDATE = 7;


	private Context mContext ;
	private Activity mActivity;

	
	public static JoinSetupProfileActivity instance = null;

	private AndroidUtil andUtil = new AndroidUtil();
	public static Bitmap selPhoto = null;
	private String selPhotoPath;

	private ImageView btn_photo;
	private String file_name = "";
	
	private TextView lengthText;
	private final int maxLength = 200;
	TextView et_bio;

	private String _room_id; //웨딩룸 이름
	private String _room_seq; //웨딩룸 번호
	private String card_type = "basic";
	Button join_profile_template_edit_btn;

	private Thread mThread = null;


	private ByteArrayOutputStream [] bos = null;
	private Bitmap [] bm = null;
	
//	private String outFilePath = CustomCropImage.outFilePath;
	public static String outFilePath = "";
	private Uri mImageCaptureUri;
	Bitmap photo;
	
	String[] all_path =null;
	
	Button btn_id_edit;
	
	private Uri uri;
	Uri profilePhotoUri;
	String profilePhotoPath;
	String path=Environment.getExternalStorageDirectory()+"";
	String img_file_name="temp";
	public static Bitmap profilePhoto = null;
	Uri selPhotoUri;
	String respUrl;
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MainActivity.firstExcutePlug = true;
	}
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		mContext = JoinSetupProfileActivity.this;
		mActivity = JoinSetupProfileActivity.this;
		setContentView(R.layout.activity_join_setup_profile);
		
		//웨딩룸 이름번호
		if(getIntent().getStringExtra("ROOM_ID") == null){
			_room_id = User.getUserInfo("ROOM_ID", JoinSetupProfileActivity.this);
		}else{
			_room_id = getIntent().getStringExtra("ROOM_ID");
		}


		if(getIntent().getStringExtra("ROOM_SEQ") == null){
			_room_seq = User.getUserInfo("ROOM_SEQ", JoinSetupProfileActivity.this);
		}else{
			_room_seq = getIntent().getStringExtra("ROOM_SEQ");
		}
		
		Button template_gallery_btn = (Button)findViewById(R.id.join_template_gallery_btn);
	    template_gallery_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(JoinSetupProfileActivity.this, TemplateGalleryActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
				intent.putExtra("room_id", _room_id);
				intent.putExtra("wed_seq", _room_seq);
				startActivity(intent);
				overridePendingTransition(0, 0);	
			}
		});

		ImageView template_btn = (ImageView)findViewById(R.id.join_template_btn);
	    template_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(JoinSetupProfileActivity.this, TemplateEditActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
				intent.putExtra("card_type", card_type);
				intent.putExtra("room_id", _room_id);
				intent.putExtra("wed_seq", _room_seq);
				startActivityForResult(intent, TEMPLATE_UPDATE);
				overridePendingTransition(0, 0);	
			}
		});
	    
	    join_profile_template_edit_btn = (Button)findViewById(R.id.join_profile_template_edit_btn);

		//아이디 변경
		btn_id_edit = (Button)findViewById(R.id.profile_id_edit_btn);
		btn_id_edit.setText(_room_id);
		btn_id_edit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				go_setup_id();
			}
		});

		//사진 앨범 버튼
		btn_photo = (ImageView)findViewById(R.id.profile_image_add_btn);
		btn_photo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//사진앨범 다이얼로그
//				createInitialJPEG();
				Intent intent = new Intent(Intent.ACTION_PICK);
				intent.setType("image/*");
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivityForResult(intent, PICK_FROM_ALBUM);
				overridePendingTransition(0,0);
			}	
		});



		//다음 버튼
		Button btn_next = (Button)findViewById(R.id.profile_next_btn);
		btn_next.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub


				//
				//processParsing(mThread, update_wedding_room);

				//설명 저장 후 다음 화면
				processParsing(mThread, update_room_des);


			}
		});
		
		baseToast("위 이미지를 누르면 사진 수정이 가능합니다.");
		
		
		
		
		et_bio = (TextView)findViewById(R.id.profile_bio_edit);
		et_bio.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(JoinSetupProfileActivity.this, SetupBioActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("bio", et_bio.getText().toString());
				startActivityForResult(intent, SET_BIO);
				overridePendingTransition(0,0);
			}
		});
				
		processParsing(mThread, upload_initialroom_img);
		
		
	}


	public void createInitialPath(){
		File file = new File(Info.filePath);
		if(!file.exists()){
			file.mkdirs(); 
		}
	}
	
	public void createInitialJPEG(){
		outFilePath = Info.filePath +"/tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
	}
	
	
	



	//가입 단계 중 공유(마지막페이지)로 이동
	protected void go_setup_share() {
		SharedPreferences saveData = getSharedPreferences("appInfoData", MODE_PRIVATE);
		SharedPreferences.Editor saveEdit = saveData.edit();
		saveEdit.putBoolean(InitialAppInfoActivity.kindOfActivity[0], false);
		saveEdit.commit();
		
		Intent intent = new Intent(JoinSetupProfileActivity.this, MainActivity.class);
		intent.putExtra("ROOM_ID", _room_id);
		intent.putExtra("ROOM_SEQ", _room_seq);
		
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
		finish();
		overridePendingTransition(0, 0);
	}


	/**
	 * 아이디 변경으로 가기
	 */
	protected void go_setup_id() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(JoinSetupProfileActivity.this, JoinSetupIDActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(JoinSetupIDActivity.SETUP_ID, _room_id);
		startActivityForResult(intent, REQUEST_ID);
		overridePendingTransition(0, 0);
	}



	protected void onActivityResult(int requestCode, int resultCode, Intent data){

		if(resultCode != RESULT_OK){
			return;
		}

		switch(requestCode){
		
		
		/* 엽이형 크롭부분
		
		
		case CROP_FROM_CAMERA : {
			outFilePath = outFilePath;
			photo  = BitmapFactory.decodeFile(outFilePath);
			btn_photo.setImageBitmap(photo);
			processParsing(mThread, upload_room_img);
			break;
		}
		
		case PICK_FROM_ALBUM:
			System.out.println("pick_from_album data !!!");

			try {

//				Uri selPhotoUri = data.getData();
//
//				selPhotoPath = andUtil.getRealPathFromURI(this, selPhotoUri);
//
//				BitmapFactory.Options options = new BitmapFactory.Options();
//				options.inPurgeable = true;
//
//				if(selPhoto != null){
//					selPhoto.recycle();
//					selPhoto = null;
//				}
//
//				//이미지 사이즈 화면 사이즈에 맞게 리스케일
//				ImageReScale imgReScale = new ImageReScale();
//				selPhoto = imgReScale.loadBackgroundBitmap(getApplicationContext(), selPhotoPath);	
//				setImage();
				
				Bitmap photo=null;
				if(requestCode == PICK_FROM_ALBUM) {
					mImageCaptureUri = data.getData();
					try {
						photo = Images.Media.getBitmap(getContentResolver(), mImageCaptureUri); 
					} catch(Exception e) {Log.i("tag", "PICK_FROM_ALBUM : " + e.toString());}	
				}
				
				if(requestCode == PICK_FROM_CAMERA) {
					Bundle extras = data.getExtras();
					photo = extras.getParcelable("data");
					Log.e("tag", "PICK_FROM_CAMERA size : " + photo.getHeight()*photo.getWidth()); 
				}
				
				if(photo != null)
					try {
						FileOutputStream fos = new FileOutputStream(outFilePath);
						photo.compress(CompressFormat.JPEG, 100, fos); 
						fos.flush(); 
						fos.close();
					} catch(Exception e) {Log.i("tag", "" + requestCode + " : " + e.toString());}
				else {
					Log.e("tag", "Bitmap is null");
					return;
				}
				

//				Intent intent = new Intent("com.android.camera.action.CROP");
//				intent.setDataAndType(mImageCaptureUri, "image/*");
//				intent.putExtra("outputX", 90);
//				intent.putExtra("outputY", 90);
//				intent.putExtra("aspectX", 0);
//				intent.putExtra("aspectY", 0);
//				intent.putExtra("scale", true);
//				intent.putExtra("return-data", true);

				Intent intent = new Intent(this, CustomCropImageActivity.class);
				startActivityForResult(intent, CROP_FROM_CAMERA);
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;
			
		*/
		/*
		 * 내장크롭
		 * written by 현식
		 */
		case PICK_FROM_ALBUM:
				
			selPhotoUri = data.getData();
			selPhotoPath = andUtil.getRealPathFromURI(JoinSetupProfileActivity.this, selPhotoUri);
			path = selPhotoPath.substring(0, selPhotoPath.lastIndexOf("/"));
			img_file_name = System.currentTimeMillis()+".jpg";
			Log.i("path", path);
			
			Intent intent = new Intent("com.android.camera.action.CROP");
			intent.setDataAndType(data.getData(), "image/*");
			intent.putExtra("outputX", 644); // crop한 이미지의 x축 크기
			intent.putExtra("outputY", 416); // crop한 이미지의 y축 크기
			intent.putExtra("aspectX", 644); // crop 박스의 x축 비율 
			intent.putExtra("aspectY", 415); // crop 박스의 y축 비율
			intent.putExtra("scale", true);
			intent.putExtra("output", selPhotoUri);
			
			File f = new File(path, "/"+img_file_name);
			try {
				f.createNewFile();
			} catch (IOException ex) {
				Log.e("io", ex.getMessage());  
			}
			
			uri = Uri.fromFile(f);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
			
			startActivityForResult(intent, CROP_FROM_ALBUM);
			
			break;

		case CROP_FROM_ALBUM:
			try {
				
//				final Bundle extras = data.getExtras();
				
				// crop된 이미지를 저장하기 위한 파일 경로
				
				String filePath = path + "/" + img_file_name;
				Log.i("filePath", filePath);
				selPhotoUri = Uri.parse(filePath);

//				Uri selPhotoUri = data.getData();
				
				selPhotoPath = andUtil.getRealPathFromURI(JoinSetupProfileActivity.this, selPhotoUri);

				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inPurgeable = true;

				if(selPhoto != null){
					selPhoto.recycle();
					selPhoto = null;
				}
				
//				if (extras != null) {
//					selPhoto = extras.getParcelable("data"); // crop된 bitmap 
//				}

				//이미지 사이즈 화면 사이즈에 맞게 리스케일
				ImageReScale imgReScale2 = new ImageReScale();
				selPhoto = imgReScale2.loadBackgroundBitmap(getApplicationContext(), selPhotoPath);	
				setImage();
//				Log.i("selPhoto", "height : "+selPhoto.getHeight()+", width : "+selPhoto.getWidth());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}



			break;
			
			
		case REQUEST_ID:
			btn_id_edit.setText(data.getExtras().getString("ROOM_ID"));
			_room_id = data.getExtras().getString("ROOM_ID");
			Log.i("tag", _room_id);
			
			break;
			
		case SET_BIO:
			et_bio.setText(data.getExtras().getString("bio"));
			break;
			
		case TEMPLATE_UPDATE:
			card_type = data.getExtras().getString("card_type");
			join_profile_template_edit_btn.setText(card_type);
			break;
		}
		
		
		super.onActivityResult(requestCode, resultCode, data);
	}


	
	
	public void setImage()
	{

/*
		//미디어 스캐닝
//		sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
//				Uri.parse("file://" + Environment.getExternalStorageDirectory())));		
		try {
			//mPhotoButton.setVisibility(View.GONE);
			//mPhotoImageView.setVisibility(View.VISIBLE);

			Bitmap tbm = null;
			if(selPhoto != null) 
			{
				// 이미지를 상황에 맞게 회전시킨다
				selPhoto = andUtil.bitmapRotate(selPhotoPath, selPhoto);

				//imageview에 큰 사이트가 들어가면 out of memory가 걸려서 축소시킨다.
				tbm = Bitmap.createScaledBitmap(selPhoto, 730, 598, true);
				//mPhotoImageView.setImageBitmap(tbm);
			} else {

				File file = new File(selPhotoPath);
				if(file.exists()) {
					//이미지 사이즈 화면 사이즈에 맞게 리스케일
					ImageReScale imgReScale = new ImageReScale();
					selPhoto = imgReScale.loadBackgroundBitmap(this, selPhotoPath);	
					selPhoto = andUtil.bitmapRotate(selPhotoPath, selPhoto);

					tbm = Bitmap.createScaledBitmap(selPhoto, 730, 598, true);
					//mPhotoImageView.setImageBitmap(tbm);
				}
			}

			file_name = CommonUtil.getFileNameWithoutExtension(selPhotoPath);

			btn_photo.setImageBitmap(tbm);
			btn_photo.invalidate();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("error" + e);
		}
		
		*/

//		//웨딩룸 사진 수정
		processParsing(mThread, upload_room_img);
	}

	

	private final Runnable update_wedding_room = new Runnable() {
		@Override
		public void run() {
			update_wedding_room();
		}
	};


	private final Runnable upload_room_img = new Runnable() {
		@Override
		public void run() {
			upload_room_img();
		}
	};
	
	private final Runnable upload_initialroom_img = new Runnable() {
		@Override
		public void run() {
			upload_initialroom_img();
		}
	};



	private final Runnable update_room_des = new Runnable() {
		@Override
		public void run() {
			update_wedding_room_des();
		}
	};


	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(final Message msg) {

			switch(msg.what) {

			case 1:

				if(pd!=null) pd.cancel();
				//이미지 등록 성공
				
				//Toast.makeText(getApplicationContext(), "등록되었습니다.", Toast.LENGTH_SHORT).show();

				break;
			case 2:
				if(pd!=null) pd.cancel();
				//이미지 등록 실패
				
				AlertDialog dialog = AlertDialogBuilder.pop_ok(JoinSetupProfileActivity.this, "다시 확인해 주세요.").create();    // 알림창 객체 생성
				dialog.show();   

				break;
			case 3:
				if(pd!=null) pd.cancel();
				//업데이트 성공
				go_setup_share();

				break;
			case 4:
				if(pd!=null) pd.cancel();
				//업데이트 실패

				AlertDialog dialog2 = AlertDialogBuilder.pop_ok(JoinSetupProfileActivity.this, "다시 확인해 주세요.").create();    // 알림창 객체 생성
				dialog2.show(); 
				break;
			case 5:
				if(pd!=null) pd.cancel();
				//웨딩룸 설명 수정 성공
				
				go_setup_share();
				
				break;
			case 6:
				if(pd!=null) pd.cancel();
				
				AlertDialog dialog3 = AlertDialogBuilder.pop_ok(JoinSetupProfileActivity.this, "텍스트 형식을 확인 부탁드립니다.").create();    // 알림창 객체 생성
				dialog3.show(); 
				
				//웨딩룸 설명 수정 실패

				break;
				
			case 7:
				if(pd!=null) pd.cancel();
				AQuery aq = new AQuery(JoinSetupProfileActivity.this);
				aq.id(btn_photo).image(Info.MASTER_FILE_URL+"/image/"+respUrl, true, true);
				
				break;
			case 13:
				if(pd!=null) pd.cancel();
				

				break;

			}
		}
	};



	//웨딩룸 이미지 수정
	protected void upload_room_img() {
		// TODO Auto-generated method stub

file_name = "croped_img_" + System.currentTimeMillis();
		
		Map<String,String> data = new HashMap<String,String>();
		data.put("wed_seq", _room_seq);
		data.put("FILE_NAME", file_name);
		data.put("pic_no", "1");
		JSONObject obj = WeddingModel.procRoomImgUpload(data, selPhoto);
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
			if(obj.getString("info").equals("ok")){
				respUrl = obj.getString("file");
				handler.sendEmptyMessage(7);
			}else{
				handler.sendEmptyMessage(2);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
	//웨딩룸 이미지 수정
		protected void upload_initialroom_img() {
			// TODO Auto-generated method stub

			Map<String,String> data = new HashMap<String,String>();
			data.put("wed_seq", _room_seq);
			data.put("pic_no", "1");
			data.put("FILE_NAME", "img_card_default");
			
			instance = this;
			Bitmap bt = BitmapFactory.decodeResource(JoinSetupProfileActivity.instance.getResources(), R.drawable.img_card_defult);
			
			JSONObject obj = WeddingModel.procRoomImgUpload(data, bt);// 이부분이부분이부분!!!!

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
				if(obj.getString("info").equals("ok")){
					handler.sendEmptyMessage(1);
				}else{
					handler.sendEmptyMessage(2);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


		}
	
	
	//웨딩룸 수정하기
	protected void update_wedding_room() {
		// TODO Auto-generated method stub
		

		String bio = et_bio.getText().toString();


		Map<String,String> data = new HashMap<String,String>();
		data.put("bio", bio);
		//		data.put("pre_room_id", _room_id);
		//		data.put("room_id", _room_id);

		JSONObject obj = MemberModel.updateId(data);
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
			if(obj.getString("info").equals("ok")){

				handler.sendEmptyMessage(3);

			}else{
				handler.sendEmptyMessage(4);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}







	//웨딩룸 설명 수정하기
	protected void update_wedding_room_des() {
		// TODO Auto-generated method stub

		//설명 글

		String bio = et_bio.getText().toString();
		
		if(bio.equals(""))
			bio = "사랑이란 이름으로 두 사람이 하나 되는 아름다운 날입니다. 부디 참석하셔서 자리를 빛내주시고, 사진도 많이 찍어주세요.";
		

		Map<String,String> data = new HashMap<String,String>();
		data.put("wed_seq", _room_seq);
		data.put("bio", bio);


		JSONObject obj = WeddingModel.procUpdateRoomDes(data);
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
			if(obj.getString("info").equals("ok")){

				handler.sendEmptyMessage(5);

			}else{
				handler.sendEmptyMessage(6);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
//	@Override
//	public void onBackPressed() {
//		// TODO Auto-generated method stub
//		Toast.makeText(JoinSetupProfileActivity.this, "방설정을 마쳐주세요.", Toast.LENGTH_SHORT).show();
//	}


}


