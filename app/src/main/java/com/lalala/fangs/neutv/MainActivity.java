package com.lalala.fangs.neutv;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lalala.fang.neutvshow.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.File;
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
    private TextView textWrong;
    private Button btnWrong;
    private List<Live> liveList = new ArrayList<>();
    private List<Type> typeList = new ArrayList<>();
    private int loginTime;
    private BroadcastReceiver broadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        textWrong = (TextView) findViewById(R.id.text_wrong);
        btnWrong = (Button) findViewById(R.id.btn_wrong);
        new getUpdateInfor().execute();
        new getUpdateLive().execute();

        SharedPreferences sp = getSharedPreferences("APPCONFIG", Context.MODE_PRIVATE);
        loginTime = sp.getInt("loginTime", 0);
        if (loginTime < 3) {
            AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);  //(普通消息框)
            ab.setPositiveButton("神奇的功能 点击加群!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    joinQQGroup("OVbiu9aw_bqHtOgXM_fb17lOW0LpzKeA");
                }
            });
            ab.setTitle("一共只提醒你三次哦");
            ab.setMessage("\n校园内使用，断开校园网账号，不然流量哗哗的～\n\n" +
                    "欢迎加群\n532607431\n向作者吐槽");
            AlertDialog dialog = ab.create();
            dialog.show();

        }
        SharedPreferences.Editor spEdit = getSharedPreferences("APPCONFIG", Context.MODE_PRIVATE).edit();
        loginTime++;
        spEdit.putInt("loginTime", loginTime);
        spEdit.apply();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @SuppressLint("ValidFragment")
    public static class AllLiveFragment extends Fragment {

        public AllLiveFragment() {
        }

        private RecyclerView recyclerView;

        private List<Live> liveList;

        private AdapterLive adapter;
        private LocalBroadcastManager localBroadcastManager;
        private FavoriteReceiver receiver;
        private IntentFilter intentFilter;

        public AllLiveFragment(String id) {
            this.liveList = DataSupport.where("itemid = ?", id).find(Live.class);
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
                    intent.putExtra("live", liveList.get(position));
                    startActivity(intent);
                }

                @Override
                public void onItemLongClick(View view, int position) {
                    addToFavorite(position);
                }
            });

            return rootView;
        }

        private void init(View rootView) {
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
            Log.e(TAG, "onDestroy: 销毁频道列表");
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
            intent.putExtra("live_favorite", t.getIsFavorite());
            localBroadcastManager.sendBroadcast(intent);
            adapter.update(position);
        }

        class FavoriteReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                String name = intent.getStringExtra("live_name");
                boolean isfavorite = intent.getBooleanExtra("live_favorite", false);
                if (name != null) {
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

    @SuppressLint("ValidFragment")
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
                    intent.putExtra("live", liveList.get(position));
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
            intent.putExtra("live_favorite", t.getIsFavorite());
            localBroadcastManager.sendBroadcast(intent);
        }

        class FavoriteReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getBooleanExtra("live_favorite", true)) {
                    liveList = DataSupport.where("isFavorite = ?", "1").find(Live.class);
                    deleteSame();
                    adapter.updateAll(liveList);
                } else {
                    String name = intent.getStringExtra("live_name");
                    if (name != null) {
                        Live temp = DataSupport.where("name = ?", name).findFirst(Live.class);
                        if (temp != null) {
                            int position = liveList.indexOf(temp);
                            adapter.remove(position);
                        }
                    }
                }
            }
        }

        private static final String TAG = "FavoriteLiveFragment";

        private void deleteSame() {
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
            Request request = new Request
                    .Builder()
                    .url(updateUrl)
                    .addHeader("User-Agent", "neutv" + getVersion())
                    .build();
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
                SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                editor.putString("type", typeData.toString());
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

                for (Type i :
                        typeList) {
                    titles.add(i.getName().substring(0, 2));
                    datas.add(new AllLiveFragment(i.getId()));
                }

                mSectionsPagerAdapter.setData(datas, titles);

                mViewPager = (ViewPager) findViewById(R.id.container);
                mViewPager.setAdapter(mSectionsPagerAdapter);

//                mViewPager.setOffscreenPageLimit(2);
                //将收藏放在-1屏
                mViewPager.setCurrentItem(1);

                tabStrip = (TabStrip) findViewById(R.id.tabstrip);
                tabStrip.setViewPager(mViewPager);
                textWrong.setVisibility(View.INVISIBLE);
                btnWrong.setVisibility(View.INVISIBLE);
            } else {
                textWrong.setVisibility(View.VISIBLE);
                btnWrong.setVisibility(View.VISIBLE);
                btnWrong.setClickable(true);
            }
            progressBar.setVisibility(View.INVISIBLE);

        }
    }

    private class getUpdateInfor extends AsyncTask<String, Integer, Boolean> {

        String res;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String updateUrl = "http://hdtv.neu6.edu.cn/soft/neutv.ver";
            OkHttpClient client = new OkHttpClient();
            Request request = new Request
                    .Builder()
                    .url(updateUrl)
                    .addHeader("User-Agent", "neutv" + getVersion())
                    .build();
            okhttp3.Response response;
            //每次取节目单前先清除磁盘缓存
            Glide.get(getContext()).clearDiskCache();
            try {
                response = client.newCall(request).execute();
                res = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                final String[] items = res.split("\n");
                try {
                    double version = Double.valueOf(items[0]);
                    final String downLoadLink = items[1];
                    double currentVersion = Double.valueOf(getVersion());
                    String content = "";
                    for (int i = 2; i < items.length; i++) {
                        content += items[i] + "\n";
                    }
                    if (version > currentVersion) {
                        AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);  //(普通消息框)
                        ab.setPositiveButton("更新", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downLoadLink));
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                request.setTitle("直视新版本");
                                request.setDescription(items[0]);
                                request.addRequestHeader("User-Agent", "neutv" + getVersion());
                                File saveFile = new File(Environment.getExternalStorageDirectory(), "neutv" + String.valueOf(items[0]) + ".apk");
                                request.setDestinationUri(Uri.fromFile(saveFile));

                                final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                final long downloadId = manager.enqueue(request);
                                IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
                                broadcastReceiver = new BroadcastReceiver() {
                                    @Override
                                    public void onReceive(Context context, Intent intent) {
                                        long ID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                                        if (ID == downloadId) {
                                            DownloadManager.Query query = new DownloadManager.Query();
                                            query.setFilterById(downloadId);
                                            query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
                                            Cursor cursor = manager.query(query);
                                            if (!cursor.moveToFirst()) {
                                                cursor.close();
                                                Snackbar.make(progressBar,"下载失败", Snackbar.LENGTH_LONG).show();
                                                return;
                                            }
                                            cursor.close();

                                            Snackbar.make(progressBar,"安装包下载好了", Snackbar.LENGTH_LONG).
                                                    setDuration(1000000).
                                                    setAction("安装", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent installIntent = new Intent(Intent.ACTION_VIEW);
                                                    File saveFile = new File(Environment.getExternalStorageDirectory(), "neutv" + String.valueOf(items[0]) + ".apk");
                                                    installIntent.setDataAndType(Uri.fromFile(saveFile),
                                                            "application/vnd.android.package-archive");
                                                    installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(installIntent);                                                }
                                            }).show();

                                        }
                                    }
                                };
                                registerReceiver(broadcastReceiver, intentFilter);
                            }
                        });
                        ab.setTitle("新版本 " + String.valueOf(items[0]) + " 来了");
                        ab.setMessage(content);
                        AlertDialog dialog = ab.create();
                        dialog.show();
                    }
                } catch (Exception e) {
                    AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
                    ab.setTitle("消息");
                    ab.setMessage(res);
                    AlertDialog dialog = ab.create();
                    dialog.show();
                }
            } else {
                Log.e(TAG, "onPostExecute: 访问更新信息失败");
            }

        }
    }


    public void reCall(View v) {
        v.setClickable(false);
        new getUpdateLive().execute();
    }

    private static final String TAG = "MainActivity";

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
                Log.e(TAG, "onKeyDown: ok");
                break;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                Log.e(TAG, "onKeyDown: down");
                mViewPager.requestFocus();
                break;

            case KeyEvent.KEYCODE_DPAD_LEFT:
                Log.e(TAG, "onKeyDown: left");
                break;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                Log.e(TAG, "onKeyDown: right");
                break;

            case KeyEvent.KEYCODE_DPAD_UP:
                Log.e(TAG, "onKeyDown: up");
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 获取版本号
     *
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


    /****************
     *
     * 发起添加群流程。群号：直视 官方BUG反馈(532607431) 的 key 为： OVbiu9aw_bqHtOgXM_fb17lOW0LpzKeA
     * 调用 joinQQGroup(OVbiu9aw_bqHtOgXM_fb17lOW0LpzKeA) 即可发起手Q客户端申请加群 直视 官方BUG反馈(532607431)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回fals表示呼起失败
     ******************/
    private boolean joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}
