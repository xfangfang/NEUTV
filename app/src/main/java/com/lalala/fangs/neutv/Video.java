package com.lalala.fangs.neutv;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.lalala.fang.neutvshow.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;

import static com.lalala.fang.neutvshow.R.id.videoView;

public class Video extends Activity {
    boolean isTvNameOn = false;//判断节目单是否出现
    boolean isOnPlay = false;//判断是否在播放
    boolean touchFlag = true;//判断控制器是否显示

    String[][] d = new String[10][0];//节目名字
    String[][] h = new String[10][0];//节目链接
    String[] dates = new String[10];//节目日期
    String liveTvName = " ";//直播节目名字
    String tvNameNow = " ";//当前节目名字
    int dayNow = 0;//当前播放时间
    int[] r;//每天节目数量

    String name; //传递过来的节目名字
    String liveUrl;
    VideoView video;
    TextView textAll, textTvName, textChange, textNow;
    ListView lv2;
    SeekBar sb;
    ProgressBar pb;
    RelativeLayout lineTop;
    LinearLayout  lineBottom;
    Button buttonPause, buttonClose;

    private Handler handler = new Handler();//每个一秒取一次当前播放时间
    private Runnable run = new Runnable() {
        public void run() {
//            Blurry.delete((ViewGroup) findViewById(R.id.video_content));
//            Blurry.with(Video.this)
//                    .radius(25)
//                    .sampling(2)
//                    .async()
//                    .animate(500)
//                    .onto((ViewGroup) findViewById(R.id.video_content));
            long a = video.getCurrentPosition();
            textNow.setText(intToString((int) a / 1000));
            sb.setProgress((int) a / 1000);
            handler.postDelayed(run, 1000);
        }
    };

