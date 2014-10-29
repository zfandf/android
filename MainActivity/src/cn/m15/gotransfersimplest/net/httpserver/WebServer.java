package cn.m15.gotransfersimplest.net.httpserver;

import org.json.JSONArray;

import android.content.Context;

public class WebServer {
	
	private static WebServer mInstance = null;
	private GoNanoHTTPD server = null;
	
	private WebServer() {}
	
	public static WebServer getInstance() {
		if (null == mInstance) {
			WebServer.mInstance = new WebServer();
		}
		return WebServer.mInstance;
	}
	
	public GoNanoHTTPD getServer(Context context, int port, JSONArray data) {
		if (null == server) {
			server = new GoNanoHTTPD(context, port, data);
		}
		return server;
	}
}