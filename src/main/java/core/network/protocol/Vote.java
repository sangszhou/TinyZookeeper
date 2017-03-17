package core.network.protocol;

import core.quorum.QuorumPeer;

/**
 * Created by xinszhou on 17/03/2017.
 */
public class Vote {
    final private int version;

    final private long id;

    final private long zxid;

    final private long electionEpoch;

    final private long peerEpoch;

    final private QuorumPeer.ServerState state;

    public Vote(long id,
                long zxid,
                long electionEpoch,
                long peerEpoch,
                QuorumPeer.ServerState state) {
        this.version = 0x0;
        this.id = id;
        this.zxid = zxid;
        this.electionEpoch = electionEpoch;
        this.state = state;
        this.peerEpoch = peerEpoch;

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

    @Override
    public int hashCode() {
        return (int) (id & zxid);
    }

    public String toString() {
        return "(" + id + ", " + Long.toHexString(zxid) + ", " + Long.toHexString(peerEpoch) + ")";
    }


}
