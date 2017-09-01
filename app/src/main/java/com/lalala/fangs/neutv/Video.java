package com.lalala.fangs.neutv;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lalala.fang.neutvshow.R;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.text.Collator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import cn.xfangfang.videocontroller.VideoController;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import q.rorbin.verticaltablayout.VerticalTabLayout;
import q.rorbin.verticaltablayout.adapter.TabAdapter;
import q.rorbin.verticaltablayout.widget.ITabView;
import q.rorbin.verticaltablayout.widget.TabView;

import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.lalala.fang.neutvshow.R.id.videoView;
import static org.litepal.LitePalApplication.getContext;

public class Video extends AppCompatActivity {

    HashMap<String, ArrayList<ArrayList<String>>> beforeList;
    ArrayList<String> tvDateList;
    ArrayList<Type> typeList;
    ArrayList<String> urlList;
    String liveUrl;
    int urlIndex;

    String name; //传递过来的节目名字
    Live live;

    VideoView video;
    VideoController videoController;
    LinearLayout linearLayout_before, linearLayout_show;
    ViewPager viewPager_before, viewPager_show;
    VerticalTabLayout tabLayout_before, tabLayout_show;
    PagerAdapter pagerAdapter;
    private TextView textFillWidth;
    private TextView textFillHeight;



    private LinearLayout layoutSources;
    private LinearLayout videoContent;
    private LinearLayout layoutSetting;

