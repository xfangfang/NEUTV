package com.lalala.fangs.neutv;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lalala.fang.neutvshow.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import cn.xfangfang.flyme6.TabStrip;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static org.litepal.LitePalApplication.getContext;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabStrip tabStrip;
    private ProgressBar progressBar;
    private List<Live> liveList = new ArrayList<>();
    private List<Type> typeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        new getUpdateLive().execute();

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    public static class AllLiveFragment extends Fragment {

        private RecyclerView recyclerView;

        private List<Live> liveList;

        private AdapterLive adapter;
        private LocalBroadcastManager localBroadcastManager;
        private FavoriteReceiver receiver;
        private IntentFilter intentFilter;

        public AllLiveFragment(String id) {
            this.liveList = DataSupport.where("itemid = ?",id).find(Live.class);
        }


        @Override
        public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_all, container, false);
            init(rootView);

            adapter = new AdapterLive(liveList);
            StaggeredGridLayoutManager sm = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(sm);
            recyclerView.setAdapter(adapter);
            adapter.setOnItemClickListener(new AdapterLive.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Intent intent = new Intent(getContext(), Video.class);
                    intent.putExtra("live",liveList.get(position));
                    startActivity(intent);
                }

                @Override
                public void onItemLongClick(View view, int position) {
                    addToFavorite(position);
                }
            });

            return rootView;
        }

        private void init(View rootView){
            recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

            localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
            intentFilter = new IntentFilter();
            intentFilter.addAction("com.lalala.fangs.neutv.LIVE_FAVORITE_CHANGE");
            receiver = new FavoriteReceiver();
            localBroadcastManager.registerReceiver(receiver, intentFilter);
        }

        @Override
        public void onDestroy() {
            localBroadcastManager.unregisterReceiver(receiver);
            Log.e(TAG, "onDestroy: 销毁频道列表" );
            super.onDestroy();
        }

        private static final String TAG = "FindBooksFragment";

        private void addToFavorite(int position) {
            Live t = liveList.get(position);
            ContentValues values = new ContentValues();
            Intent intent = new Intent("com.lalala.fangs.neutv.LIVE_FAVORITE_CHANGE");

            if (t.getIsFavorite()) {
                t.setIsFavorite(false);
                values.put("isFavorite", "0");
            } else {
                t.setIsFavorite(true);
                values.put("isFavorite", "1");
            }
            //更新数据库
            DataSupport.updateAll(Live.class, values, "name = ?", t.getName());
            intent.putExtra("live_name", t.getName());
            intent.putExtra("live_favorite",t.getIsFavorite());
            localBroadcastManager.sendBroadcast(intent);
            adapter.update(position);
        }

        class FavoriteReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                String name = intent.getStringExtra("live_name");
                boolean isfavorite = intent.getBooleanExtra("live_favorite",false);
                if(name != null){
                    Live temp = DataSupport.where("name = ?",name).findFirst(Live.class);
                    if(temp != null){
                        int count = liveList.indexOf(temp);
                        if(count != -1) {
                            liveList.set(count, temp);
                            adapter.update(count);
                        }
                    }
                }
            }
        }


    }

    public static class FavoriteLiveFragment extends Fragment {

        public FavoriteLiveFragment() {
        }

        private RecyclerView recyclerView;
        private List<Live> liveList = new ArrayList<>();
        private AdapterLive adapter;
        private LocalBroadcastManager localBroadcastManager;
        private FavoriteReceiver receiver;
        private IntentFilter intentFilter;


        @Override
        public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_all, container, false);
            recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
            liveList = DataSupport.where("isFavorite = ?", "1").find(Live.class);
            deleteSame();
            adapter = new AdapterLive(liveList);
            StaggeredGridLayoutManager sm = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(sm);
            recyclerView.setAdapter(adapter);
            adapter.setOnItemClickListener(new AdapterLive.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Intent intent = new Intent(getContext(), Video.class);
                    intent.putExtra("live",liveList.get(position));
                    startActivity(intent);
                }

                @Override
                public void onItemLongClick(View view, int position) {
                    addToFavorite(position);
                }
            });
            localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
            intentFilter = new IntentFilter();
            intentFilter.addAction("com.lalala.fangs.neutv.LIVE_FAVORITE_CHANGE");
            receiver = new FavoriteReceiver();
            localBroadcastManager.registerReceiver(receiver, intentFilter);
            return rootView;
        }

        private void addToFavorite(int position) {
            Live t = liveList.get(position);
            ContentValues values = new ContentValues();
            Intent intent = new Intent("com.lalala.fangs.neutv.LIVE_FAVORITE_CHANGE");

            if (t.getIsFavorite()) {
                t.setIsFavorite(false);
                values.put("isFavorite", "0");
            } else {
                //对于收藏夹，这个程序块不会被运行
                t.setIsFavorite(true);
                values.put("isFavorite", "1");
                adapter.update(position);
            }
            //更新数据库
            DataSupport.updateAll(Live.class, values, "name = ?", t.getName());

            //发送给分类
            intent.putExtra("live_name", t.getName());
            intent.putExtra("live_favorite",t.getIsFavorite());
            localBroadcastManager.sendBroadcast(intent);
        }

        class FavoriteReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getBooleanExtra("live_favorite",true)){
                    liveList = DataSupport.where("isFavorite = ?", "1").find(Live.class);
                    deleteSame();
                    adapter.updateAll(liveList);
                }else {
                    String name = intent.getStringExtra("live_name");
                    if(name != null){
                        Live temp = DataSupport.where("name = ?",name).findFirst(Live.class);
                        if(temp != null){
                            int position = liveList.indexOf(temp);
                            adapter.remove(position);
                        }
                    }
                }
            }
        }

        private static final String TAG = "FavoriteLiveFragment";
        private void deleteSame(){
            HashSet<Live> lives = new HashSet<>(liveList);
            liveList = new ArrayList<>(lives);
        }

    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> datas;
        private ArrayList<String> titles;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void setData(ArrayList<Fragment> datas,ArrayList<String> titles) {
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
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

    }

    private class getUpdateLive extends AsyncTask<String, Integer, Boolean> {
        private static final String TAG = "getUpdateLive";

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String updateUrl = "http://hdtv.neu6.edu.cn/hdtv.json";
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(updateUrl).build();
            okhttp3.Response response;
            //每次取节目单前先清除磁盘缓存
            Glide.get(getContext()).clearDiskCache();
            try {
                response = client.newCall(request).execute();
                String res = response.body().string();
                JSONObject dataJson = new JSONObject(res);
                JSONArray data = dataJson.getJSONArray("live");
                JSONArray typeData = dataJson.getJSONArray("type");
                Gson gson = new Gson();
                liveList = gson.fromJson(data.toString(), new TypeToken<ArrayList<Live>>() {
                }.getType());
                typeList = gson.fromJson(typeData.toString(), new TypeToken<ArrayList<Type>>() {
                }.getType());

                //储存type信息
                SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();
                editor.putString("type",typeData.toString());
                editor.apply();

            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return false;
            }

            //读取收藏信息
            for (Live i :
                    liveList) {
                Live temp = DataSupport.where("num = ?", String.valueOf(i.getNum())).findFirst(Live.class);
                if (temp == null) {
                    i.save();
                } else {
                    i.setIsFavorite(temp.getIsFavorite());
                }
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
                ArrayList<Fragment> datas = new ArrayList<>();
                ArrayList<String> titles = new ArrayList<>();
                titles.add("收藏");
                datas.add(new FavoriteLiveFragment());

                for(Type i :
                        typeList){
                    titles.add(i.getName().substring(0,2));
                    datas.add(new AllLiveFragment(i.getId()));
                }

                mSectionsPagerAdapter.setData(datas,titles);

                mViewPager = (ViewPager) findViewById(R.id.container);
                mViewPager.setAdapter(mSectionsPagerAdapter);

                mViewPager.setOffscreenPageLimit(2);
                //将收藏放在-1屏
                mViewPager.setCurrentItem(1);

                tabStrip = (TabStrip) findViewById(R.id.tabstrip);
                tabStrip.setViewPager(mViewPager);
            } else {
                Toast.makeText(getContext(), "出了点问题，需要检查网络，并重启app", Toast.LENGTH_LONG).show();
                Log.e(TAG, "onPostExecute: 出了点问题");
            }
            progressBar.setVisibility(View.INVISIBLE);

        }
    }

}