    private Handler handler2 = new Handler();//每隔一秒时间加一
    int tim = 0;
    private Runnable autoGone = new Runnable() {
        public void run() {
            if (!isOnPlay || isTvNameOn) {
                tim = 0;
            } else tim++;
            if (tim == 4) {
                iCantSee();
                tim = 0;
            }
            handler.postDelayed(autoGone, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_video);
        video = (VideoView) findViewById(videoView);
        textAll = (TextView) findViewById(R.id.textAll);
        textNow = (TextView) findViewById(R.id.textNow);
        textTvName = (TextView) findViewById(R.id.textTvName);
        lineBottom = (LinearLayout) findViewById(R.id.controlerBottom);
        textChange = (TextView) findViewById(R.id.textChange);
        lineTop = (RelativeLayout) findViewById(R.id.controlerTop);
        lv2 = (ListView) findViewById(R.id.listView2);
        sb = (SeekBar) findViewById(R.id.seekBar);
        pb = (ProgressBar) findViewById(R.id.progressBar);
        buttonPause = (Button) findViewById(R.id.buttonPause);
        buttonClose = (Button) findViewById(R.id.buttonClose);


        Bundle bundle = this.getIntent().getExtras();
        liveUrl = bundle.getString("Name");
        String[] n = liveUrl.split("/");
        int q = n.length;
        int len = n[q - 1].length();
        String ans = n[q - 1].substring(0, len - 5);
        name = ans;

        playTv(liveUrl);
        iniList();

        textTvName.setText(liveTvName);
        try {
            getHistoryList();
        } catch (IOException e) {
            e.printStackTrace();
        }

        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                textTvName.setText(tvNameNow);
                pb.setVisibility(View.GONE);
                long a = video.getDuration();
                sb.setMax((int) a / 1000);
                textAll.setText(intToString((int) a / 1000));
                isOnPlay = true;

            }
        });

        video.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(getApplicationContext(), "不能播放此视频", Toast.LENGTH_LONG).show();
                finish();
                return false;
            }
        });


        video.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!touchFlag) {
                    printScreen(v);
                    tim = 0;
                    lineBottom.setVisibility(View.VISIBLE);
                    lineTop.setVisibility(View.VISIBLE);
                    touchFlag = true;

                } else {
                    lineBottom.setVisibility(View.GONE);
                    lineTop.setVisibility(View.GONE);
                    touchFlag = false;
                }
                return false;
            }
        });


        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int a = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pb.setVisibility(View.GONE);
                if (fromUser) {
                    textNow.setText(intToString(progress));
                    textChange.setVisibility(View.VISIBLE);
                    if (progress > a)
                        textChange.setText("+" + intToString(progress - a));
                    else
                        textChange.setText("-" + intToString(a - progress));
                } else {
                    isOnPlay = true;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                a = seekBar.getProgress();
                textChange.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textChange.setVisibility(View.GONE);
                pb.setVisibility(View.VISIBLE);
                int a = seekBar.getProgress();
                video.seekTo(a * 1000);
                video.start();
                isOnPlay = false;
            }
        });

        handler.post(run);
        handler2.post(autoGone);


    }


    @Override
    protected void onResume() {
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onResume();
    }

    //横竖屏切换时不重载
    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }

    Document doc;

    public void getHistoryList() throws IOException {
//        JSONArray json2 = (JSONArray) JSONSerializer.toJSON("[1,2,3]");
//        List java2 = (List) JSONSerializer.toJava(json2);
//        System.out.println("——用JSONSerializer将json转换list——" + java2);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    doc = Jsoup.connect("http://hdtv.neu6.edu.cn/hdtv.json").get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void listHref(String name) throws IOException {
        r = new int[10];
        int day = 0;
        String date = "20";

        Document doc = Jsoup.connect("http://hdtv.neu6.edu.cn/time-select?p=" + name).get();
        Elements noons = doc.getElementsByTag("div");

        for (Element link : noons) {
            if (link.attr("id").contains("list_item")) {
                if (link.parent().parent().attr("id").toString().contains(date)) {
                    date = link.parent().parent().attr("id").toString();
                    dates[day] = date;
                } else if (link.text().contains("1") || link.text().contains("0")) {
                    date = link.parent().parent().attr("id").toString();
                    day++;
                    dates[day] = date;
                }
                if (!link.text().equals(" ")) {
                    h[day] = Arrays.copyOf(h[day], h[day].length + 1);
                    d[day] = Arrays.copyOf(d[day], d[day].length + 1);
                    h[day][r[day]] = "";
                    d[day][r[day]] = link.text();
                    r[day]++;
                }
            } else if (link.attr("id").contains("list_status")) {
                h[day][r[day] - 1] = link.getElementsByTag("a").attr("href").toString();
            }
        }
        for (int j = 0; j < 8; j++) {
            System.out.println("\n" + dates[j]);
            for (int i = 0; i < r[j]; i++) {
                if (h[j][i].contains("newplayer")) {
                    liveTvName = dates[0] + " " + d[j][i];
                    tvNameNow = liveTvName;
                }
                System.out.println(d[j][i] + "\n" + h[j][i]);
            }
        }
    }

    private static final String TAG = "Video";

    public void pause(View view) {
        if (video.isPlaying()) {
            buttonPause.setText("播放");
            video.pause();
            isOnPlay = false;
        } else {
            buttonPause.setText("暂停");
            video.start();
            isOnPlay = true;
        }
    }

    public void playTv(String url) {
        Uri uri = Uri.parse(url);
        Log.e(TAG, "playTv: " + url);
        video.setVideoURI(uri);
        video.start();
//        handler.post(run);
//        handler.removeCallbacks(run);

    }

    public void closeWin(View view) {
        finish();
    }

    public void showTv(View view) {
        if (isTvNameOn) {
            LinearLayout l = (LinearLayout) findViewById(R.id.LineTvShow);
            l.setVisibility(View.GONE);
            isTvNameOn = false;
            textTvName.setText(tvNameNow);
        } else {
            LinearLayout l = (LinearLayout) findViewById(R.id.LineTvShow);
            l.setVisibility(View.VISIBLE);
            isTvNameOn = true;
            textTvName.setText(dates[dayNow]);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_POWER) {
            video.pause();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (!isTvNameOn) {
                finish();
            } else {
                LinearLayout l = (LinearLayout) findViewById(R.id.LineTvShow);
                l.setVisibility(View.GONE);
                isTvNameOn = false;
                textTvName.setText(tvNameNow);
            }
            return true;
        }
        return false;
    }

    public void s(final int i) {
        dayNow = i;
        final String tvName = textTvName.getText().toString();
        lv2.setAdapter(new ArrayAdapter<>(Video.this, android.R.layout.simple_list_item_1, d[i]));
        lv2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                LinearLayout l = (LinearLayout) findViewById(R.id.LineTvShow);
                l.setVisibility(View.GONE);
                isTvNameOn = false;
                if (h[i][arg2].contains("newplayer")) {
                    if (textTvName.getText().toString().equals(liveTvName.split(" ")[1]))
                        Toast.makeText(getApplicationContext(), "正在直播...", Toast.LENGTH_SHORT).show();
                    else {
                        playTv("http://media2.neu6.edu.cn/hls/" + name + ".m3u8");
                        textTvName.setText(liveTvName);
                    }
                } else if (!h[i][arg2].equals("")) {
                    playTv("http://media2.neu6.edu.cn/review/program-" + h[i][arg2].substring(23, 45) + name + ".m3u8");
                    pb.setVisibility(View.VISIBLE);
                    isOnPlay = false;
                    tvNameNow = dates[dayNow] + " " + d[i][arg2];
                    textTvName.setText(tvNameNow);
                    dayNow = i;
                } else {
                    textTvName.setText(tvName);
                    Toast.makeText(getApplicationContext(), "没找到资源呀...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void s1(View view) {
        s(0);
        textTvName.setText(dates[0]);
    }

    public void s2(View view) {
        s(1);
        textTvName.setText(dates[1]);
    }

    public void s3(View view) {
        s(2);
        textTvName.setText(dates[2]);

    }

    public void s4(View view) {
        s(3);
        textTvName.setText(dates[3]);
    }

    public void s5(View view) {
        s(4);
        textTvName.setText(dates[4]);
    }

    public void s6(View view) {
        s(5);
        textTvName.setText(dates[5]);
    }

    public void s7(View view) {
        s(6);
        textTvName.setText(dates[6]);
    }

    public void s8(View view) {
        s(7);
        textTvName.setText(dates[7]);
    }

    public void iniList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    listHref(name);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        s(0);
                    }
                });
            }
        }).start();
    }

    public String intToString(int t) {
        String a = "00:00:00";
        int sec = t % 60;
        t /= 60;
        int min = t % 60;
        int hour = t / 60;
        if (hour == 0)
            a = new DecimalFormat("00").format(min) + ":" + new DecimalFormat("00").format(sec);
        else
            a = new DecimalFormat("00").format(hour) + ":" + new DecimalFormat("00").format(min) + ":" + new DecimalFormat("00").format(sec);
        return a;
    }

    public void iCantSee() {
        touchFlag = false;
        lineBottom.setVisibility(View.GONE);
        lineTop.setVisibility(View.GONE);
    }

    public void printScreen(View view) {
        String imgPath = "/sdcard/test.png";
//        view.setDrawingCacheEnabled(true);
//        view.buildDrawingCache();
//        Bitmap bitmap = view.getDrawingCache();

//        try {
//            MediaMetadataRetriever rev = new MediaMetadataRetriever();
//            Uri uri = Uri.parse(liveUrl);
//            rev.setDataSource(Video.this, uri); //这里第一个参数需要Context，传this指针
//            Bitmap bitmap = rev.getFrameAtTime(((VideoView) view).getCurrentPosition() * 1000,
//                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
//            if (bitmap != null) {
//                Log.e(TAG, "printScreen: get" );
//                try {
//                    FileOutputStream out = new FileOutputStream(imgPath);
//                    bitmap.compress(Bitmap.CompressFormat.PNG, 100,
//                            out);
//                } catch (Exception e) {
//                    Log.e(TAG, "printScreen: something happend i don't know" ,e);
//                }
//            }
//        }catch (Exception e){
//            Log.e(TAG, "printScreen: lalala ",e );
//        }

    }

}