    LocalBroadcastManager localBroadcastManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_video);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }


        video = (VideoView) findViewById(videoView);
        videoController = (VideoController) findViewById(R.id.video_controller);
        linearLayout_before = (LinearLayout) findViewById(R.id.list_layout_before);
        linearLayout_show = (LinearLayout) findViewById(R.id.list_layout_show);
        videoContent = (LinearLayout) findViewById(R.id.video_content);
        layoutSetting = (LinearLayout) findViewById(R.id.layout_setting);
        textFillWidth = (TextView) findViewById(R.id.text_fillWidth);
        textFillHeight = (TextView) findViewById(R.id.text_fillHeight);


        viewPager_before = (ViewPager) findViewById(R.id.view_pager_before_list);
        viewPager_show = (ViewPager) findViewById(R.id.view_pager_show);
        tabLayout_before = (VerticalTabLayout) findViewById(R.id.tablayout_before_list);
        tabLayout_show = (VerticalTabLayout) findViewById(R.id.tablayout_show);
        layoutSources = (LinearLayout) findViewById(R.id.layout_sources);



        video.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        videoController.setSystemUiVisibility(SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getSupportActionBar().hide();

        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());



        live = (Live) getIntent().getSerializableExtra("live");
        genUrls();

        Gson gson = new Gson();

        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        typeList = gson.fromJson(pref.getString("type", ""), new TypeToken<ArrayList<Type>>() {
        }.getType());


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
                showSettingLayout();
            }

            @Override
            public void onList() {
                linearLayout_show.setVisibility(View.VISIBLE);
            }

            @Override
            public void onBeforeList() {
                linearLayout_before.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFavorite() {
                ContentValues values = new ContentValues();
                Intent intent = new Intent("com.lalala.fangs.neutv.LIVE_FAVORITE_CHANGE");

                if (live.getIsFavorite()) {
                    live.setIsFavorite(false);
                    values.put("isFavorite", "0");
                    Toast.makeText(getApplicationContext(), "不喜欢了", Toast.LENGTH_LONG).show();
                } else {
                    live.setIsFavorite(true);
                    values.put("isFavorite", "1");
                    Toast.makeText(getApplicationContext(), "喜欢", Toast.LENGTH_LONG).show();
                }
                videoController.setFavorite(live.getIsFavorite());
                //更新数据库
                DataSupport.updateAll(Live.class, values, "name = ?", live.getName());
                intent.putExtra("live_name", live.getName());
                intent.putExtra("live_favorite",live.getIsFavorite());
                localBroadcastManager.sendBroadcast(intent);
            }
        });
        videoController.setOnStateListener(new VideoController.OnStateListener() {
            @Override
            public void onError() {
                urlIndex++;
                if(urlIndex < urlList.size()) {
                    Toast.makeText(getApplicationContext(), "当前源不可用 正在自动切换", Toast.LENGTH_LONG).show();
                    playTv(urlList.get(urlIndex));
                }else{
                    Toast.makeText(getApplicationContext(), "资源出了点问题", Toast.LENGTH_LONG).show();
                    finish();
                }
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

            @Override
            public void onVisible() {
                videoController.setSystemUiVisibility(SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                if (linearLayout_before.isShown()) {
                    linearLayout_before.setVisibility(View.INVISIBLE);
                }
                if (linearLayout_show.isShown()) {
                    linearLayout_show.setVisibility(View.INVISIBLE);
                }
                if(layoutSetting.isShown()){
                    hideSettingLayout();
                }
            }

            @Override
            public void onInvisible() {
                videoController.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            }
        });

        if(urlIndex == -1){
            Toast.makeText(getApplicationContext(),"资源出了点问题",Toast.LENGTH_SHORT).show();
            finish();
        }
        liveUrl = urlList.get(urlIndex);

        name = getName(liveUrl);

        playTv(liveUrl);

        new getLiveBefore().execute(name);


        ArrayList<Fragment> datas = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();
        titles.add("收藏");

        TvShowFragment t1 = new TvShowFragment();
        TvShowFragment.GoToAnotherTVListener gotoAnotherTVListener = new TvShowFragment.GoToAnotherTVListener() {
            @Override
            public void onGoAnother(Live one) {
                live = one;
                genUrls();
                if(urlIndex == -1) finish();
                liveUrl = urlList.get(urlIndex);
                videoController.setFavorite(live.getIsFavorite());
                name = getName(liveUrl);

                videoController.contentVisible();
                playTv(one.getUrllist());
                new getLiveBefore().execute(name);
                videoController.setTitle(one.getName());
                linearLayout_show.setVisibility(View.INVISIBLE);
                videoController.setIsLiveOrNot(true);
            }
        };
        t1.setOnGoToAnotherTVListener(gotoAnotherTVListener);
        datas.add(t1);
        for (Type i :
                typeList) {
            titles.add(i.getName().substring(0, 2));
            TvShowFragment t = new TvShowFragment(i.getId());
            t.setOnGoToAnotherTVListener(gotoAnotherTVListener);
            datas.add(t);
        }
        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        pagerAdapter.setData(datas, titles);
        viewPager_show.setAdapter(pagerAdapter);
        viewPager_show.setOffscreenPageLimit(10);
        tabLayout_show.setupWithViewPager(viewPager_show);
        viewPager_show.setPageTransformer(true, new ZoomOutPageTransformer());

        videoController.setActivity(this);
    }


    private List<String> getDiffRes(String url) {
        String[] n = url.split("#");
        return Arrays.asList(n);
    }

    private String getName(String url) {
        String short_url = url.substring(26, url.length()-5);
        String[] temp = short_url.split("/");
        return temp[temp.length - 1];
    }

    private void full(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
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
        if (linearLayout_before.isShown()) {
            linearLayout_before.setVisibility(View.INVISIBLE);
            return;
        } else if (linearLayout_show.isShown()) {
            linearLayout_show.setVisibility(View.INVISIBLE);
            return;
        }else if(layoutSetting.isShown()){
            hideSettingLayout();
            return;
        }
        else if (videoController.isShown()) {
//            videoController.contentInvisible();
//            return;
        }
        super.onBackPressed();
    }


    private boolean videoViewState;
    @Override
    protected void onPause() {
        videoViewState = video.isPlaying();
        videoController.pause();
        super.onPause();
    }

    @Override
    protected void onPostResume() {
        videoController.contentVisible();
        if(videoViewState){
            videoController.start();
        }else{
            videoController.pause();
        }
        super.onPostResume();
    }

    //横竖屏切换时不重载
    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }

    private static final String TAG = "Video";

    public void playTv(String url) {
        updateResPos();
        Uri uri = Uri.parse(url);
        videoController.contentVisible();
        videoController.ProgressBarVisible();
        video.setVideoURI(uri);
        video.start();
    }

    private class getLiveBefore extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(String... params) {
            String updateUrl = "http://hdtv.neu6.edu.cn/" + params[0] + ".review";
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(updateUrl)
                    .addHeader("user-agent","neutv"+getVersion())
                    .build();
            okhttp3.Response response;
            try {
                response = client.newCall(request).execute();
                String res = response.body().string();
                if (res.equals("")) return false;

                Gson gson = new Gson();
                beforeList = gson.fromJson(
                        res,
                        new TypeToken<HashMap<String, ArrayList<ArrayList<String>>>>() {
                        }.getType()
                );
                tvDateList = new ArrayList<>(beforeList.keySet());
                Collections.sort(tvDateList, Collator.getInstance(Locale.ENGLISH));
                Collections.reverse(tvDateList);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                pagerAdapter = new PagerAdapter(getSupportFragmentManager(), 5);
                ArrayList<Fragment> datas = new ArrayList<>();
                for (String i : tvDateList) {
                    datas.add(new TvListFragment(beforeList.get(i)));
                }
                pagerAdapter.setData(datas, tvDateList);
                viewPager_before.setAdapter(pagerAdapter);
                viewPager_before.setOffscreenPageLimit(2);
                tabLayout_before.setupWithViewPager(viewPager_before);
                viewPager_before.setPageTransformer(true, new ZoomOutPageTransformer());
                pagerAdapter.notifyDataSetChanged();
            } else {
                //没有取得历史节目单
            }
        }
    }

    @SuppressLint("ValidFragment")
    public static class TvListFragment extends Fragment {

        public TvListFragment(ArrayList<ArrayList<String>> beforeList) {
            this.beforeList = beforeList;
        }

        private RecyclerView recyclerView;
        private AdapterList adapter;
        private ArrayList<ArrayList<String>> beforeList;
        private Video parentActivity;

        @Override
        public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_before_list, container, false);
            recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_list);
            adapter = new AdapterList(beforeList);
            StaggeredGridLayoutManager sm = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(sm);
            recyclerView.setAdapter(adapter);
            parentActivity = (Video) getActivity();
            adapter.setOnItemClickListener(new AdapterList.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    List<String> one = beforeList.get(position);
                    String url = "http://media2.neu6.edu.cn/review/program-"
                            + one.get(0) + "-"
                            + one.get(1) + "-" + parentActivity.name + ".m3u8";

                    Long timeStart = Long.valueOf(one.get(0))*1000L;
                    Long timeNow = System.currentTimeMillis();
                    Log.e(TAG, "onItemClick: "+timeNow+"---"+timeStart );
                    if(timeNow > timeStart+60000) {
                        parentActivity.playTv(url);
                        parentActivity.videoController.setTitle(one.get(2));
                        parentActivity.videoController.contentVisible();
                        parentActivity.linearLayout_before.setVisibility(View.INVISIBLE);
                        parentActivity.videoController.setIsLiveOrNot(false);
                    }else{
                        Toast.makeText(getContext(), "节目还未开始", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onItemLongClick(View view, int position) {

                }
            });
            return rootView;
        }


    }

    @SuppressLint("ValidFragment")
    public static class TvShowFragment extends Fragment {

        public TvShowFragment(String id) {
            this.liveList = DataSupport.where("itemid = ?", id).find(Live.class);
            isFavoritePage = false;
        }

        public TvShowFragment() {
            liveList = DataSupport.where("isFavorite = ?", "1").find(Live.class);
            deleteSame();
            isFavoritePage = true;
        }

        private RecyclerView recyclerView;
        private AdapterShow adapter;
        private List<Live> liveList;
        private boolean isFavoritePage;

        public interface GoToAnotherTVListener {
            void onGoAnother(Live one);
        }

        private GoToAnotherTVListener listener;
        private LocalBroadcastManager localBroadcastManager;
        private FavoriteReceiver receiver;
        private IntentFilter intentFilter;


        public void setOnGoToAnotherTVListener(GoToAnotherTVListener listener) {
            this.listener = listener;
        }

        @Override
        public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_before_list, container, false);
            recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_list);

            localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
            intentFilter = new IntentFilter();
            intentFilter.addAction("com.lalala.fangs.neutv.LIVE_FAVORITE_CHANGE");
            receiver = new FavoriteReceiver();
            localBroadcastManager.registerReceiver(receiver, intentFilter);

            adapter = new AdapterShow(liveList);
            StaggeredGridLayoutManager sm = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(sm);
            recyclerView.setAdapter(adapter);
            adapter.setOnItemClickListener(new AdapterShow.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Live one = liveList.get(position);
                    if (listener != null) {
                        listener.onGoAnother(one);
                    }
                }

                @Override
                public void onItemLongClick(View view, int position) {

                }
            });
            return rootView;
        }

        @Override
        public void onDestroy() {
//            Log.e(TAG, "onDestroy: "+liveList.size()+"-"+liveList.get(0).getItemid()+"-"+isFavoritePage );
            localBroadcastManager.unregisterReceiver(receiver);
            super.onDestroy();
        }

        class FavoriteReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                String name = intent.getStringExtra("live_name");
                boolean isfavorite = intent.getBooleanExtra("live_favorite",false);
                if(name != null){
                    if(isFavoritePage){
                        liveList = DataSupport.where("isFavorite = ?","1").find(Live.class);
                        deleteSame();
                        adapter.updateAll(liveList);
                    }else {
                        Live temp = DataSupport.where("name = ?", name).findFirst(Live.class);
                        if (temp != null) {
                            int count = liveList.indexOf(temp);
                            if (count != -1) {
                                liveList.set(count, temp);
                                adapter.update(count);
                            }
                        }
                    }
                }
            }
        }

        private void deleteSame(){
            HashSet<Live> lives = new HashSet<>(liveList);
            liveList = new ArrayList<>(lives);
        }

    }

    private class PagerAdapter extends FragmentStatePagerAdapter implements TabAdapter {
        private ArrayList<Fragment> datas;
        private ArrayList<String> titles;
        int titleBegin = 0;

        public PagerAdapter(FragmentManager fm, int titleBegin) {
            super(fm);
            this.titleBegin = titleBegin;
        }

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }


        public void setData(ArrayList<Fragment> datas, ArrayList<String> titles) {
            this.datas = datas;
            this.titles = titles;
        }

        @Override
        public Fragment getItem(int position) {
            return datas == null ? null : datas.get(position);
        }

        @Override
        public int getCount() {
            return datas == null ? 0 : datas.size();
        }

        @Override
        public ITabView.TabBadge getBadge(int position) {
            return null;
        }

        @Override
        public ITabView.TabIcon getIcon(int position) {
            return null;
        }

        @Override
        public ITabView.TabTitle getTitle(int position) {
            if(getWeek(titles.get(position)) == null){
                return new TabView.TabTitle.Builder()
                        .setContent(
                                titles.get(position).substring(titleBegin, titles.get(position).length()))
                        .setTextColor(0xFF36BC9B, 0xFFFFFFFF)
                        .build();
            }
            return new TabView.TabTitle.Builder()
                    .setContent(
                            titles.get(position).substring(titleBegin, titles.get(position).length())+
                            "\n"+
                            getWeek(titles.get(position)))
                    .setTextColor(0xFF36BC9B, 0xFFFFFFFF)
                    .build();
        }

        @Override
        public int getBackground(int position) {
            return 0;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position).substring(titleBegin, titles.get(position).length());
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    private class ZoomOutPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.85f;

        @SuppressLint("NewApi")
        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

