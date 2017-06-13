package com.lalala.fangs.neutv;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.VideoView;

import com.lalala.fang.neutvshow.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Arrays;

import cn.xfangfang.videocontroller.VideoController;

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
    Live live;

    VideoView video;
    ListView lv2;
    VideoController videoController;
    LinearLayout linearLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_video);

        video = (VideoView) findViewById(videoView);
        lv2 = (ListView) findViewById(R.id.listView2);
        videoController = (VideoController) findViewById(R.id.video_controller);
        linearLayout = (LinearLayout) findViewById(R.id.line_tv_show);



        live = (Live) getIntent().getSerializableExtra("live");
        if(live != null){
            liveUrl = live.getUrllist();
        }

        videoController.setTitle(live.getName());
        videoController.setVideoView(video);
        videoController.setFavorite(live.getIsFavorite());
        videoController.setIsLiveOrNot(true);
        videoController.setOnClickEventListener(new VideoController.OnClickEventListener() {
            @Override
            public void onBack() {
                finish();
            }

            @Override
            public void onMenu() {
                Toast.makeText(getApplicationContext(), "换源", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onList() {
                Toast.makeText(getApplicationContext(), "频道", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onBeforeList() {
                linearLayout.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), "回看", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFavorite() {
                if(live.getIsFavorite()){
                    videoController.setFavorite(true);
                }else{
                    videoController.setFavorite(false);
                }
            }
        });
        videoController.setOnStateListener(new VideoController.OnStateListener() {
            @Override
            public void onError() {
                Toast.makeText(getApplicationContext(), "不能播放此视频", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onPause() {

            }

            @Override
            public void onStart() {

            }

            @Override
            public void onPrepared() {

            }
        });

        String[] n = liveUrl.split("/");
        int q = n.length;
        int len = n[q - 1].length();
        String ans = n[q - 1].substring(0, len - 5);
        name = ans;

        playTv(liveUrl);
        iniList();

        try {
            getHistoryList();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onResume() {
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if(linearLayout.isShown()){
            linearLayout.setVisibility(View.INVISIBLE);
            return;
        }else if(videoController.isShown()){
            videoController.contentInvisible();
            return;
        }
        super.onBackPressed();
    }

    //横竖屏切换时不重载
    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }

    Document doc;

    public void getHistoryList() throws IOException {

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


    public void playTv(String url) {
        Uri uri = Uri.parse(url);
        video.setVideoURI(uri);
        video.start();
    }



    public void s(final int i) {
        dayNow = i;
        lv2.setAdapter(new ArrayAdapter<>(Video.this, android.R.layout.simple_list_item_1, d[i]));
        lv2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                LinearLayout l = (LinearLayout) findViewById(R.id.line_tv_show);
                l.setVisibility(View.GONE);
                isTvNameOn = false;
                if (h[i][arg2].contains("newplayer")) {
                    if (live.getName().equals(liveTvName.split(" ")[1]))
                        Toast.makeText(getApplicationContext(), "正在直播...", Toast.LENGTH_SHORT).show();
                    else {
                        playTv("http://media2.neu6.edu.cn/hls/" + name + ".m3u8");
                        videoController.setTitle(liveTvName);
                    }
                } else if (!h[i][arg2].equals("")) {
                    playTv("http://media2.neu6.edu.cn/review/program-" + h[i][arg2].substring(23, 45) + name + ".m3u8");
                    isOnPlay = false;
                    tvNameNow = dates[dayNow] + " " + d[i][arg2];
                    videoController.setTitle(tvNameNow);
                    dayNow = i;
                } else {
                    videoController.setTitle(live.getName());
                    Toast.makeText(getApplicationContext(), "没找到资源呀...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void s1(View view) {
        s(0);
        videoController.setTitle(dates[0]);
    }

    public void s2(View view) {
        s(1);
        videoController.setTitle(dates[1]);
    }

    public void s3(View view) {
        s(2);
        videoController.setTitle(dates[2]);

    }

    public void s4(View view) {
        s(3);
        videoController.setTitle(dates[3]);
    }

    public void s5(View view) {
        s(4);
        videoController.setTitle(dates[4]);
    }

    public void s6(View view) {
        s(5);
        videoController.setTitle(dates[5]);
    }

    public void s7(View view) {
        s(6);
        videoController.setTitle(dates[6]);
    }

    public void s8(View view) {
        s(7);
        videoController.setTitle(dates[7]);
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


}
