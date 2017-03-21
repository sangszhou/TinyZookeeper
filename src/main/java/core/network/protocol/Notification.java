package core.network.protocol;

import core.quorum.QuorumPeer;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by xinszhou on 17/03/2017.
 */
public class Notification implements Message {
    long msgId;
    long sid;
    long destId;
    long proposedLeader;
    long epoch;
    QuorumPeer.ServerState state = QuorumPeer.ServerState.LOOKING;
    ZXID zxid;

    public Notification(long msgId, long sid, long destId, long proposedLeader, long epoch, ZXID zxid) {
        this.destId = destId;
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
        return destId;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public long getMsgId() {
        return msgId;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public long getSid() {
        return sid;
    }

    public void setSid(long sid) {
        this.sid = sid;
    }

    public long getProposedLeader() {
        return proposedLeader;
    }

    public void setProposedLeader(long proposedLeader) {
        this.proposedLeader = proposedLeader;
    }

    public long getEpoch() {
        return epoch;
    }

    public void setEpoch(long epoch) {
        this.epoch = epoch;
    }

    public QuorumPeer.ServerState getState() {
        return state;
    }

    public void setState(QuorumPeer.ServerState state) {
        this.state = state;
    }

    public void setDestId(long destId) {
        this.destId = destId;
    }

    public ZXID getZxid() {
        return zxid;
    }

    public void setZxid(ZXID zxid) {
        this.zxid = zxid;
    }
}
