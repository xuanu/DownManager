package utils.cn.zeffect.downlibrary.runnable;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.litesuits.orm.db.assit.WhereBuilder;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import utils.cn.zeffect.downlibrary.bean.Block;
import utils.cn.zeffect.downlibrary.bean.Task;
import utils.cn.zeffect.downlibrary.imp.DownImp;
import utils.cn.zeffect.downlibrary.interfaces.DownListener;
import utils.cn.zeffect.downlibrary.interfaces.ListenerUtils;
import utils.cn.zeffect.downlibrary.utils.Constant;
import utils.cn.zeffect.downlibrary.utils.NetUtils;
import utils.cn.zeffect.downlibrary.orm.OrmUtils;
import utils.cn.zeffect.downlibrary.utils.WeakHandler;

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

public class DownRunnable implements Runnable, Handler.Callback {
    /**
     * 默认线程
     */
    public static final int THREAD_NUM = 10;
    /**
     * 最小的分块
     */
    public static final int MIN_BLOCK_SIZE = 15 * 1024 * 1024;
    /***
     * 下载失败重试两次
     */
    public static final int TRY_AGAIN_TIME = 2;
    private Task mTask;
    private DownImp mDownImp;
    /**
     * 线程池
     */
    private ExecutorService mExecutorService;
    private CountDownLatch mDownLatch;
    private WeakReference<DownListener> mDownListener;
    private WeakHandler mWeakHandler;

    public DownRunnable(DownImp pImp, Task pDown) {
        this.mDownImp = pImp;
        this.mTask = pDown;
        mExecutorService = Executors.newFixedThreadPool(THREAD_NUM);
        mDownListener = ListenerUtils.getInstance().getListener(mTask.getDownUrl());
        mWeakHandler = new WeakHandler(pImp.getService().getMainLooper(), this);
    }

    @Override
    public void run() {
        try {
            boolean isConnect = NetUtils.isConnected(mDownImp.getService());
            if (!isConnect) {
                mTask.setStatus(Task.STATU_FAILE);
                return;
            }
            if (NetUtils.isMobile(mDownImp.getService()) && mTask.getStatus() == Task.IS_WIFI_DOWN) {//必须要WIFI下载
                mTask.setStatus(Task.STATU_FAILE);
                return;
            }
            long totalLenght = getFileLengthFormNet(mTask.getDownUrl());
            if (totalLenght < 0) {
                mTask.setStatus(Task.STATU_FAILE);
                return;
            }
            if (mTask.getTotalLength() != totalLenght) {
                mTask.setTotalLength(totalLenght);
                mTask.setBlocks(disposeBlocks(mTask, totalLenght));
            } else mTask.setBlocks(Task.toBlocks(mTask.getBlocksString()));
            ArrayList<Block> blocks = mTask.getBlocks();
            if (blocks == null || blocks.size() < 1) {
                blocks = disposeBlocks(mTask, totalLenght);
                mTask.setBlocks(blocks);
            }
            int status = mTask.getStatus();
            if (status == Task.STATU_PAUSE) return;
            else if (status == Task.STATU_SUCCESS) return;
            mDownLatch = new CountDownLatch(blocks.size());
            mTask.setStatus(Task.STATU_ING);
            sendMessage(Task.STATU_NORMAL, mTask);//发个普通过去代表开始任务
            for (int i = 0; i < blocks.size(); i++) {
                mExecutorService.execute(new DownChildRunnable(this, mDownLatch, mTask, i));
            }
            mDownLatch.await();
        } catch (Exception pE) {
        } finally {
            //根据每个块的下载状态，查看是否全部下载完成
            ArrayList<Block> tempBlocks = mTask.getBlocks();
            boolean isSucess = true;
            for (Block tempBlock : tempBlocks) {
                if (tempBlock.getStatus() != Block.STATU_SUCCESS) {
                    isSucess = false;
                    break;
                }
            }
            if (isSucess) {
                mTask.setStatus(Task.STATU_SUCCESS);
                sendMessage(Task.STATU_SUCCESS, mTask);
            } else {
                if (mTask.getStatus() == Task.STATU_PAUSE) {
                    sendMessage(Task.STATU_PAUSE, mTask);
                } else if (mTask.getStatus() == Task.STATU_FAILE) {
                    sendMessage(Task.STATU_FAILE, mTask);
                }
            }
            //不是暂停状态都保存一下当前的进度。暂停是设置状态的时候就保存了。
            if (mTask.getStatus() != Task.STATU_PAUSE) {
                saveTask(mTask);
            }
            mDownImp.removeTask(mTask.getDownUrl());
        }
    }

