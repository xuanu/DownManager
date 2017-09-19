package utils.cn.zeffect.downlibrary.interfaces;

import java.io.Serializable;

import utils.cn.zeffect.downlibrary.bean.Task;

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

public interface DownListener {
    void onStart(Task pTask);

    void onPause(Task pTask);

    void onSuccess(Task pTask);

    void onFaile(Task pTask);

    void onDowning(Task pTask);
}
