package cn.m15.app.android.gotransfer.ui.activity;

import java.lang.ref.WeakReference;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import cn.m15.app.android.gotransfer.R;

@SuppressLint("SetJavaScriptEnabled")
public class CommonWebViewActivity extends BaseActivity implements OnClickListener {
    private static final String PARAM = "session=";
    private ProgressBar mProgressBar;
    private WebView mWebView;
    private ImageButton mIbGoForward;
    private ImageButton mIbGoBack;
    private ImageButton mIbReload;
    private String mSession;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_detail);
        String noticeTitle = getIntent().getStringExtra("notice_title");
        if (!TextUtils.isEmpty(noticeTitle)) {
//            setCustomTitle(noticeTitle);
        }

        String url = getIntent().getStringExtra("link");

        mWebView = (WebView) findViewById(R.id.wv_notice_detail);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mIbGoForward = (ImageButton) findViewById(R.id.ib_web_goforward);
        mIbGoBack = (ImageButton) findViewById(R.id.ib_web_goback);
        mIbReload = (ImageButton) findViewById(R.id.ib_web_reload);
        mIbGoForward.setOnClickListener(this);
        mIbGoBack.setOnClickListener(this);
        mIbReload.setOnClickListener(this);

        setupWebview();
        loadUrl(buildUrl(url));
    }
    
    public static class ActivityFinishJsObject {
    	WeakReference<CommonWebViewActivity> mRef;
    	
    	public ActivityFinishJsObject(CommonWebViewActivity activity) {
    		mRef = new WeakReference<CommonWebViewActivity>(activity);
    	}
    	
    	@JavascriptInterface
    	public void finishActivity(String message) {
    		if (mRef != null) {
    			CommonWebViewActivity activity = mRef.get();
    			if (activity != null && !activity.isFinishing()) {
    				Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    				activity.finish();		
    			}
    		}
    	}
    	
    	@JavascriptInterface
    	public String getCountryName() {
    		if (mRef != null) {
    			CommonWebViewActivity activity = mRef.get();
    			if (activity != null && !activity.isFinishing()) {
    				return activity.getResources().getConfiguration().locale.getCountry();
    			}
    		}
    		return "";
    	}
    	
    }

	@SuppressWarnings("deprecation")
	private void setupWebview() {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setLightTouchEnabled(false);
        webSettings.setNeedInitialFocus(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        webSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        webSettings.setUseWideViewPort(true);
        webSettings.setAllowFileAccess(true);
        mWebView.setBackgroundColor(Color.WHITE);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        mWebView.setWebChromeClient(new WebBrowserClient());
        mWebView.addJavascriptInterface(new ActivityFinishJsObject(this), "Android");
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                url = buildUrl(url);
                if (url.startsWith("sms") || url.startsWith("tel")) {
                    broadcastIntent(url);
                } else {
                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                setWebViewNavigation();
                super.onPageFinished(view, url);
            }
        });
        mWebView.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                    String mimetype, long contentLength) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(Intent.createChooser(i, ""));
            }
        });
        mWebView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!v.hasFocus()) {
                    v.clearFocus();
                    v.requestFocusFromTouch();
                }
                return false;
            }
        });

        mWebView.requestFocusFromTouch();
    }

    private void broadcastIntent(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        startActivity(Intent.createChooser(intent, ""));
    }

    public void loadUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            finish();
            return;
        }
        mWebView.loadUrl(url);
    }

    private void setWebViewNavigation() {
        if (mWebView.canGoBack()) {
            mIbGoBack.setClickable(true);
            mIbGoBack.setImageResource(R.drawable.bg_btn_can_go_back);
        } else {
            mIbGoBack.setClickable(false);
            mIbGoBack.setImageResource(R.drawable.bg_btn_cannot_go_back);
        }

        if (mWebView.canGoForward()) {
            mIbGoForward.setClickable(true);
            mIbGoForward.setImageResource(R.drawable.bg_btn_can_go_forward);
        } else {
            mIbGoForward.setClickable(false);
            mIbGoForward.setImageResource(R.drawable.bg_btn_cannot_go_forward);
        }
    }

    private String buildUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            if (!TextUtils.isEmpty(host)
                    && (host.endsWith("tshenbian.com") || host.endsWith("m15.cn"))) {
                int index = url.indexOf("?");
                if (index == -1) {
                    url += "?" + PARAM + mSession;
                } else {
                    url += "&" + PARAM + mSession;
                }
            }
            return url;
        }
        return "";
    }

    private final class WebBrowserClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (newProgress == 100) {
                mProgressBar.setVisibility(View.INVISIBLE);
            } else {
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(newProgress);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.ib_web_goback:
            mWebView.goBack();
            break;

        case R.id.ib_web_goforward:
            mWebView.goForward();
            break;

        case R.id.ib_web_reload:
            mWebView.reload();
            break;

        default:
            break;
        }
    }

}
