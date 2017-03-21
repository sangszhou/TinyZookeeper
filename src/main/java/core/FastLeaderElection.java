package core;

import com.google.common.annotations.VisibleForTesting;
import com.sun.corba.se.spi.activation.Server;
import core.network.QuorumCnxManager;
import core.network.config.LeaderElectionConfig;
import core.network.config.Node;
import core.network.protocol.Message;
import core.network.protocol.Notification;
import core.network.protocol.Vote;
import core.network.protocol.ZXID;
import core.quorum.QuorumPeer;
import core.util.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import core.quorum.QuorumPeer.ServerState;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by xinszhou on 17/03/2017.
 */
public class FastLeaderElection implements Runnable {

    Logger log = LoggerFactory.getLogger(getClass());

    QuorumCnxManager cnxManager;

    volatile boolean stop;

    // leader election always runs

    AtomicLong logicalclock = new AtomicLong(); /* Election instance */
    AtomicLong msgId = new AtomicLong();

    long proposedLeader;
    long proposedEpoch;
    ZXID proposedZxid;

    long waitTime = 5;

    QuorumPeer self;

    // configuration read from configurtion file
    LeaderElectionConfig leaderElectionConfig;

    @VisibleForTesting
    public LinkedBlockingQueue<Notification> recvQueue = new LinkedBlockingQueue<>();

    @VisibleForTesting
    public HashMap<Long, Vote> recvVote = new HashMap<>();
    // 当 peer 已经处于 looking 或者 leading 状态下后，新来的动作


    @VisibleForTesting
    public HashMap<Long, Vote> outofelecition = new HashMap<>();

    public FastLeaderElection(QuorumCnxManager cnxManager, LeaderElectionConfig config, QuorumPeer self) {
        init();
        this.cnxManager = cnxManager;
        this.self = self;
        leaderElectionConfig = config;
    }

    private void init() {
        proposedEpoch = -1;
        proposedLeader = -1;
        proposedZxid = new ZXID(0, -1);
        stop = false;
    }

