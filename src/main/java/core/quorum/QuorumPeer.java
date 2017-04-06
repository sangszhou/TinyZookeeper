package core.quorum;

import core.network.config.LeaderElectionConfig;
import core.network.protocol.Vote;
import core.network.protocol.ZXID;

/**
 * Created by xinszhou on 17/03/2017.
 */
public class QuorumPeer {

    long mySid;
    ZXID latestZxid;
    LeaderElectionConfig leaderElectionConfig;

    public LeaderElectionConfig getLeaderElectionConfig() {
        return leaderElectionConfig;
    }

    public void setLeaderElectionConfig(LeaderElectionConfig leaderElectionConfig) {
        this.leaderElectionConfig = leaderElectionConfig;
    }

    public Vote getCurrentVote() {
        return currentVote;
    }

    public void setCurrentVote(Vote currentVote) {
        this.currentVote = currentVote;
    }

    Vote currentVote;

    ServerState state = ServerState.LOOKING;

    public ServerState getState() {
        return state;
    }

    public void setState(ServerState state) {
        this.state = state;
    }

    public long getMySid() {
        return mySid;
    }

    public void setMySid(long mySid) {
        this.mySid = mySid;
    }

    public ZXID getLatestZxid() {
        return latestZxid;
    }

    public void setLatestZxid(ZXID latestZxid) {
        this.latestZxid = latestZxid;
    }

    public enum ServerState {
        LOOKING, FOLLOWING, LEADING, OBSERVING;
    }
}
