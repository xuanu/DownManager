package utils.cn.zeffect.downlibrary.orm;

import android.content.Context;

import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.DataBaseConfig;

/**
 * 青檬数据库,没有extends BaseApplication的不能使用
 * <pre>
 *      author  ：zzx
 *      e-mail  ：zhengzhixuan18@gmail.com
 *      time    ：2017/03/29
 *      desc    ：
 *      version:：1.0
 * </pre>
 *
 * @author zzx
 */

public class OrmUtils {
    /**
     * 数据库操作对象
     */
    private static LiteOrm liteOrm;

    /***
     * 取得当前数据库操作对象
     * @return liteorm
     */
    public static LiteOrm getLiteOrm() {
        return liteOrm;
    }

    /***
     * 默认初始化
     *
     * @param pContext 上下文
     */
    public static void defaultInit(Context pContext) {
        defaultInit(pContext, pContext.getPackageName());
    }

    /***
     * 默认初始化
     *
     * @param pContext 上下文
     * @param dbName db名(可以是路径)
     */
    public static void defaultInit(Context pContext, String dbName) {
        if (pContext != null) {
            DataBaseConfig defaultConfig = new DataBaseConfig(pContext, dbName);
            liteOrm = LiteOrm.newSingleInstance(defaultConfig);
        }
    }
}
