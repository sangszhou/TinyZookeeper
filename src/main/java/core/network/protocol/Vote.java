package core.network.protocol;

import core.quorum.QuorumPeer;

/**
 * Created by xinszhou on 17/03/2017.
 */
public class Vote {
    final private int version;

    final private long leader;

    final private long id;

    final private ZXID zxid;

    final private long electionEpoch;

    final private long peerEpoch;

    final private QuorumPeer.ServerState state;

    public Vote(long leader, ZXID zxid) {
        this.version = 0x0;
        this.zxid = zxid;
        this.leader = leader;
        this.state = QuorumPeer.ServerState.LOOKING;
        this.peerEpoch = -1;
        this.electionEpoch = -1;
        this.id = -1;
    }

    public Vote(long id,
                long leader,
                ZXID zxid,
                long electionEpoch,
                long peerEpoch,
                QuorumPeer.ServerState state) {
        this.version = 0x0;
        this.leader = leader;
        this.id = id;
        this.zxid = zxid;
        this.electionEpoch = electionEpoch;
        this.state = state;
        this.peerEpoch = peerEpoch;
    }

    public int getVersion() {
        return version;
    }

    public long getId() {
        return id;
    }

    public ZXID getZxid() {
        return zxid;
    }

    public long getElectionEpoch() {
        return electionEpoch;
    }

    public long getPeerEpoch() {
        return peerEpoch;
    }

    public QuorumPeer.ServerState getState() {
        return state;
    }

    public long getLeader() {
        return leader;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Vote)) {
            return false;
        }
        Vote other = (Vote) o;
        return (id == other.id
                && zxid == other.zxid
                && electionEpoch == other.electionEpoch
                && peerEpoch == other.peerEpoch);

    }

//    @Override
//    public int hashCode() {
//        return (int) (id & zxid);
//    }
//
//    public String toString() {
//        return "(" + id + ", " + Long.toHexString(zxid) + ", " + Long.toHexString(peerEpoch) + ")";
//    }


}
