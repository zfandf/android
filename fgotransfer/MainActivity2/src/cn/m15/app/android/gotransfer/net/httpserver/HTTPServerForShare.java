package cn.m15.app.android.gotransfer.net.httpserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;

public class HTTPServerForShare extends NanoHTTPD {
    public HTTPServerForShare(String apkPath, Context context) {
        super(8888);
        this.mAPKPathsString = apkPath;
        this.mContext = context;
    }

    public void setAPKPath(String apkPath) {
        mAPKPathsString = apkPath;
    }

    /**
     * Common mime type for dynamic content: binary
     */
    public static final String MIME_DEFAULT_BINARY = "application/octet-stream";
    /**
     * Default Index file names.
     */
    public static final String INDEX_FILE_NAME = "index.html";
    public static final String APK_FILE_NAME = "gotransfer.apk";
    
    private String mAPKPathsString;
    private Context mContext;

    public Response serve(IHTTPSession session) {
        Map<String, String> header = session.getHeaders();
        //Map<String, String> parms = session.getParms();
        String uri = session.getUri();
        return respond(Collections.unmodifiableMap(header), session, uri);
    }

    private Response respond(Map<String, String> headers, IHTTPSession session, String uri) {
        // Remove URL arguments
        uri = uri.trim().replace(File.separatorChar, '/');
        if (uri.indexOf('?') >= 0) {
            uri = uri.substring(0, uri.indexOf('?'));
        }
        
        if (uri.equals("/")) {
        	return respond(headers, session, uri + INDEX_FILE_NAME);
        }
        
        if (uri.indexOf(APK_FILE_NAME) >= 0) {
        	File apk = new File(mAPKPathsString);
        	if (apk.exists()) {
        		FileInputStream apkStream = null;
				try {
					apkStream = new FileInputStream(apk);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				if (apkStream != null) {
					return serveFile(headers, apkStream, getMimeTypeForFile(uri));
				} else {
	        		return getNotFoundResponse();
	        	}
        	} else {
        		return getNotFoundResponse();
        	}
        }
        
        InputStream inputStream = null;
		try {
			inputStream = mContext.getAssets().open("wwwroot" + uri);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	if (inputStream != null) {
    		return serveFile(headers, inputStream, getMimeTypeForFile(uri));
    	} else {
    		return getNotFoundResponse();
    	}
    }

    /**
     * Serves file from homeDir and its' subdirectories (only). Uses only URI, ignores all headers and HTTP parameters.
     */
    Response serveFile(Map<String, String> header, InputStream inputStream, String mimeType) {
        return createResponse(Response.Status.OK, mimeType, inputStream);
    }

    // Announce that the file server accepts partial content requests
    private Response createResponse(Response.Status status, String mimeType, InputStream message) {
        Response res = new Response(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

    // Announce that the file server accepts partial content requests
    private Response createResponse(Response.Status status, String mimeType, String message) {
        Response res = new Response(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }
    
    protected Response getNotFoundResponse() {
        return createResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT,
            "Error 404, file not found.");
    }
    
    private static final Map<String, String> MIME_TYPES = new HashMap<String, String>() {{
        put("apk", "application/vnd.android.package-archive");
        put("html", "text/html");
        put("jpg", "image/jpeg");
    }};
    
    // Get MIME type from file name extension, if possible
    private String getMimeTypeForFile(String uri) {
        int dot = uri.lastIndexOf('.');
        String mime = null;
        if (dot >= 0) {
            mime = MIME_TYPES.get(uri.substring(dot + 1).toLowerCase());
        }
        return mime == null ? MIME_DEFAULT_BINARY : mime;
    }
}