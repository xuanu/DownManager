package utils.cn.zeffect.downlibrary.interfaces;

import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <pre>
 *      author  ：zzx
 *      e-mail  ：zhengzhixuan18@gmail.com
 *      time    ：2017/08/02
 *      desc    ：
 *      version:：1.0
 * </pre>
 *
 * @author zzx
 */

public class ListenerUtils {
    public static ListenerUtils instance;
    private ConcurrentHashMap<String, WeakReference<DownListener>> mHashMap = new ConcurrentHashMap<>();

    public static ListenerUtils getInstance() {
        if (instance == null) {
            synchronized (ListenerUtils.class) {
                if (instance == null) {
                    instance = new ListenerUtils();
                }
            }
        }
        return instance;
    }


    public void addListener(String url, DownListener pListener) {
        if (pListener == null) return;
        if (TextUtils.isEmpty(url)) return;
        if (mHashMap.containsKey(url)) return;
        mHashMap.put(url, new WeakReference<DownListener>(pListener));
    }

    public void removeListener(String url) {
        if (TextUtils.isEmpty(url)) return;
        if (!mHashMap.containsKey(url)) return;
        mHashMap.remove(url);
    }

    public WeakReference<DownListener> getListener(String url) {
        if (TextUtils.isEmpty(url)) return null;
        if (!mHashMap.containsKey(url)) return null;
        return mHashMap.get(url);
    }

}