    // let other node knows
    public void sendNotification() throws Exception {
        // send notification to self
        {
            Notification notif = new Notification(msgId.getAndIncrement(), self.getMySid(), self.getMySid(), proposedLeader, logicalclock.get(), proposedZxid);
            // send notification to self
            recvQueue.put(notif);
        }

        leaderElectionConfig.getAllSids().forEach(sid -> {
            if (sid != self.getMySid()) {
                Notification notif2 = new Notification(msgId.getAndIncrement(), self.getMySid(), sid, proposedLeader, logicalclock.get(), proposedZxid);
                try {
                    log.info("send notification to sid: " + sid + ", notif: " + JSONUtils.toJson(notif2));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cnxManager.send(sid, notif2);
            }
        });
    }

    // 注意，这里并不是 mysid 而是 proposed leader
    protected boolean totalOrderPredict(ZXID newZxid, long newId) {
        if (newZxid != proposedZxid) {
            if (newZxid.compareTo(proposedZxid) > 0) return true;
            return false;
        }
        if (newId > proposedLeader) {
            return true;
        }
        return false;
    }

    // key is node, value is the leader, the node proposed
    // why we need the second zxid? to make sure epoch is same???
    protected boolean terminatePredicate(HashMap<Long, Vote> votes, long leader, ZXID zxid) {
        int count = 0;

        Collection<Vote> voteView = votes.values();
        for (Vote vote : voteView) {
            if (vote.getLeader() == leader)
                count++;
        }

        if (count > votes.size() / 2) {
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                Message newRecvMsg = cnxManager.recvMsg.poll(3, TimeUnit.SECONDS);

                if (newRecvMsg == null) {
                    log.info("poll data failed, retry poll");
                    continue;
                }

                if (newRecvMsg instanceof Notification) {
                    log.info("receive notification from server, add it to queue");
                    recvQueue.offer((Notification) newRecvMsg);
                } else {
                    log.info("recv msg is not type of notification: " + newRecvMsg.getClass().getName());
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

    // blocking operation
    public Vote lookForLeader() throws Exception {

        logicalclock.incrementAndGet();
        proposedLeader = self.getMySid();
        proposedZxid = self.getLatestZxid();

        log.info("start send notifications to other nodes: ");
        sendNotification();

        while (self.getState() == QuorumPeer.ServerState.LOOKING) {
            // todo, set timeout
            Notification newn = recvQueue.poll(5, TimeUnit.SECONDS);

            if (newn == null) {
                log.info("failed to receive nodes from other node in 5 seconds, there must be something wrong");
                sendNotification();
                continue;
            }

            switch (newn.getState()) {
                case LOOKING:
                    if (newn.getEpoch() > logicalclock.get()) {
                        log.info("new node come with higher epoch, update self epoch and vote counter");
                        logicalclock.set(newn.getEpoch());
                        recvVote.clear();

                        // update self state only if leader and nid is bigger?
                        if (totalOrderPredict(newn.getZxid(), newn.getProposedLeader())) {
                            proposedLeader = newn.getProposedLeader();
                            proposedZxid = newn.getZxid();
                        }
                        sendNotification();
                    } else if (newn.getEpoch() < logicalclock.get()) {
                        log.info("detect old epoch number");
                        break;
                    } else {
                        log.info("epoch same with incoming request");
                        if (totalOrderPredict(newn.getZxid(), newn.getProposedLeader())) {
                            proposedLeader = newn.getProposedLeader();
                            proposedZxid = newn.getZxid();
                            sendNotification();
                        }
                    }

                    recvVote.put(newn.getSid(), new Vote(newn.getSid(),
                            newn.getProposedLeader(),
                            newn.getZxid(), -1, -1, ServerState.LOOKING));

                    if (terminatePredicate(recvVote, newn.getProposedLeader(), newn.getZxid())) {
                        Thread.sleep(waitTime * 1000);

                        // deprecated,
//                        while (!recvQueue.isEmpty() && !totalOrderPredict(recvQueue.peek().getZxid(),
//                                recvQueue.peek().getProposedLeader())) {
//                            recvQueue.poll();
//                        }

                        // this method is better
                        while ((newn = recvQueue.poll(waitTime, TimeUnit.SECONDS)) != null) {
                            if (totalOrderPredict(newn.getZxid(), newn.getProposedLeader())) {
                                recvQueue.put(newn);
                                break;
                            }
                        }

                        if (recvQueue.isEmpty()) {
                            if (proposedLeader == self.getMySid()) {
                                self.setState(ServerState.LEADING);
                            } else {
                                self.setState(ServerState.FOLLOWING);
                            }

                            leaveInstance();
                            return new Vote(proposedLeader, proposedZxid);
                        }
                    }
                    break;

                case LEADING:
                case FOLLOWING:
                    // if still in the same epoch, use existing data
                    if (newn.getEpoch() == logicalclock.get()) {
                        recvVote.put(newn.getSid(), new Vote(newn.getSid(), newn.getProposedLeader(),
                                newn.getZxid(), -1, -1, ServerState.LOOKING));

                        if (terminatePredicate(recvVote, newn.getProposedLeader(), newn.getZxid())) {
                            if (newn.getProposedLeader() != self.getMySid()) {
                                self.setState(ServerState.LEADING);
                            } else {
                                self.setState(ServerState.FOLLOWING);
                            }

                            Vote vote = new Vote(newn.getSid(), newn.getProposedLeader(),
                                    newn.getZxid(), -1, -1, ServerState.LOOKING);
                            leaveInstance();
                            return vote;
                        }

                        final Vote newVote = notif2Vote(newn);

                        outofelecition.put(newn.getSid(), newVote);

                        if (terminatePredicate(outofelecition, newn.getProposedLeader(), newn.getZxid())) {
                            synchronized (this) {
                                logicalclock.set(newn.getEpoch());
                                updateState(newn);
                            }
                            leaveInstance();
                            return newVote;
                        }
                    }

                    break;
                default:
                    log.info("peer in some sort of uncommon state");
            }

        }
        return null;
    }

    // what gonna happen when state is changed ?
    public void updateState(Notification newn) {
        if (newn.getProposedLeader() == self.getMySid()) {
            self.setState(ServerState.LEADING);
        } else {
            self.setState(ServerState.FOLLOWING);
        }
    }

    // election epoch and epoch, what's difference
    public Vote notif2Vote(Notification newn) {
        return new Vote(newn.getSid(), newn.getProposedLeader(),
                newn.getZxid(), newn.getEpoch(), newn.getEpoch(), newn.getState());
    }

    // 每次 Leader 选举完都要加 2 的？
    private void leaveInstance() {
//        logicalclock.getAndIncrement();

        recvVote.clear();
    }


}
