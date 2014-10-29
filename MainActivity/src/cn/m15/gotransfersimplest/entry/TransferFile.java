package cn.m15.gotransfersimplest.entry;

import org.json.JSONException;
import org.json.JSONObject;

public class TransferFile {
	public String path;
	public String name;
	public String size;
	public String duration;
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		try {
			json.put("path", path);
			json.put("name", name);
			json.put("size", size);
			json.put("duration", duration);
		} catch (JSONException e) {
		}
		return json;
	}
	
	@Override
	public String toString() {
		return "TransferFile [path=" + path + ", name=" + name + ", size=" + size + ", duration="
				+ duration + "]";
	}
}