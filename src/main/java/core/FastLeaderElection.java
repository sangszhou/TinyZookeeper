package core;

import core.network.QuorumCnxManager;
import core.network.config.LeaderElectionConfig;
import core.network.config.Node;
import core.network.protocol.Message;
import core.network.protocol.Notification;
import core.network.protocol.ZXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    LeaderElectionConfig leaderElectionConfig;

    //    LinkedBlockingQueue<>
    LinkedBlockingQueue<Notification> recvQueue = new LinkedBlockingQueue<>();

    public FastLeaderElection(QuorumCnxManager cnxManager, LeaderElectionConfig config) {
        this.cnxManager = cnxManager;
        leaderElectionConfig = config;
    }

    public void init() {
        proposedEpoch = -1;
        proposedLeader = -1;
        proposedZxid = new ZXID(0, -1);
        stop = false;
    }

    // let other node knows
    public void sendNotification() throws Exception {
        leaderElectionConfig.getAllSids().forEach(sid -> {
            if (sid != leaderElectionConfig.getSelfInfo().getSid()) {
                Message notif = new Notification(msgId.getAndIncrement(), leaderElectionConfig.getSelfInfo().getSid(),
                        proposedLeader, proposedEpoch, proposedZxid);
                try {
                    cnxManager.send(sid, notif);
                } catch (Exception e) {
                    log.error("failed to send msg to sid: " + sid + ", from sid " + leaderElectionConfig.getSelfInfo().getSid(), e);
                }
            }
        });
    }

    protected boolean totalOrderPredict(long newEpoch, ZXID newZxid, long newId, long curlEpoch, ZXID curZxid, long curId) {
        if(newEpoch != curlEpoch) {
            if(newEpoch - curlEpoch > 0) return true;
            return false;
        }
        if (newZxid != curZxid) {
            if(newZxid.compareTo(curZxid) > 0) return true;
            return false;
        }
        if (newId > curId) {
            return true;
        }
        return false;
    }

    protected boolean terminatePredicate(HashMap<Long, Vote>)

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

                } else {
                    log.info("recv msg is not type of notification");
                }


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

    // blocking operation
    public Node lookForLeader() {
        return null;
    }


}
