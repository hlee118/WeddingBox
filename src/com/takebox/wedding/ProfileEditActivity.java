package com.takebox.wedding;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.google.analytics.tracking.android.EasyTracker;
import com.takebox.wedding.dialog.AlertDialogBuilder;
import com.takebox.wedding.info.Info;
import com.takebox.wedding.model.MemberModel;
import com.takebox.wedding.model.WeddingModel;
import com.takebox.wedding.util.AndroidUtil;
import com.takebox.wedding.util.CustomTextFont;
import com.takebox.wedding.util.ImageReScale;

public class ProfileEditActivity extends HttpExceptionActivity {
	
	//카메라 액티비티 리턴
	private static final int PICK_FROM_ALBUM1 = 1;
	private static final int PICK_FROM_ALBUM2 = 2;
	private static final int PICK_FROM_ALBUM3 = 3;
	public static final int NAVERMAP = 4;
	public static final int CROP_FROM_ALBUM1 = 5;
	public static final int CROP_FROM_ALBUM2 = 6;
	public static final int CROP_FROM_ALBUM3 = 7;
	private static final int SET_BIO = 8;
	public static final int TEMPLATE_UPDATE = 9;
	public static final int RESULTCODE = 1;
	
	
	private Thread mThread = null;
	EditText name1;		//Bride or Groom's name
	EditText name2;		//Fiance(e)'s name
	TextView Date;		//Wedding Date
	TextView time;
	TextView lengthText;
	EditText ID;		//Wedding ID
	TextView et_bio;	//Wedding Bio
	EditText place;
	public String wed_seq;
	public String room_id;
	public String male_name;
	public String female_name;
	public String wed_date;
	public String wed_time;
	public String description;
	public String img_name;
	public String place_name;
	public String latitude;
	public String longitude;
	public String card_type;
	
	private String file_name = "";

	
	
	private AndroidUtil andUtil = new AndroidUtil();
	public static Bitmap selPhoto = null;
	private String selPhotoPath;
	
	private Context mContext ;
	private Activity mActivity;
	
	private int IMG_FLAG = 0;		//어느 이미지를 업로드하는지 체크
	
	private int deleteNum;
	
	String respUrl;
	int uploadNum;
	
	ArrayList<ImageView> profImgArray;
	ArrayList<ImageView> cancelImgArray;
	ArrayList<Boolean> cancelFlugArray;
	ArrayList<String> profUrlArray;
	
	private Uri uri;
	String path=Environment.getExternalStorageDirectory()+"";
	String img_file_name="temp";
	Uri selPhotoUri;
	