//            Log.e("TAG", view + " , " + position + "");

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 1) //a页滑动至b页 ； a页从 0.0 -1 ；b页从1 ~ 0.0
            { // [-1,1]
                // Modify the default slide transition to shrink the page as well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE)
                        / (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }


    HashMap<String,String> diffRes;
    ArrayList<Button> buttons;
    private void genUrls(){
        urlList = new ArrayList<>();
        diffRes = new HashMap<>();
        layoutSources.removeAllViews();
        buttons = new ArrayList<>();
        if(live.getUrllist() != null){
            urlIndex = 0;
            urlList = new ArrayList<>(Arrays.asList(live.getUrllist().split("#")));
            for(String i:urlList){
                String name = getName(i);
                diffRes.put(name,i);
                Button btn = new Button(getApplicationContext());
                btn.setText(name);
                btn.setWidth(MATCH_PARENT);
                btn.setHeight(dp2px(48));
                btn.setTextSize(18);
                btn.setTextColor(Color.parseColor("#FFFFFF"));
                btn.setBackgroundColor(Color.parseColor("#00ffffff"));
                buttons.add(btn);
                layoutSources.addView(btn);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        urlIndex = buttons.indexOf(v);
                        playTv(diffRes.get(((Button)v).getText()));
                        hideSettingLayout();
                    }
                });
            }
        }else{
            urlIndex = -1;
        }
    }

    public void fillWidth(View view){
        LinearLayout.LayoutParams layoutParams=
                new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        video.setLayoutParams(layoutParams);
        view.setBackgroundColor(Color.parseColor("#80000000"));
        textFillHeight.setBackgroundColor(Color.parseColor("#00000000"));
        hideSettingLayout();
    }

    public void fillHeight(View view){
        LinearLayout.LayoutParams layoutParams=
                new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        video.setLayoutParams(layoutParams);
        view.setBackgroundColor(Color.parseColor("#80000000"));
        textFillWidth.setBackgroundColor(Color.parseColor("#00000000"));
        hideSettingLayout();
    }

    private void updateResPos(){
        for (Button i:buttons){
            i.setBackgroundColor(Color.parseColor("#00ffffff"));
        }
        if(urlIndex < buttons.size()){
            buttons.get(urlIndex).setBackgroundColor(Color.parseColor("#80000000"));
        }
    }

    private int px2dp(float px){
        float scale = getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    private int dp2px(float dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private void hideSettingLayout(){
//        Animator.AnimatorListener listener = null;
//        listener = new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
                layoutSetting.setVisibility(View.INVISIBLE);
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        };
//        AnimatorSet set = new AnimatorSet();
//        ObjectAnimator anim1 = ObjectAnimator.ofFloat(layoutSetting, "translationX",  0f,dp2px( 300));
//
//
//        anim1.setInterpolator(new AccelerateInterpolator());
//
//        anim1.setDuration(200);
//        set.play(anim1);
//        set.start();
//        set.addListener(listener);
    }

    private void showSettingLayout(){
        layoutSetting.setVisibility(View.VISIBLE);

//        Animator.AnimatorListener listener = null;
//        listener = new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//                layoutSetting.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        };
//        AnimatorSet set = new AnimatorSet();
//        ObjectAnimator anim1 = ObjectAnimator.ofFloat(layoutSetting, "translationX", 0f, 0f);
//
//
//        anim1.setInterpolator(new AccelerateInterpolator());
//
//        anim1.setDuration(200);
//        set.play(anim1);
//        set.start();
//        set.addListener(listener);
    }

    private String getWeek(String pTime) {
        String Week = "";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(format.parse(pTime));
        } catch (ParseException e) {
            return null;
        }
        switch (c.get(Calendar.DAY_OF_WEEK)){
            case Calendar.SUNDAY:
                Week = "周日";
                break;
            case Calendar.MONDAY:
                Week = "周一";
                break;
            case Calendar.TUESDAY:
                Week = "周二";
                break;
            case Calendar.WEDNESDAY:
                Week = "周三";
                break;
            case Calendar.THURSDAY:
                Week = "周四";
                break;
            case Calendar.FRIDAY:
                Week = "周五";
                break;
            case Calendar.SATURDAY:
                Week = "周六";
                break;
        }
        return Week;
    }

    /**
     * 获取版本号
     * @return 当前应用的版本号
     */
    public String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "null";
        }
    }
}
