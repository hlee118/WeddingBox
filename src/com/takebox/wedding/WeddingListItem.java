package com.takebox.wedding;

public class WeddingListItem{

	public String room_id;
	public int room_seq;
	public String room_img;
	public String content;
	public String idx;
	
	public WeddingListItem (String room_id, String room_img){
		this.room_id = room_id;
		this.room_img = room_img;
	}

}