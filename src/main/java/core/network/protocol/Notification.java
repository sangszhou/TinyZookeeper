package core.network.protocol;

import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by xinszhou on 17/03/2017.
 */
public class Notification implements Message {

    static final long serialVersionUID = 1L;

    long msgId;
    long sid;
    long proposedLeader;
    long epoch;

    ZXID zxid;

    public Notification(long msgId, long sid, long proposedLeader, long epoch, ZXID zxid) {
        this.msgId = msgId;
        this.sid = sid;
        this.proposedLeader = proposedLeader;
        this.epoch = epoch;
        this.zxid = zxid;
    }

    @Override
    public long getMessageId() {
        return msgId;
    }

    @Override
    public long getDestSid() {
        return sid;
    }
}
