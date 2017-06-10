package com.lalala.fangs.neutv;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import java.util.List;

import cn.xfangfang.flyme6.TabStrip;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabStrip tabStrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ArrayList<Fragment> datas = new ArrayList<>();
        datas.add(new AllLiveFragment());
        datas.add(new FavoriteLiveFragment());
//        datas.add(new FindBooksFragment());

        mSectionsPagerAdapter.setData(datas);

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOffscreenPageLimit(2);

        tabStrip = (TabStrip) findViewById(R.id.tabstrip);
        tabStrip.setViewPager(mViewPager);


    }



    public static class AllLiveFragment extends Fragment {

        public AllLiveFragment() {
        }

        private RecyclerView recyclerView;
        private ProgressBar progressBar;
        private ArrayList<Live> liveList=new ArrayList<>();
        private AdapterLive adapter;

        @Override
        public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_all, container, false);
            recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
            progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
            new getUpdateLive().execute();
            return rootView;
        }

        class getUpdateLive extends AsyncTask<String, Integer, Boolean> {
            private static final String TAG = "getUpdateLive";
            int num;
            @Override
            protected void onPreExecute() {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected Boolean doInBackground(String... params) {
                num = 0;
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
                    Gson gson = new Gson();
                    liveList = gson.fromJson(data.toString(), new TypeToken<ArrayList<Live>>(){}.getType());
                }catch (IOException | JSONException e) {
                    e.printStackTrace();
                    return false;
                }
                for(Live i:
                        liveList){
                    Live temp = DataSupport.where("num = ?",String.valueOf(i.getNum())).findFirst(Live.class);
                    if(temp == null){
                        i.save();
                    }else{
                        i.setIsFavorite(temp.getIsFavorite());
                        if(temp.getIsFavorite()){
                            Log.e(TAG, "doInBackground: "+temp.getName() );
                        }
                    }
                }

                return true;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                if(aBoolean) {
                    progressBar.setVisibility(View.INVISIBLE);
                    adapter = new AdapterLive(liveList);
                    StaggeredGridLayoutManager sm = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
                    recyclerView.setLayoutManager(sm);
                    recyclerView.setAdapter(adapter);
                    adapter.setOnItemClickListener(new AdapterLive.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            Intent intent = new Intent(getContext(), Video.class);
                            intent.putExtra("Name",liveList.get(position).getUrllist());
                            startActivity(intent);
                        }

                        @Override
                        public void onItemLongClick(View view, int position) {
                            addToFavorite(position);
                        }
                    });
                }else{
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getContext(),"出了点问题，需要检查网络，并重启app",Toast.LENGTH_LONG).show();
                    Log.e(TAG, "onPostExecute: 出了点问题" );
                }
            }
        }

        private static final String TAG = "FindBooksFragment";
        private void addToFavorite(int position){
            Live t = liveList.get(position);
            ContentValues values = new ContentValues();
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
            Intent bookShelfintent = new Intent("com.lalala.fangs.neutv.LIVE_FAVORITE_CHANGE");
            if(t.getIsFavorite()){
                t.setIsFavorite(false);
                values.put("isFavorite", "0");
                Toast.makeText(getContext(),"从收藏中已删除～",Toast.LENGTH_LONG).show();
            }else {
                t.setIsFavorite(true);
                values.put("isFavorite", "1");
                Toast.makeText(getContext(),"加入到我的收藏～",Toast.LENGTH_LONG).show();
            }
            //更新数据库
            DataSupport.updateAll(Live.class, values, "num = ?", String.valueOf(t.getNum()));
            localBroadcastManager.sendBroadcast(bookShelfintent);
            adapter.update(position);
        }


    }

    public static class FavoriteLiveFragment extends Fragment {

        public FavoriteLiveFragment() {
        }

        private RecyclerView recyclerView;
        private ProgressBar progressBar;
        private List<Live> liveList=new ArrayList<>();
        private AdapterLive adapter;
        private LocalBroadcastManager loaclBroadcastManager;
        private FavoriteReceiver receiver;
        private IntentFilter intentFilter;


        @Override
        public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_all, container, false);
            recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
            progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.GONE);
            liveList = DataSupport.where("isFavorite = ?","1").find(Live.class);
            adapter = new AdapterLive(liveList);
            StaggeredGridLayoutManager sm = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(sm);
            recyclerView.setAdapter(adapter);
            adapter.setOnItemClickListener(new AdapterLive.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Intent intent = new Intent(getContext(), Video.class);
                    intent.putExtra("Name",liveList.get(position).getUrllist());
                    startActivity(intent);
                }

                @Override
                public void onItemLongClick(View view, int position) {
                    addToFavorite(position);
                }
            });
            loaclBroadcastManager = LocalBroadcastManager.getInstance(getContext());
            intentFilter = new IntentFilter();
            intentFilter.addAction("com.lalala.fangs.neutv.LIVE_FAVORITE_CHANGE");
            receiver = new FavoriteReceiver();
            loaclBroadcastManager.registerReceiver(receiver, intentFilter);
            return rootView;
        }


        private static final String TAG = "FindBooksFragment";
        private void addToFavorite(int position){
            Live t = liveList.get(position);
            ContentValues values = new ContentValues();
            if(t.getIsFavorite()){
                t.setIsFavorite(false);
                values.put("isFavorite", "0");
                Toast.makeText(getContext(),"从收藏中已删除～",Toast.LENGTH_LONG).show();
                adapter.remove(position);
            }else {
                t.setIsFavorite(true);
                values.put("isFavorite", "1");
                Toast.makeText(getContext(),"加入到我的收藏～",Toast.LENGTH_LONG).show();
                adapter.update(position);
            }
            //更新数据库
            DataSupport.updateAll(Live.class, values, "num = ?", String.valueOf(t.getNum()));
        }

        class FavoriteReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e(TAG, "onReceive: 收到信息" );
                liveList = DataSupport.where("isFavorite = ?","1").find(Live.class);
                for(Live i:
                        liveList){
                    Log.e(TAG, "onReceive: "+i.getName() );
                }
                adapter.updateAll(liveList);
            }
        }

    }



    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> datas;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void setData(ArrayList<Fragment> datas) {
            this.datas = datas;
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
            String[] titles = new String[] { "全部", "收藏", "收藏"};
            return titles[position];
        }

    }

}
