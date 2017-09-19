package utils.cn.zeffect.downlibrary.runnable;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import utils.cn.zeffect.downlibrary.bean.Block;
import utils.cn.zeffect.downlibrary.bean.Task;

/**
 * <pre>
 *      author  ：zzx
 *      e-mail  ：zhengzhixuan18@gmail.com
 *      time    ：2017/07/27
 *      desc    ：
 *      version:：1.0
 * </pre>
 *
 * @author zzx
 */

public class DownChildRunnable implements Runnable {
    private CountDownLatch mDownLatch;
    private DownRunnable mRunnable;
    private Task mItem;
    private int mIndex;

    public DownChildRunnable(DownRunnable pRunnable, CountDownLatch pLatch, Task pRecords, int index) {
        this.mRunnable = pRunnable;
        this.mDownLatch = pLatch;
        this.mItem = pRecords;
        this.mIndex = index;
    }

    @Override
    public void run() {
        InputStream inputStream = null;
        RandomAccessFile fileStream = null;
        BufferedInputStream bis = null;
        boolean hasError = false;
        Block tempProcessItem = mItem.getBlocks().get(mIndex);
        try {
            long start = tempProcessItem.getStart() + tempProcessItem.getDownlength();
            long end = tempProcessItem.getStart() + tempProcessItem.getBlockLength();
//            L.e("下载块" + start + "->" + end);
            if (start > end) {
                //代表出错或下载完成
                start = end;
            } else {
                HttpURLConnection httpURLConnection = null;
                URL url = new URL(mItem.getDownUrl());
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setConnectTimeout(30 * 1000);
                // 设置读取超时时间 add by cbf 0411
                httpURLConnection.setReadTimeout(60 * 1000);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("connection", "keep-alive");
                httpURLConnection.setRequestProperty("Range", "bytes=" + start + "-" + end);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setAllowUserInteraction(true);
                // 不使用缓冲区
                // httpURLConnection.setUseCaches(false);
                // httpURLConnection.setChunkedStreamingMode(0);
                inputStream = httpURLConnection.getInputStream();
                bis = new BufferedInputStream(inputStream);
                /**
                 * 构建文件写出流。
                 */
                File dir = new File(mItem.getSaveDir());
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                fileStream = new RandomAccessFile(new File(dir, mItem.getSaveName()), "rw");
                fileStream.seek(start);
                byte[] buf = new byte[1024 * 2];
                int read = 0;
                long blockDownload = tempProcessItem.getDownlength();
                while (mRunnable.isDowning()) {
                    read = inputStream.read(buf);
                    if (read == -1) {
                        break;
                    }
                    fileStream.write(buf, 0, read);
                    blockDownload += read;
                    tempProcessItem.setDownlength(blockDownload);
                    mRunnable.addDownLength(read);
                }
            }
        } catch (Exception pE) {
            //下载出错
            hasError = true;
        } finally {
            if (hasError) {
                //告诉父线程，有错，要不要重新下载
                mRunnable.tryAgainDown(tempProcessItem);
            } else {
                //如果暂停就算没下载完.当前下载进度小于块长度
                tempProcessItem.setStatus(tempProcessItem.getDownlength() >= tempProcessItem.getBlockLength() ? Block.STATU_SUCCESS : Block.STATU_FAILE);
                mDownLatch.countDown();
            }
            //更新下载进度
            mItem.getBlocks().set(mIndex, tempProcessItem);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException pE) {
                    pE.printStackTrace();
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
