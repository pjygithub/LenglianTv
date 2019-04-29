package com.beetech.lenglian_tv;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import java.util.Properties;

public class IndexActivity extends Activity {
    public static final String TAG = IndexActivity.class.getSimpleName();

    public static String URL = "http://cdc.wendu114.com/test.html";
    public static int ALART_TIME_SECOND = 30; //报警时长,单位：秒

    private WebView webView;
    private long exitTime = 0;
    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN , WindowManager.LayoutParams. FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_index);

        try{
            Properties props = new Properties();
            props.load(getApplicationContext().getAssets().open("tv.properties"));

            URL = props.getProperty("URL");
            Log.d(TAG, "URL="+URL);
            ALART_TIME_SECOND = Integer.valueOf(props.getProperty("ALART_TIME_SECOND"));
            Log.d(TAG, "ALART_TIME_SECOND="+ALART_TIME_SECOND);
        } catch (Exception e){
            e.printStackTrace();
        }


        webView = new WebView(this);
        webView.clearHistory();// 清除当前 WebView 访问的历史记录
        webView.clearCache(true);//清空网页访问留下的缓存数据。需要注意的时，由于缓存是全局的，所以只要是WebView用到的缓存都会被清空，即便其他地方也会使用到。该方法接受一个参数，从命名即可看出作用。若设为false，则只清空内存里的资源缓存，而不清空磁盘里的。
        webView.loadUrl("about:blank");// 清空当前加载
        webView.removeAllViews();// 清空子 View
        mMediaPlayer = MediaPlayer.create(IndexActivity.this, R.raw.alert);

        webView.setWebViewClient(new WebViewClient() {
            //设置在webView点击打开的新网页在当前界面显示,而不跳转到新的浏览器中
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {//允许有alert弹出框

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Log.d(TAG,"onJsAlert:"+message);
                if("1".equals(message)){
                    //播放报警
                    if(!mMediaPlayer.isPlaying()){
                        mMediaPlayer.setLooping(true);
                        mMediaPlayer.start();
                        Log.d(TAG,"ALART_TIME_SECOND:"+ALART_TIME_SECOND);
                        Message msgStopPlay = new Message();
                        msgStopPlay.what = 0;
                        mHandler.sendMessageDelayed(msgStopPlay, ALART_TIME_SECOND*1000);
                    }

                } else if ("0".equals(message)){
                    //关闭报警
                    if(mMediaPlayer.isPlaying()){
                        mMediaPlayer.pause();
                        mMediaPlayer.seekTo(0);
                    }

                }
                result.confirm();
                return true;
            }

        });

        WebSettings webSettings = webView.getSettings();
        // 设置与Js交互的权限
        webSettings.setJavaScriptEnabled(true);
        // 设置允许JS弹窗
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        //分表率自适应
        int screenDensity = getResources().getDisplayMetrics(). densityDpi ;
        WebSettings.ZoomDensity zoomDensity  = WebSettings.ZoomDensity. MEDIUM ;

        //主要用于平板，针对特定屏幕代码调整分辨率
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int mDensity = metrics.densityDpi;
        if (mDensity == 240) {
            zoomDensity = WebSettings.ZoomDensity.FAR;
        } else if (mDensity == 160) {
            zoomDensity =  WebSettings.ZoomDensity.MEDIUM;
        } else if (mDensity == 120) {
            zoomDensity = WebSettings.ZoomDensity.CLOSE;
        } else if (mDensity == DisplayMetrics.DENSITY_XHIGH) {
            zoomDensity = WebSettings.ZoomDensity.FAR;
        } else if (mDensity == DisplayMetrics.DENSITY_TV) {
            zoomDensity = WebSettings.ZoomDensity.FAR;
        } else {
            zoomDensity = WebSettings.ZoomDensity.MEDIUM;
        }
        webSettings.setDefaultZoom(zoomDensity) ;

        webSettings.setUseWideViewPort(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);

        webView.loadUrl(URL);
        setContentView(webView);
    }
    private Handler mHandler = new Handler() {
        public void handleMessage(Message m) {
            Log.d(TAG,"m.what:"+m.what);
            switch (m.what){
                case 0:
                    if(mMediaPlayer.isPlaying()){
                        mMediaPlayer.pause();
                        mMediaPlayer.seekTo(0);
                    }

                    break;

                case 1:
                    break;
            }
         }
    };


    //我们需要重写回退按钮的时间,当用户点击回退按钮：
    //1.webView.canGoBack()判断网页是否能后退,可以则goback()
    //2.如果不可以连续点击两次退出App,否则弹出提示Toast
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                super.onBackPressed();
            }

        }
    }
}
