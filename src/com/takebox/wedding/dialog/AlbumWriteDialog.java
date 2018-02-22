package com.takebox.wedding.dialog;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.takebox.wedding.CategoryEditData;
import com.takebox.wedding.MainActivity;
import com.takebox.wedding.R;


public class AlbumWriteDialog extends Dialog implements android.view.View.OnClickListener {
	public Context mContext;

	MainActivity mActivity;

	JSONArray cate;
	
	public String cate_seq;
	
	public LinearLayout btn_select_cate;
	public  Button btn_ok;
	public  Button btn_skip;
	
	public  EditText et_content;
	
	private ListView lv_list;
	private Spinner spinner;
	
	public AlbumWriteDialog(Context context, MainActivity activity, JSONArray _cate) {
		super(context, android.R.style.Theme_Black_NoTitleBar);

		mActivity = activity;
		mContext = context;
		cate = _cate;
		
		// TODO Auto-generated constructor stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);  
		setContentView(R.layout.dialog_album_write);
		this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		
		
		//완료
		btn_ok = (Button)findViewById(R.id.pop_ok_btn);
		//그냥올리기
		btn_skip = (Button)findViewById(R.id.pop_skip_btn);
		
		et_content = (EditText)findViewById(R.id.pop_content_edit);
		
		
		//카테고리 선택
		
		lv_list = (ListView)findViewById(R.id.pop_list);
		
		final ArrayList<CategoryEditData> items = new ArrayList<CategoryEditData>();
		
		SelectCategoryAdapter adapter;
		
		
		if(cate==null){
			mActivity.baseToast("사진을 다시 촬영해주세요.");
			return;
		}
		int list_cnt = cate.length();
		
		for(int i=0; i<list_cnt; i++){
			try {
				JSONObject obj = cate.getJSONObject(i);
				
				items.add(new CategoryEditData(obj.getString("name"), obj.getString("cata_seq")));
				
				
				//기본선택 웨딩사진
				if(obj.getString("name").equals("웨딩사진")){
					cate_seq = obj.getString("cata_seq");
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		adapter = new SelectCategoryAdapter(mActivity, R.layout.edit_category_list, items);
		spinner = (Spinner)findViewById(R.id.cate_spinner);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				cate_seq = items.get(position).cata_seq;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
		
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
	}

}