    public void setStaus(int statu) {
        if (statu != Task.STATU_PAUSE) return;
        mTask.setStatus(statu);
        saveTask(mTask);
        sendMessage(Task.STATU_PAUSE, mTask);
    }

    private void saveTask(Task pTask) {
        if (pTask == null) return;
        String url = pTask.getDownUrl();
        if (TextUtils.isEmpty(url)) return;
        if (DownImp.isDbExist(url))
            OrmUtils.getLiteOrm().delete(new WhereBuilder(Task.class).andEquals(Constant.URL_KEY, url));
        pTask.setBlocksString(Task.blocksToString(pTask.getBlocks()));
        long status = OrmUtils.getLiteOrm().save(pTask);
    }


    public boolean isDowning() {
        return mTask.getStatus() == Task.STATU_ING;
    }

    /***
     * 存储重试次数
     */
    private HashMap<Integer, Integer> mTryTimes = new HashMap<>();

    /***
     * 重新下载某一块
     *
     * 重试2次吧
     * @param pItem 块
     */

    public synchronized void tryAgainDown(Block pItem) {
        int tryTime = mTryTimes.containsKey(pItem.getIndex()) ? mTryTimes.get(pItem.getIndex()) : 0;
        if (tryTime < TRY_AGAIN_TIME) {
            tryTime++;
            mTryTimes.put(pItem.getIndex(), tryTime);
            mExecutorService.execute(new DownChildRunnable(this, mDownLatch, mTask, pItem.getIndex()));
        } else {
            mDownLatch.countDown();
        }

    }

    public static final int UPDATA_TIME = 2000;
    private long mStartTime = 0;

    /***
     * 增加下载长度
     * @param downLength 下载点
     */
    public void addDownLength(long downLength) {
        if (mTask.getStatus() == Task.STATU_PAUSE) return;
        long nowDownLenght = mTask.getDownLength();
        mTask.setDownLength(nowDownLenght + downLength);
        //两秒回调一次
        long nowTime = System.currentTimeMillis();
        if (nowTime - mStartTime > UPDATA_TIME) {
            mStartTime = nowTime;
            sendMessage(Task.STATU_ING, mTask);
        }
    }


    /***
     * 返回文件真实长度，从网络
     * @param downUrl 下载地址
     * @return 真实长度。<0代表出错
     */
    private long getFileLengthFormNet(String downUrl) {
        long len = -1;
        try {
            URL url = new URL(downUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10 * 1000);
            connection.setReadTimeout(10 * 1000);
            len = connection.getContentLength();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                len = -1;
            }
        } catch (Exception e1) {
        } finally {
            return len;
        }
    }

    /***
     * 只有第一次会调用这个分块操作，下次就不会了，所以下载进度=0OK的。
     * @param pRecords 记录
     * @return 分好的块
     */
    private ArrayList<Block> disposeBlocks(Task pRecords, long pTotal) {
        ArrayList<Block> blocks = new ArrayList<>();
        if (pRecords != null) {
            int blockNum = pRecords.getTotalLength() < MIN_BLOCK_SIZE ? 1 : THREAD_NUM;
            int blockLength = (int) Math.ceil(pRecords.getTotalLength() / blockNum);
            for (int i = 0; i < blockNum; i++) {
                Block tempItem = new Block();
                tempItem.setBlockLength(i != blockNum - 1 ? blockLength : (pTotal - i * blockLength));
                tempItem.setTotal(pRecords.getTotalLength());
                tempItem.setDownlength(0);
                tempItem.setIndex(i);
                tempItem.setStart(i * blockLength);
                blocks.add(tempItem);
            }
        }
        return blocks;
    }


    private void sendMessage(int what, Task pTask) {
        Message msg = new Message();
        msg.what = what;
        msg.obj = pTask;
        mWeakHandler.sendMessage(msg);
    }

    /***
     * 用这个来做回调的操作，不然在Runnable里操作好像会报错。
     * @param msg
     * @return
     */
    @Override
    public boolean handleMessage(Message msg) {
        int what = msg.what;
        if (msg.obj == null) return true;
        Task tempTask = (Task) msg.obj;
        if (tempTask == null) return true;
        if (mDownListener == null) return true;
        if (mDownListener.get() == null) return true;
        switch (what) {
            case Task.STATU_NORMAL:
                mDownListener.get().onStart(mTask);
                break;
            case Task.STATU_FAILE:
                mDownListener.get().onFaile(mTask);
                break;
            case Task.STATU_ING:
                mDownListener.get().onDowning(mTask);
                break;
            case Task.STATU_PAUSE:
                mDownListener.get().onPause(mTask);
                break;
            case Task.STATU_SUCCESS:
                mDownListener.get().onSuccess(mTask);
                break;
        }
        return true;
    }
}
