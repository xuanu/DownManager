package utils.cn.zeffect.downlibrary;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

import utils.cn.zeffect.downlibrary.bean.Task;
import utils.cn.zeffect.downlibrary.imp.DownImp;
import utils.cn.zeffect.downlibrary.utils.Constant;

/**
 * 下载管理器
 * <pre>
 *      author  ：zzx
 *      e-mail  ：zhengzhixuan18@gmail.com
 *      time    ：2017/08/01
 *      desc    ：
 *      version:：1.0
 * </pre>
 *
 * @author zzx
 */

public class DownService extends Service {
    private DownImp mImp;

    @Override
    public void onCreate() {
        super.onCreate();
        mImp = new DownImp(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action.equals(Constant.START_ACTION)) {
                Task newTask = (Task) intent.getSerializableExtra(Constant.CONTENT_KEY);
                if (newTask != null) {
                    mImp.addTask(newTask);
                }
            } else if (action.equals(Constant.PAUSE_ACTION)) {
                String url = intent.getStringExtra(Constant.CONTENT_KEY);
                if (!TextUtils.isEmpty(url)) mImp.pauseTask(url);
            } else if (action.equals(Constant.DELETE_ACTION)) {
                String url = intent.getStringExtra(Constant.CONTENT_KEY);
                if (!TextUtils.isEmpty(url)) mImp.deleteTask(url);
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
