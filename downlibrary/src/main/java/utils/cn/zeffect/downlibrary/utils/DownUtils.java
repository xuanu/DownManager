package utils.cn.zeffect.downlibrary.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;

import com.litesuits.orm.db.assit.QueryBuilder;

import java.io.File;
import java.net.IDN;
import java.util.List;
import java.util.UUID;

import utils.cn.zeffect.downlibrary.DownService;
import utils.cn.zeffect.downlibrary.bean.Task;
import utils.cn.zeffect.downlibrary.interfaces.DownListener;
import utils.cn.zeffect.downlibrary.interfaces.ListenerUtils;
import utils.cn.zeffect.downlibrary.orm.OrmUtils;

/**
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

public class DownUtils {
    public static void addTask(Context pContext, Task pTask) {
        addTask(pContext, pTask, null);
    }

    public static void addTask(Context pContext, Task pTask, DownListener pListener) {
        if (pContext == null) throw new NullPointerException("context null");
        if (pTask == null) return;
        String url = pTask.getDownUrl();
        if (TextUtils.isEmpty(url)) return;
        String dir = pTask.getSaveDir();
        if (TextUtils.isEmpty(dir))
            dir = Environment.getExternalStorageDirectory() + File.separator + Constant.DIR_NAME;
        File dirFile = new File(dir);
        if (!dirFile.isDirectory())
            dir = Environment.getExternalStorageDirectory() + File.separator + Constant.DIR_NAME;
        File dirFile2 = new File(dir);
        if (!dirFile2.exists()) dirFile2.mkdirs();
        pTask.setSaveDir(dir);
        String name = pTask.getSaveName();
        if (TextUtils.isEmpty(name)) {
            String fix = url.substring(url.lastIndexOf("."));
            name = UUID.randomUUID().toString() + fix;
        }
        pTask.setSaveName(name);
        if (pListener != null) ListenerUtils.getInstance().addListener(url, pListener);
        Intent dataIntent = new Intent(pContext, DownService.class);
        dataIntent.putExtra(Constant.CONTENT_KEY, pTask);
        dataIntent.setAction(Constant.START_ACTION);
        pContext.startService(dataIntent);
    }

    public static void pauseTask(Context pContext, Task pTask) {
        if (pContext == null) throw new NullPointerException("context null");
        if (pTask == null) return;
        String url = pTask.getDownUrl();
        if (TextUtils.isEmpty(url)) return;
        pContext.startService(new Intent(pContext, DownService.class).setAction(Constant.PAUSE_ACTION).putExtra(Constant.CONTENT_KEY, url));
    }

    public static void pauseTask(Context pContext, String url) {
        if (pContext == null) throw new NullPointerException("context null");
        if (TextUtils.isEmpty(url)) return;
        pContext.startService(new Intent(pContext, DownService.class).setAction(Constant.PAUSE_ACTION).putExtra(Constant.CONTENT_KEY, url));
    }


    public static void deleteTask(Context pContext, String url) {
        if (pContext == null) throw new NullPointerException("context null");
        if (TextUtils.isEmpty(url)) return;
        pContext.startService(new Intent(pContext, DownService.class).setAction(Constant.DELETE_ACTION).putExtra(Constant.CONTENT_KEY, url));
    }

    public static Task queryTask(int id) {
        List<Task> tempTasks = OrmUtils.getLiteOrm().query(new QueryBuilder<Task>(Task.class).whereEquals(Constant.ID_KEY, id));
        if (tempTasks != null && !tempTasks.isEmpty()) {
            return tempTasks.get(0);
        } else {
            return null;
        }
    }

    public static Task queryTask(String url) {
        if (TextUtils.isEmpty(url)) return null;
        List<Task> tempTasks = OrmUtils.getLiteOrm().query(new QueryBuilder<Task>(Task.class).whereEquals(Constant.URL_KEY, url));
        if (tempTasks != null && !tempTasks.isEmpty()) {
            return tempTasks.get(0);
        } else {
            return null;
        }
    }

}