	Button profile_template_edit_btn;
	public boolean data_sending_flag = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_profile_edit); 
	    
	    mContext = ProfileEditActivity.this;
		mActivity = ProfileEditActivity.this;
		
		Intent i =getIntent();
		
	    room_id = i.getStringExtra("room_id");
	    male_name = i.getStringExtra("male_name");
	    female_name = i.getStringExtra("female_name");
	    wed_date = i.getStringExtra("wed_date");
	    wed_time = i.getStringExtra("wed_time");
	    description = i.getStringExtra("description");
	    img_name = i.getStringExtra("img_name");
	    wed_seq = i.getStringExtra("room_seq");
	    latitude = i.getStringExtra("latitude");
	    longitude = i.getStringExtra("longitude");
	    card_type = i.getStringExtra("card_type");
	    
	    
	    profUrlArray = new ArrayList<String>();
	    profUrlArray.add(i.getStringExtra("img_url1"));
	    profUrlArray.add(i.getStringExtra("img_url2"));
	    profUrlArray.add(i.getStringExtra("img_url3"));
	    
	    place_name = i.getStringExtra("place_name");
	    Log.i("PLACE_NAME", place_name+"dd");

	    createUI();
	    
	    Button template_gallery_btn = (Button)findViewById(R.id.template_gallery_btn);
	    template_gallery_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ProfileEditActivity.this, TemplateGalleryActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
				intent.putExtra("room_id", room_id);
				intent.putExtra("wed_seq", wed_seq);
				startActivity(intent);
				overridePendingTransition(0, 0);
			}
		});
	    
	    ImageView template_btn = (ImageView)findViewById(R.id.template_btn);
	    template_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ProfileEditActivity.this, TemplateEditActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
				intent.putExtra("card_type", card_type);
				intent.putExtra("room_id", room_id);
				intent.putExtra("wed_seq", wed_seq);
				startActivityForResult(intent, TEMPLATE_UPDATE);
				overridePendingTransition(0, 0);	
			}
		});
	    
		FrameLayout btn = (FrameLayout)findViewById(R.id.edit_button);
		TextView inner_btn = (TextView)findViewById(R.id.inner_edit_button);
		inner_btn.setTypeface(CustomTextFont.hunsomsatangR);
	    btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//서버 통신
				processParsing(mThread, Edit);
			}
		});
	    
	    profImgArray.get(0).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//사진앨범 다이얼로그
				if(cancelFlugArray.get(0)){
					//이미지 삭제
					deleteImage(0);
				}else{
					doTakeAlbumAction1();
				}
			}
		});
	    
	    profImgArray.get(1).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//사진앨범 다이얼로그		
				if(cancelFlugArray.get(1)){
					deleteImage(1);
				}else{
					doTakeAlbumAction2();
				}
				
				
			}
		});
	    
	    profImgArray.get(2).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//사진앨범 다이얼로그			
				if(cancelFlugArray.get(2)){
					deleteImage(2);
				}else{
					doTakeAlbumAction3();
				}
				
			}
		});
	    
	    Button naver_map = (Button)findViewById(R.id.profile_naver_map);
	    naver_map.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ProfileEditActivity.this, NaverMapActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
				intent.putExtra("latitude", latitude);
				intent.putExtra("longitude", longitude);
				intent.putExtra("flag", "edit");
				startActivityForResult(intent, NAVERMAP);
				overridePendingTransition(0, 0);	
			}
		});
	
	    //날짜선택
	    Date.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DialogDatePicker();
			}
		});
	    
	    //시간선택
	    time.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogTimePicker();
			}
		});
	    
	    setStartImage();
	    
	}
	
	public void deleteImage(int num){
		deleteNum = num;
		profDeleteDialog("이미지를 삭제하시겠습니까?");

	}
	
	
	public void setStartImage(){
		 AQuery aq = new AQuery(ProfileEditActivity.this);

		 for(int i=0; i<profImgArray.size(); i++){
			 if(!profUrlArray.get(i).contains("null")){
				 aq.id(profImgArray.get(i)).image(profUrlArray.get(i), true, true);
				 cancelFlugArray.set(i, true);
				 cancelImgArray.get(i).setVisibility(View.VISIBLE);
			 }else{
				 cancelFlugArray.set(i, false);
				 cancelImgArray.get(i).setVisibility(View.INVISIBLE);
				 profImgArray.get(i).setImageResource(R.drawable.img_cdedit_photoarea);
			 //	 imgv1.setImageResource(R.drawable.img_card_defult); //디폴트 이미지
			 }
			  
		 }
		 
	}
	public void createUI(){
		
		name1 = (EditText)findViewById(R.id.profile_edit_name1);
		name1.setText(male_name);
		name2 = (EditText)findViewById(R.id.profile_edit_name2);
		name2.setText(female_name);
		Date = (TextView)findViewById(R.id.profile_edit_date);
		Date.setText(wed_date);
		time = (TextView)findViewById(R.id.profile_edit_time);
		wed_time = wed_time.substring(0,2)+" : "+wed_time.substring(2,4);
		time.setText(wed_time);
		
		
		et_bio = (TextView)findViewById(R.id.profile_edit_bio);
		et_bio.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(ProfileEditActivity.this, SetupBioActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("bio", et_bio.getText().toString());
				startActivityForResult(intent, SET_BIO);
				overridePendingTransition(0,0);
			}
		});
		
		et_bio.setText(description);
		place = (EditText)findViewById(R.id.profile_edit_place);
		place.setText(place_name);
		
