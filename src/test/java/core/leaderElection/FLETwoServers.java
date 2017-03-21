package core.leaderElection;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import core.FastLeaderElection;
import core.network.QuorumCnxManager;
import core.network.config.LeaderElectionConfig;
import core.network.config.Node;
import core.network.protocol.Vote;
import core.network.protocol.ZXID;
import core.quorum.QuorumPeer;
import core.util.JSONUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by xinszhou on 21/03/2017.
 */
public class FLETwoServers {
    String oneServerConfig = "cluster {\n" +
            "  leaderElection: {\n" +
            "    host: \"localhost\"\n" +
            "    port: 5555\n" +
            "    sid: 1\n" +
            "    nodes: [\"1:localhost:5555\", \"2:localhost:6666\"]\n" +
            "  }\n" +
            "}";

    LeaderElectionConfig leaderElectionConfig1 = new LeaderElectionConfig();
    LeaderElectionConfig leaderElectionConfig2 = new LeaderElectionConfig();

    @Before
    public void loadConfig() {
        Config loadedConfig = ConfigFactory.parseString(oneServerConfig);
        leaderElectionConfig1.load(loadedConfig);
        leaderElectionConfig1.getAllSids().forEach(System.out::println);
        System.out.println(leaderElectionConfig1.toString());

        leaderElectionConfig2.load(loadedConfig);
        leaderElectionConfig2.setSelf(new Node("localhost", 6666, 2));
    }

    FastLeaderElection fle1, fle2;

    @Test
    public void sameZxidDiffSid() throws Exception {
        QuorumCnxManager quorumCnxManager1 = new QuorumCnxManager(leaderElectionConfig1);
        quorumCnxManager1.init();

        QuorumCnxManager quorumCnxManager2 = new QuorumCnxManager(leaderElectionConfig2);
        quorumCnxManager2.init();

        QuorumPeer peer1 = new QuorumPeer();
        peer1.setMySid(1);

        QuorumPeer peer2 = new QuorumPeer();
        peer2.setMySid(2);

        fle1 = new FastLeaderElection(quorumCnxManager1, leaderElectionConfig1, peer1);
        fle2 = new FastLeaderElection(quorumCnxManager2, leaderElectionConfig2, peer2);

        new Thread(fle1, "fle1").start();
        new Thread(fle2, "fle2").start();

        // wait for leader

        Thread t1 = new Thread(() -> {
            try {
                Vote v1 = fle1.lookForLeader();
                System.out.println("v1: " + JSONUtils.toJson(v1));

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                Vote v2 = fle2.lookForLeader();
                System.out.println("v2: " + JSONUtils.toJson(v2));

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        // result, because node 2 has bigger thread id, so leader 2 win
        // v2: {"version":0,"leader":2,"id":-1,"electionEpoch":-1,"peerEpoch":-1,"state":"LOOKING"}
        // v1: {"version":0,"leader":2,"id":-1,"electionEpoch":-1,"peerEpoch":-1,"state":"LOOKING"}
    }

    @Test
    public void differentZxid() throws Exception {
        QuorumCnxManager quorumCnxManager1 = new QuorumCnxManager(leaderElectionConfig1);
        quorumCnxManager1.init();

        QuorumCnxManager quorumCnxManager2 = new QuorumCnxManager(leaderElectionConfig2);
        quorumCnxManager2.init();

        QuorumPeer peer1 = new QuorumPeer();
        peer1.setMySid(1);
        peer1.setLatestZxid(new ZXID(10, 10));

        QuorumPeer peer2 = new QuorumPeer();
        peer2.setMySid(2);
        peer2.setLatestZxid(new ZXID(3, 9));

        fle1 = new FastLeaderElection(quorumCnxManager1, leaderElectionConfig1, peer1);
        fle2 = new FastLeaderElection(quorumCnxManager2, leaderElectionConfig2, peer2);

        new Thread(fle1, "fle1").start();
        new Thread(fle2, "fle2").start();

        // wait for leader

        Thread t1 = new Thread(() -> {
            try {
                Vote v1 = fle1.lookForLeader();
                System.out.println("v1: " + JSONUtils.toJson(v1));

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                Vote v2 = fle2.lookForLeader();
                System.out.println("v2: " + JSONUtils.toJson(v2));

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();

//        v1: {"version":0,"leader":1,"id":-1,"zxid":{"counter":10,"epoch":10},"electionEpoch":-1,"peerEpoch":-1,"state":"LOOKING"}
//        v2: {"version":0,"leader":1,"id":-1,"zxid":{"counter":10,"epoch":10},"electionEpoch":-1,"peerEpoch":-1,"state":"LOOKING"}

        System.out.println("peer1 state: " + peer1.getState());
        System.out.println("peer2 state: " + peer2.getState());
    }

}
