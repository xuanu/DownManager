package utils.cn.zeffect.downlibrary.bean;

import android.text.TextUtils;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.Ignore;
import com.litesuits.orm.db.annotation.Mapping;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.Relation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

import utils.cn.zeffect.downlibrary.utils.Constant;

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
@Table(Constant.TABLE_NAME_TASK)
public class Task implements Serializable {
    public static final String COL_RECORD_STATUS = "record_status";
    /***
     * 下载记录应该会有三种状态。第一种显示下载进度，第二种不显示下载进度，第三种用户已经删除了。
     */
    public static final int COL_RECORD_STATUS_VISIBLE_0 = 0, COL_RECORD_STATUS_INVISIBLE_1 = 1, COL_RECORD_STATUS_HIDE_2 = 2;
    public static final int STATU_FAILE = 4, STATU_SUCCESS = 3, STATU_ING = 2, STATU_PAUSE = 1, STATU_NORMAL = 0;
    public static final int IS_MOBILE_DOWN = 0, IS_WIFI_DOWN = 1;
    private int id;
    private String uuid;
    @Column(Constant.URL_KEY)
    private String downUrl;
    private String saveDir;
    private String saveName;
    private long totalLength;
    private long downLength;
    private String blocksString;
    private int status;
    @Ignore
    private ArrayList<Block> blocks;
    /***
     * 值为1，表示WIFI时才下载，值为0流量也下载。其它都不下载
     */
    private int wifidown;
    @Column(COL_RECORD_STATUS)
    private int record_status;

    public int getRecord_status() {
        return record_status;
    }

    public Task setRecord_status(int pRecord_status) {
        record_status = pRecord_status;
        return this;
    }

    public String getBlocksString() {
        return blocksString;
    }

    public Task setBlocksString(String pBlocksString) {
        blocksString = pBlocksString;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public Task setStatus(int pStatus) {
        status = pStatus;
        return this;
    }

    public int getWifidown() {
        return wifidown;
    }

    public Task setWifidown(int pWifidown) {
        wifidown = pWifidown;
        return this;
    }

    public int getId() {
        return id;
    }

    public Task setId(int pId) {
        id = pId;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public Task setUuid(String pUuid) {
        uuid = pUuid;
        return this;
    }

    public String getDownUrl() {
        return downUrl;
    }

    public Task setDownUrl(String pDownUrl) {
        downUrl = pDownUrl;
        return this;
    }

    public String getSaveDir() {
        return saveDir;
    }

    public Task setSaveDir(String pSaveDir) {
        saveDir = pSaveDir;
        return this;
    }

    public String getSaveName() {
        return saveName;
    }

    public Task setSaveName(String pSaveName) {
        saveName = pSaveName;
        return this;
    }

    public long getTotalLength() {
        return totalLength;
    }

    public Task setTotalLength(long pTotalLength) {
        totalLength = pTotalLength;
        return this;
    }

    public long getDownLength() {
        return downLength;
    }

    public Task setDownLength(long pDownLength) {
        downLength = pDownLength;
        return this;
    }

    public ArrayList<Block> getBlocks() {
        return blocks;
    }

    public Task setBlocks(ArrayList<Block> pBlocks) {
        this.blocks = pBlocks;
        return this;
    }


    public static String blocksToString(ArrayList<Block> pBlocks) {
        StringBuilder retuString = new StringBuilder("");
        if (pBlocks != null && pBlocks.size() > 0) {
            retuString.append("[");
            for (int i = 0; i < pBlocks.size(); i++) {
                String blockJson = pBlocks.get(i).toString();
                retuString.append(blockJson);
                if (i != pBlocks.size() - 1) retuString.append(",");
            }
            retuString.append("]");
        }
        return retuString.toString();
    }

    /***
     * 把String转成ArrayList
     * @param data
     * @return
     */
    public static ArrayList<Block> toBlocks(String data) {
        ArrayList<Block> tempBlocks = new ArrayList<>();
        try {
            if (!TextUtils.isEmpty(data)) {
                JSONArray dataArray = new JSONArray(data);
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject blockJson = dataArray.getJSONObject(i);
                    Block tempBlock = new Block();
                    tempBlock.setStatus(blockJson.getInt(Constant.STATUS_KEY));
                    tempBlock.setTotal(blockJson.getLong(Constant.TOTAL_KEY));
                    tempBlock.setBlockLength(blockJson.getLong(Constant.BLOCK_LENGTH_KEY));
                    tempBlock.setDownlength(blockJson.getLong(Constant.DOWN_LENGTH_KEY));
                    tempBlock.setId(blockJson.getInt(Constant.ID_KEY));
                    tempBlock.setIndex(blockJson.getInt(Constant.INDEX_KEY));
                    tempBlock.setStart(blockJson.getInt(Constant.START_KEY));
                    tempBlocks.add(tempBlock);
                }
            }
        } catch (JSONException pE) {
            pE.printStackTrace();
        } finally {
            return tempBlocks;
        }
    }


}