//	     System.out.println(img_url);
		
		profImgArray = new ArrayList<ImageView>();
		ImageView imgv1 = (ImageView)findViewById(R.id.wedding_card_edit_img1);
		imgv1.setScaleType(ScaleType.FIT_XY);
		ImageView imgv2 = (ImageView)findViewById(R.id.wedding_card_edit_img2);
		imgv2.setScaleType(ScaleType.FIT_XY);
		ImageView imgv3 = (ImageView)findViewById(R.id.wedding_card_edit_img3);
		imgv3.setScaleType(ScaleType.FIT_XY);
		profImgArray.add(imgv1);
		profImgArray.add(imgv2);
		profImgArray.add(imgv3);
		
		cancelImgArray = new ArrayList<ImageView>();
		ImageView img01 = (ImageView)findViewById(R.id.cancelImg01);
		ImageView img02 = (ImageView)findViewById(R.id.cancelImg02);
		ImageView img03 = (ImageView)findViewById(R.id.cancelImg03);
		cancelImgArray.add(img01);
		cancelImgArray.add(img02);
		cancelImgArray.add(img03);
		
		cancelFlugArray = new ArrayList<Boolean>();
		for(int i=0; i<3; i++){
			boolean flug = false;
			cancelFlugArray.add(flug);
		}
		
		profile_template_edit_btn = (Button)findViewById(R.id.profile_template_edit_btn);
		profile_template_edit_btn.setText(card_type);
	}
	
	private void doTakeAlbumAction1(){
		System.out.println("doTakeAlbumAction");
		// TODO Auto-generated method stub
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(intent, PICK_FROM_ALBUM1);
		overridePendingTransition(0,0);
	}
	
	private void doTakeAlbumAction2(){
		System.out.println("doTakeAlbumAction");
		// TODO Auto-generated method stub
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(intent, PICK_FROM_ALBUM2);
		overridePendingTransition(0,0);
	}
	
	private void doTakeAlbumAction3(){
		System.out.println("doTakeAlbumAction");
		// TODO Auto-generated method stub
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(intent, PICK_FROM_ALBUM3);
		overridePendingTransition(0,0);
	}
	
	private final Runnable Edit = new Runnable() {
		@Override
		public void run() {
			Edit();
		}
	};
	
	private final Runnable upload_room_img = new Runnable() {
		@Override
		public void run() {
			upload_room_img();
		}
	};
	
	private final Runnable delete_room_img = new Runnable() {
		@Override
		public void run() {
			Map<String,String> data = new HashMap<String,String>();
			data.put("wed_seq", wed_seq);
			String index = String.valueOf(deleteNum+1);
			data.put("pic_no", index);
			if(IMG_FLAG != 0){
				data.put("pic_no", IMG_FLAG+"");
			}
			JSONObject obj = WeddingModel.procRoomImgDelete(data);
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
					
					Message msg = new Message();
					msg.arg1 = deleteNum;
					msg.what = 5;
					handler.sendMessage(msg);
				}else{
					handler.sendEmptyMessage(4);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
	
	//웨딩룸 이미지 수정
	protected void upload_room_img() {
		// TODO Auto-generated method stub
		file_name = "croped_img_" + System.currentTimeMillis();
		
		Map<String,String> data = new HashMap<String,String>();
		data.put("wed_seq", wed_seq);
		data.put("FILE_NAME", file_name);
		if(IMG_FLAG != 0){
			data.put("pic_no", IMG_FLAG+"");
		}
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
				
				Log.i("tag", obj.toString());
				
				respUrl = obj.getString("file");
				
				Log.i("tag", "file : " + respUrl);
				
				handler.sendEmptyMessage(3);
			}else{
				handler.sendEmptyMessage(4);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
	//정보수정
	protected void Edit() {
		// TODO Auto-generated method stub
		Map<String, String> data = new HashMap<String,String>();
		
		male_name = name1.getText().toString();
		female_name = name2.getText().toString();
		wed_date = Date.getText().toString();
		wed_time = time.getText().toString();
		wed_time = wed_time.substring(0,2)+wed_time.substring(5);		//HH : MM
		Log.i("wed_time", wed_time);
		description = et_bio.getText().toString();
		place_name = place.getText().toString();
		
		data.put("wed_seq", wed_seq);
		data.put("room_id",room_id);
		data.put("male_name", male_name);
		data.put("female_name", female_name);
		data.put("wed_date", wed_date);
		data.put("wed_time", wed_time);
		data.put("description", description);
		data.put("place", place_name);
		data.put("latitude", latitude);
		data.put("longtitude", longitude);

		JSONObject obj = MemberModel.MemberInfoUpdate(data);	//사진 외 정보들 저장
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
			System.out.println(info.toString());


			//에러코드 확인
			if(info.equals("ok")){
				//성공
				handler.sendEmptyMessage(1);
			}else{
				//실패
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

				if(pd!=null) pd.cancel(); //등록 성공
				MainActivity.firstExcutePlug = false;
				go_wedding_bio();
				
				break;
			case 2:
				if(pd!=null) pd.cancel();
				//등록 실패

				AlertDialog dialog = AlertDialogBuilder.pop_ok(ProfileEditActivity.this, "텍스트 형식을 확인 부탁드립니다.").create();    // 알림창 객체 생성
				dialog.show();
				
				break;
				
			case 3:

				if(pd!=null) pd.cancel(); //등록 성공
				MainActivity.firstExcutePlug = false;
				data_sending_flag = true;
//				AlertDialog dialog3 = AlertDialogBuilder.pop_ok(ProfileEditActivity.this, "이미지가 업로드 되었습니다. 이미지 터치 시  삭제 가능합니다.").create();    // 알림창 객체 생성
//				dialog3.show();
				Toast.makeText(ProfileEditActivity.this, "이미지가 업로드가 완료되었습니다.", Toast.LENGTH_SHORT).show();
				
				AQuery aq = new AQuery(ProfileEditActivity.this);
				Log.i("tag", "test Url : " + respUrl);
				
				aq.id(profImgArray.get(uploadNum)).image(Info.MASTER_FILE_URL+"/image/"+respUrl, true, true);
				
				profUrlArray.set(uploadNum, Info.MASTER_FILE_URL+"/image/"+respUrl);
				
				
				break;
			case 4:
				if(pd!=null) pd.cancel();
				//등록 실패

				AlertDialog dialog2 = AlertDialogBuilder.pop_ok(ProfileEditActivity.this, "다시 시도해주세요.").create();    // 알림창 객체 생성
				dialog2.show();
				
				break;
				
			case 5:
				if(pd!=null) pd.cancel();
				
				
				profImgArray.get(msg.arg1).setImageResource(R.drawable.img_cdedit_photoarea);
				cancelFlugArray.set(msg.arg1, false);
				cancelImgArray.get(msg.arg1).setVisibility(View.INVISIBLE);
				profUrlArray.set(msg.arg1, "null");
				break;

			}
		}

	};
	

	/**
	 * WEDDING BIO 소개페이지로 이동 
	 */
	protected void go_wedding_bio() {
		
		male_name = name1.getText().toString();
		female_name = name2.getText().toString();
		wed_date = Date.getText().toString();
		wed_time = time.getText().toString();
		wed_time = wed_time.substring(0,2)+wed_time.substring(5,7);
		description = et_bio.getText().toString();
		place_name = place.getText().toString();
		
		// TODO Auto-generated method stub
		Intent resultIntent = new Intent();
		resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		resultIntent.putExtra("room_id", room_id);
		resultIntent.putExtra("male_name", male_name);
		resultIntent.putExtra("female_name", female_name);
		resultIntent.putExtra("wed_date", wed_date);
		resultIntent.putExtra("wed_time", wed_time);
		resultIntent.putExtra("description", description);
		resultIntent.putExtra("img_url1", profUrlArray.get(0));
		resultIntent.putExtra("img_url2", profUrlArray.get(1));
		resultIntent.putExtra("img_url3", profUrlArray.get(2));
		resultIntent.putExtra("place_name", place_name);
		resultIntent.putExtra("latitude", latitude);
		resultIntent.putExtra("longitude", longitude);
		resultIntent.putExtra("card_type", card_type);
		setResult(RESULTCODE, resultIntent);
		finish();
		overridePendingTransition(0, 0);
	}
	
	
	public void profDeleteDialog(String msg){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(ProfileEditActivity.this);     // 여기서 this는 Activity의 this
		// 여기서 부터는 알림창의 속성 설정
		builder.setTitle("")        // 제목 설정
		.setMessage(msg)        // 메세지 설정
		.setCancelable(true)        // 뒤로 버튼 클릭시 취소 가능 설정
		.setNegativeButton("취소", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.setPositiveButton("확인", new DialogInterface.OnClickListener(){       
			// 확인 버튼 클릭시 설정
			public void onClick(DialogInterface dialog, int whichButton){
				//로그아웃
				processParsing(mThread, delete_room_img);
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
		
	}
	
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data){

		if(resultCode != RESULT_OK){
			return;
		}
		
		switch(requestCode){
		case PICK_FROM_ALBUM1:
			
			selPhotoUri = data.getData();
			selPhotoPath = andUtil.getRealPathFromURI(ProfileEditActivity.this, selPhotoUri);
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
			
			startActivityForResult(intent, CROP_FROM_ALBUM1);
			
			break;
			            
		case CROP_FROM_ALBUM1:
			try {
				
//				final Bundle extras = data.getExtras();
				
				// crop된 이미지를 저장하기 위한 파일 경로
				
				String filePath = path + "/" + img_file_name;
				Log.i("filePath", filePath);
				selPhotoUri = Uri.parse(filePath);

//				Uri selPhotoUri = data.getData();
				
				selPhotoPath = andUtil.getRealPathFromURI(ProfileEditActivity.this, selPhotoUri);
				profUrlArray.set(0, selPhotoPath);

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
				uploadNum = 0;
				setImage1();
//				Log.i("selPhoto", "height : "+selPhoto.getHeight()+", width : "+selPhoto.getWidth());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}



			break;
			
		case PICK_FROM_ALBUM2:
			selPhotoUri = data.getData();
			selPhotoPath = andUtil.getRealPathFromURI(ProfileEditActivity.this, selPhotoUri);
			path = selPhotoPath.substring(0, selPhotoPath.lastIndexOf("/"));
			img_file_name = System.currentTimeMillis()+".jpg";
			Log.i("path", path);
			
			intent = new Intent("com.android.camera.action.CROP");
			intent.setDataAndType(data.getData(), "image/*");
			// crop한 이미지를 저장할때 200x200 크기로 저장
			intent.putExtra("outputX", 644); // crop한 이미지의 x축 크기
			intent.putExtra("outputY", 416); // crop한 이미지의 y축 크기
			intent.putExtra("aspectX", 644); // crop 박스의 x축 비율 
			intent.putExtra("aspectY", 416); // crop 박스의 y축 비율
			intent.putExtra("scale", true);
			intent.putExtra("output", selPhotoUri);
			
			File f2 = new File(path, img_file_name);
			try {
				f2.createNewFile();
			} catch (IOException ex) {
				Log.e("io", ex.getMessage());  
			}
			
			uri = Uri.fromFile(f2);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
			
			startActivityForResult(intent, CROP_FROM_ALBUM2);
			
			break;
			
		case CROP_FROM_ALBUM2:
			try {
				
//				final Bundle extras = data.getExtras();
				
				// crop된 이미지를 저장하기 위한 파일 경로
				
				String filePath = path + "/" + img_file_name;
				selPhotoUri = Uri.parse(filePath);

//				Uri selPhotoUri = data.getData();
				
				selPhotoPath = andUtil.getRealPathFromURI(ProfileEditActivity.this, selPhotoUri);
				profUrlArray.set(1, selPhotoPath);

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
				uploadNum = 1;
				setImage2();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}



			break;
			
		case PICK_FROM_ALBUM3:
			
			selPhotoUri = data.getData();
			selPhotoPath = andUtil.getRealPathFromURI(ProfileEditActivity.this, selPhotoUri);
			path = selPhotoPath.substring(0, selPhotoPath.lastIndexOf("/"));
			img_file_name = System.currentTimeMillis()+".jpg";
			Log.i("path", path);
			
			intent = new Intent("com.android.camera.action.CROP");
			intent.setDataAndType(data.getData(), "image/*");
			// crop한 이미지를 저장할때 200x200 크기로 저장
			intent.putExtra("outputX", 644); // crop한 이미지의 x축 크기
			intent.putExtra("outputY", 416); // crop한 이미지의 y축 크기
			intent.putExtra("aspectX", 644); // crop 박스의 x축 비율 
			intent.putExtra("aspectY", 416); // crop 박스의 y축 비율
			intent.putExtra("scale", true);
			intent.putExtra("output", selPhotoUri);
			
			File f3 = new File(path, img_file_name);
			try {
				f3.createNewFile();
			} catch (IOException ex) {
				Log.e("io", ex.getMessage());  
			}
			
			uri = Uri.fromFile(f3);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
			
			startActivityForResult(intent, CROP_FROM_ALBUM3);
			
			break;
			
		case CROP_FROM_ALBUM3:
			try {
				
//				final Bundle extras = data.getExtras();
				
				// crop된 이미지를 저장하기 위한 파일 경로
				
				String filePath = path + "/" + img_file_name;
				selPhotoUri = Uri.parse(filePath);

//				Uri selPhotoUri = data.getData();
				
				selPhotoPath = andUtil.getRealPathFromURI(ProfileEditActivity.this, selPhotoUri);
				profUrlArray.set(2, selPhotoPath);

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
				uploadNum = 2;
				setImage3();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;
			
		case NAVERMAP:
				
			latitude = data.getDoubleExtra("latitude", 126.978371)+"";
			longitude = data.getDoubleExtra("longitude", 37.5666091)+"";
			Log.i("data", latitude + ", " + longitude + ", " + place);
			break;
			
		case SET_BIO:
			et_bio.setText(data.getExtras().getString("bio"));
			break;
			
		case TEMPLATE_UPDATE:
			card_type = data.getExtras().getString("card_type");
			profile_template_edit_btn.setText(card_type);
			data_sending_flag = true;
			MainActivity.firstExcutePlug = false;
			break;
		}
		
		
		
	
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
	public void setCancelWorth(int num){
		cancelFlugArray.set(num, true);
		cancelImgArray.get(num).setVisibility(View.VISIBLE);
		
	}
	
	
	public void setImage1()
	{


		//미디어 스캐닝
//		sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
//				Uri.parse("file://" + Environment.getExternalStorageDirectory())));		
//		try {
//			//mPhotoButton.setVisibility(View.GONE);
//			//mPhotoImageView.setVisibility(View.VISIBLE);
//
//			Bitmap tbm = null;
//			if(selPhoto != null) 
//			{
//				// 이미지를 상황에 맞게 회전시킨다
//				selPhoto = andUtil.bitmapRotate(selPhotoPath, selPhoto);
//
//				//imageview에 큰 사이트가 들어가면 out of memory가 걸려서 축소시킨다.
//				tbm = Bitmap.createScaledBitmap(selPhoto, 200, 200, true);
//				//mPhotoImageView.setImageBitmap(tbm);
//			} else {
//
//				File file = new File(selPhotoPath);
//				if(file.exists()) {
//					//이미지 사이즈 화면 사이즈에 맞게 리스케일
//					ImageReScale imgReScale = new ImageReScale();
//					selPhoto = imgReScale.loadBackgroundBitmap(ProfileEditActivity.this, selPhotoPath);	
//					selPhoto = andUtil.bitmapRotate(selPhotoPath, selPhoto);
//
//					tbm = Bitmap.createScaledBitmap(selPhoto, 200, 200, true);
//					//mPhotoImageView.setImageBitmap(tbm);
//				}
//			}
//
////			file_name = CommonUtil.getFileNameWithoutExtension(selPhotoPath);
//
//			setCancelWorth(0);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			System.out.println("error" + e);
//		}

//		//웨딩룸 사진 수정
		IMG_FLAG = 1;
		
		processParsing(mThread, upload_room_img);

	}
	
	
	public void setImage2()
	{

//		try {
//			//mPhotoButton.setVisibility(View.GONE);
//			//mPhotoImageView.setVisibility(View.VISIBLE);
//
//			Bitmap tbm = null;
//			if(selPhoto != null) 
//			{
//				// 이미지를 상황에 맞게 회전시킨다
//				selPhoto = andUtil.bitmapRotate(selPhotoPath, selPhoto);
//
//				//imageview에 큰 사이트가 들어가면 out of memory가 걸려서 축소시킨다.
//				tbm = Bitmap.createScaledBitmap(selPhoto, 200, 200, true);
//				//mPhotoImageView.setImageBitmap(tbm);
//			} else {
//
//				File file = new File(selPhotoPath);
//				if(file.exists()) {
//					//이미지 사이즈 화면 사이즈에 맞게 리스케일
//					ImageReScale imgReScale = new ImageReScale();
//					selPhoto = imgReScale.loadBackgroundBitmap(ProfileEditActivity.this, selPhotoPath);	
//					selPhoto = andUtil.bitmapRotate(selPhotoPath, selPhoto);
//
//					tbm = Bitmap.createScaledBitmap(selPhoto, 200, 200, true);
//					//mPhotoImageView.setImageBitmap(tbm);
//				}
//			}
//
//			
////			file_name = CommonUtil.getFileNameWithoutExtension(selPhotoPath);
//			
//
//			setCancelWorth(1);
//			
//			
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			System.out.println("error" + e);
//
//		}


//		//웨딩룸 사진 수정
		IMG_FLAG = 2;
		processParsing(mThread, upload_room_img);
//

	}
	
	public void setImage3()
	{

//		try {
//			//mPhotoButton.setVisibility(View.GONE);
//			//mPhotoImageView.setVisibility(View.VISIBLE);
//
//			Bitmap tbm = null;
//			if(selPhoto != null) 
//			{
//				// 이미지를 상황에 맞게 회전시킨다
//				selPhoto = andUtil.bitmapRotate(selPhotoPath, selPhoto);
//
//				//imageview에 큰 사이트가 들어가면 out of memory가 걸려서 축소시킨다.
//				tbm = Bitmap.createScaledBitmap(selPhoto, 200, 200, true);
//				//mPhotoImageView.setImageBitmap(tbm);
//			} else {
//
//				File file = new File(selPhotoPath);
//				if(file.exists()) {
//					//이미지 사이즈 화면 사이즈에 맞게 리스케일
//					ImageReScale imgReScale = new ImageReScale();
//					selPhoto = imgReScale.loadBackgroundBitmap(ProfileEditActivity.this, selPhotoPath);	
//					selPhoto = andUtil.bitmapRotate(selPhotoPath, selPhoto);
//
//					tbm = Bitmap.createScaledBitmap(selPhoto, 200, 200, true);
//					//mPhotoImageView.setImageBitmap(tbm);
//				}
//			}
//
//			
////			file_name = CommonUtil.getFileNameWithoutExtension(selPhotoPath);
////			profImgArray.get(2).invalidate();
//			setCancelWorth(2);
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			System.out.println("error" + e);
//		}

//		//웨딩룸 사진 수정
		IMG_FLAG = 3;
		processParsing(mThread, upload_room_img);
//

	}

	
	private void DialogDatePicker(){
	    Calendar c = Calendar.getInstance();
	    int cyear = c.get(Calendar.YEAR);
	    int cmonth = c.get(Calendar.MONTH);
	    int cday = c.get(Calendar.DAY_OF_MONTH);
	     
	    DatePickerDialog.OnDateSetListener mDateSetListener = 
	    new DatePickerDialog.OnDateSetListener() {
	    // onDateSet method
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				String date_selected = String.valueOf(year)+"-"+String.format("%02d", monthOfYear+1)+
						"-"+String.format("%02d", dayOfMonth);
				
				Date.setText(date_selected);
			}
	    };
		DatePickerDialog alert = new DatePickerDialog(this,  mDateSetListener,  
		cyear, cmonth, cday);
		alert.show();
	}
	
	//시간 선택 다이얼로그
	protected void DialogTimePicker() {
		// TODO Auto-generated method stub
		TimePickerDialog.OnTimeSetListener mTimeSetListener = 
				new TimePickerDialog.OnTimeSetListener() {
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

				String _time = ""+String.format("%02d", hourOfDay) + String.format("%02d", minute);
				System.out.println("time = " + _time);

				//버튼에 시간 출력
//				time.setText(_time.substring(0, 2) + _time.substring(2, 4));
				time.setText(_time.substring(0, 2) + " : " + _time.substring(2, 4));
			}
		};
		TimePickerDialog alert = new TimePickerDialog(this, 
				mTimeSetListener, 12, 0, true);
		alert.show();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(data_sending_flag){
			data_sending_flag = false;
			go_wedding_bio();
		}else{
			super.onBackPressed();
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
}
