package core.leaderElection;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import core.FastLeaderElection;
import core.network.QuorumCnxManager;
import core.network.config.LeaderElectionConfig;
import core.network.config.Node;
import core.network.protocol.Notification;
import core.quorum.QuorumPeer;
import core.util.JSONUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.TimeUnit;

/**
 * Created by xinszhou on 3/20/17.
 */
@RunWith(JUnit4.class)
public class FastLeaderElectionOneServerTest {

    // only server with port:7777 is available, other node are not online
    String oneServerConfig = "cluster {\n" +
            "  leaderElection: {\n" +
            "    host: \"localhost\"\n" +
            "    port: 5555\n" +
            "    sid: 1\n" +
            "    nodes: [\"1:localhost:5555\", \"2:localhost:6666\", \"3:localhost:7777\"]\n" +
            "  }\n" +
            "}";


    LeaderElectionConfig leaderElectionConfig = new LeaderElectionConfig();

    @Before
    public void loadConfig() {
        Config loadedConfig = ConfigFactory.parseString(oneServerConfig);
        leaderElectionConfig.load(loadedConfig);
        leaderElectionConfig.getAllSids().forEach(System.out::println);
        System.out.println(leaderElectionConfig.toString());
    }

    FastLeaderElection fle, fle2;


    @Test
    public void startServer() throws Exception {
        QuorumCnxManager quorumCnxManager = new QuorumCnxManager(leaderElectionConfig);
        quorumCnxManager.init();

        QuorumPeer p1 = new QuorumPeer();
        p1.setMySid(3);

        QuorumPeer p2 = new QuorumPeer();
        p1.setMySid(1);

        // create and start fastLeaderElection
        fle = new FastLeaderElection(quorumCnxManager, leaderElectionConfig, p1);

        // start a client server and expecting notification
        fle.sendNotification();


        LeaderElectionConfig leaderElectionConfig2 = new LeaderElectionConfig();
        leaderElectionConfig2.setSelf(new Node("localhost", 6666, 2));
        QuorumCnxManager quorumCnxManager2 = new QuorumCnxManager(leaderElectionConfig2);
        quorumCnxManager2.init();

        // fle2 has no notion of cluster, it only start a netty server
        fle2 = new FastLeaderElection(quorumCnxManager2, leaderElectionConfig2, p2);
        new Thread(fle2, "fast leader election 2").start(); // start receive notification from other node
        // fle should connect to fle2
        Notification n;

        while ((n = fle2.recvQueue.poll(3, TimeUnit.SECONDS)) == null){
            System.out.println("fle2 failed to poll data from recv queue ");
        }

        System.out.println(JSONUtils.toJson(n));

    }

    public void sendNotification() throws Exception {
        // send notification to other clients
//        fle.sendNotification();
    }

}
