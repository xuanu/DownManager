package utils.cn.zeffect.downmanager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import utils.cn.zeffect.downlibrary.bean.Task;
import utils.cn.zeffect.downlibrary.interfaces.DownListener;
import utils.cn.zeffect.downlibrary.utils.DownUtils;
import utils.cn.zeffect.downlibrary.utils.NetUtils;
import zeffect.cn.common.weak.WeakAsyncTask;

public class MainActivity extends Activity {
    private TextView mShowView;
    private Button mStartBtn;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mShowView = (TextView) findViewById(R.id.show);
        mStartBtn = (Button) findViewById(R.id.start_down);
        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAll();
            }
        });
        registerReceiver(mReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }


    private void startAll() {
        if (!NetUtils.isConnected(mContext)) return;
        if (NetUtils.isMobile(mContext)) return;
        String savePath = Environment.getExternalStorageDirectory() + File.separator + "wyt" + File.separator;
        String serverIp = "http://192.168.1.148:8888/wyt/";
        new WeakAsyncTask<String, String, Void, Context>(mContext) {
            @Override
            protected Void doInBackground(Context pContext, @NotNull String... pVoids) {
                String savePath = pVoids[0];
                String serverIp = pVoids[1];
//                if (!serverIp.endsWith("//")) serverIp += "/";
                long startTime = System.currentTimeMillis();
                onProgressUpdate(pContext, "开始解析文件");
                String jsonData = getAssetFileString(pContext, "yzbfile.txt");
                onProgressUpdate(pContext, "解析文件用时:" + (System.currentTimeMillis() - startTime) * 1f / 1000 + "秒");
                try {
                    JSONArray dataArray = new JSONArray(jsonData);
                    //本地文件存在路径，/sdcard/wyt/+path
                    onProgressUpdate(pContext, "共需下载：" + dataArray.length() + "个文件");
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject dataJson = dataArray.getJSONObject(i);
                        String name = dataJson.getString("name");
                        String md5 = dataJson.getString("md5");
                        String path = dataJson.getString("path");
                        File tempFile = new File(savePath, path);
                        if (tempFile.isDirectory()) continue;
                        if (!tempFile.exists())
                            DownUtils.addTask(pContext, new Task().setDownUrl(serverIp + path).setSaveDir(tempFile.getParent()).setSaveName(name), mListener);
                        else {
                            String localMd5 = FileCompare.getFileMD5(tempFile);
                            if (localMd5.equals(md5)) continue;
                            tempFile.delete();
                            DownUtils.addTask(pContext, new Task().setDownUrl(serverIp + path).setSaveDir(tempFile.getParent()).setSaveName(name), mListener);
                        }
                    }
                } catch (JSONException pE) {
                    pE.printStackTrace();
                } finally {
                    return null;
                }
            }

            @Override
            protected void onProgressUpdate(String... values) {
                addShowText(values[0]);
            }
        }.execute(savePath, serverIp);

    }


    private void addShowText(String text) {
        if (TextUtils.isEmpty(text)) return;
        mShowView.setText(text);
    }


    private DownListener mListener = new DownListener() {
        @Override
        public void onStart(Task pTask) {
            addShowText(pTask.getDownUrl() + ",开始下载");
        }

        @Override
        public void onPause(Task pTask) {
            addShowText(pTask.getDownUrl() + ",下载暂停");
        }

        @Override
        public void onSuccess(Task pTask) {
            addShowText(pTask.getDownUrl() + ",下载完成");
        }

        @Override
        public void onFaile(Task pTask) {
            addShowText(pTask.getDownUrl() + ",下载失败");
        }

        @Override
        public void onDowning(Task pTask) {
            addShowText("下载进度：" + (pTask.getDownLength() * 1L / pTask.getTotalLength()) + "下载地址：" + pTask.getDownUrl() + ",下载中");
        }
    };


    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                startAll();
            }
        }
    };

    /**
     * 获取Asset目录的文件内容
     *
     * @param pName asset文件名如：xx.txt、xx/xxx/xxx.txt;
     * @return 返回数据
     */
    public static String getAssetFileString(Context pContext, String pName) {
        if (pContext == null) {
            return "";
        }
        if (TextUtils.isEmpty(pName)) {
            return "";
        }
        String retuString = "";
        InputStream is = null;
        try {
            is = pContext.getAssets().open(pName);
            byte[] buffer = new byte[10240];
            int len;
            StringBuilder sb = new StringBuilder();
            while ((len = is.read(buffer)) > 0) {
                sb.append(new String(buffer, 0, len));
            }
            retuString = sb.toString();
            is.close();
        } catch (IOException pE) {
            pE.printStackTrace();
        }
        return retuString;
    }

}
