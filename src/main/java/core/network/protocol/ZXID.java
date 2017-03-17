package core.network.protocol;

import java.io.Serializable;

/**
 * Created by xinszhou on 17/03/2017.
 */
public class ZXID implements Serializable, Comparable<ZXID> {

    static final long serialVersionUID = 1L;

    long counter;
    long epoch;

    public ZXID(long counter, long epoch) {
        this.counter = counter;
        this.epoch = epoch;
    }

    public long getCounter() {
        return counter;
    }

    public long getEpoch() {
        return epoch;
    }

    public void setCounter(long counter) {
        this.counter = counter;
    }

    public void setEpoch(long epoch) {
        this.epoch = epoch;
    }

    @Override
    public int compareTo(ZXID o) {
        if (epoch - o.epoch != 0) {
            return Math.toIntExact(epoch - o.epoch);
        }
        return Math.toIntExact(counter - o.counter);
    }
}
