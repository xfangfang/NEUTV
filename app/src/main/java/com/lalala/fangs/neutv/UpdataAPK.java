package com.lalala.fangs.neutv;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 获取更新信息的函数
 * Created by fang on 2018/3/23.
 */

public class UpdataAPK {

    public interface UpdateListener{
        void info(double version,String description,String downloadLink);
        void error(String errorInfo);
    }
    public static void onUpdate(UpdateListener listener){
        new getUpdateInfor(listener);
    }

    private static class getUpdateInfor extends AsyncTask<String, Integer, Boolean> {

        private static final String TAG = "getUpdateInfor";
        String res;
        UpdateListener listener;
        getUpdateInfor(UpdateListener listener){
            this.listener = listener;
        }

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
                    .addHeader("User-Agent", "neutv" + utils.APP_VERSION)
                    .build();
            okhttp3.Response response;
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
                    StringBuilder content = new StringBuilder();
                    for (int i = 2; i < items.length; i++) {
                        content.append(items[i]).append("\n");
                    }
                    listener.info(version, content.toString(),downLoadLink);
                } catch (Exception e) {
                    e.printStackTrace();
                    listener.error(res);
                }
            } else {
                Log.e(TAG, "onPostExecute: 访问更新信息失败");
            }

        }
    }

}
