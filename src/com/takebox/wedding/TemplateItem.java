package com.takebox.wedding;

import org.json.JSONException;
import org.json.JSONObject;

public class TemplateItem {
	JSONObject	obj1;
	JSONObject	obj2;
	JSONObject	obj3;
	String view_type1 = "준비중";
	String type1 = "#";
	String save_path1 = "#";
	String view_type2= "준비중";
	String type2 = "#";
	String save_path2 = "#";
	String view_type3 = "준비중";
	String type3 = "#";
	String save_path3= "#";
	String cash_yn1 = "N";
	String cash_yn2 = "N";
	String cash_yn3 = "N";
	String cash1 = "";
	String cash2 = "";
	String cash3 = "";
	int btn_radio1 = R.drawable.btn_radio;
	int btn_radio2 = R.drawable.btn_radio;
	int btn_radio3 = R.drawable.btn_radio;
	int no;
	
	TemplateItem(JSONObject obj1, JSONObject obj2, JSONObject obj3){
		this.obj1 = obj1;
		this.obj2 = obj2;
		this.obj3 = obj3;
		try {
			if(obj1 != null){
				this.view_type1 = obj1.getString("view_type");
				this.type1 = obj1.getString("type");
				this.save_path1 = obj1.getString("save_path");
				this.cash_yn1 = obj1.getString("cash_yn");
				this.cash1 = obj1.getString("cash");
			}
			if(obj2 != null){
				this.view_type2 = obj2.getString("view_type");
				this.type2 = obj2.getString("type");
				this.save_path2 = obj2.getString("save_path");
				this.cash_yn2 = obj2.getString("cash_yn");
				this.cash2 = obj2.getString("cash");
			}
			if(obj3 != null){
				this.view_type3 = obj3.getString("view_type");
				this.type3 = obj3.getString("type");
				this.save_path3 = obj3.getString("save_path");
				this.cash_yn3 = obj3.getString("cash_yn");
				this.cash3 = obj3.getString("cash");
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	TemplateItem(JSONObject obj1, JSONObject obj2, JSONObject obj3, int drawable, int no){
		this.obj1 = obj1;
		this.obj2 = obj2;
		this.obj3 = obj3;
		this.no = no;
		try {
			if(obj1 != null){
				this.view_type1 = obj1.getString("view_type");
				this.type1 = obj1.getString("type");
				this.save_path1 = obj1.getString("save_path");
				this.cash_yn1 = obj1.getString("cash_yn");
				this.cash1 = obj1.getString("cash");
			}
			if(obj2 != null){
				this.view_type2 = obj2.getString("view_type");
				this.type2 = obj2.getString("type");
				this.save_path2 = obj2.getString("save_path");
				this.cash_yn2 = obj2.getString("cash_yn");
				this.cash2 = obj2.getString("cash");
			}
			if(obj3 != null){
				this.view_type3 = obj3.getString("view_type");
				this.type3 = obj3.getString("type");
				this.save_path3 = obj3.getString("save_path");
				this.cash_yn3 = obj3.getString("cash_yn");
				this.cash3 = obj3.getString("cash");
			}
			
			switch (no){
				case 1:
					this.btn_radio1 = R.drawable.btn_radio_on;
					break;
				case 2:
					this.btn_radio2 = R.drawable.btn_radio_on;
					break;
				case 3:
					this.btn_radio3 = R.drawable.btn_radio_on;
					break;
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}