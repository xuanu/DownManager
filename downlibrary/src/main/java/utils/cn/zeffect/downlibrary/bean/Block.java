package utils.cn.zeffect.downlibrary.bean;

import com.litesuits.orm.db.annotation.Table;

import org.json.JSONException;
import org.json.JSONObject;

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
@Table(Constant.TABLE_NAME_BLOCK)
public class Block {
    public static final int STATU_SUCCESS = 2, STATU_FAILE = 1;
    private int id;
    private int index;
    private long blockLength;
    private long downlength;
    private long start;
    private long total;
    private int status;

    public int getStatus() {
        return status;
    }

    public Block setStatus(int pStatus) {
        status = pStatus;
        return this;
    }

    public int getId() {
        return id;
    }

    public Block setId(int pId) {
        id = pId;
        return this;
    }

    public int getIndex() {
        return index;
    }

    public Block setIndex(int pIndex) {
        index = pIndex;
        return this;
    }

    public long getBlockLength() {
        return blockLength;
    }

    public Block setBlockLength(long pBlockLength) {
        blockLength = pBlockLength;
        return this;
    }

    public long getDownlength() {
        return downlength;
    }

    public Block setDownlength(long pDownlength) {
        downlength = pDownlength;
        return this;
    }

    public long getStart() {
        return start;
    }

    public Block setStart(long pStart) {
        start = pStart;
        return this;
    }

    public long getTotal() {
        return total;
    }

    public Block setTotal(long pTotal) {
        total = pTotal;
        return this;
    }

    @Override
    public String toString() {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put(Constant.ID_KEY, id);
            dataJson.put(Constant.INDEX_KEY, index);
            dataJson.put(Constant.BLOCK_LENGTH_KEY, blockLength);
            dataJson.put(Constant.DOWN_LENGTH_KEY, downlength);
            dataJson.put(Constant.START_KEY, start);
            dataJson.put(Constant.TOTAL_KEY, total);
            dataJson.put(Constant.STATUS_KEY, status);
        } catch (JSONException pE) {
            pE.printStackTrace();
        } finally {
            return dataJson.toString();
        }
    }
}
