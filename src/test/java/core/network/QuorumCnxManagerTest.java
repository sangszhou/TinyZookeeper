package core.network;

import core.network.client.RpcClientHandler;
import core.network.config.LeaderElectionConfig;
import core.network.config.Node;
import core.network.protocol.Ack;
import core.network.protocol.Message;
import core.network.protocol.Notification;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by xinszhou on 17/03/2017.
 */
@RunWith(JUnit4.class)
public class QuorumCnxManagerTest {

    LeaderElectionConfig leaderElectionConfig;
    QuorumCnxManager cnxMgr;

    @Before
    public void startServer() throws Exception {
        System.out.println("init");
        leaderElectionConfig = new LeaderElectionConfig();
        Node self = new Node("localhost", 5555, 1);
        Map<Long, Node> cluster = new HashMap<>();
        cluster.put(Long.parseLong("1"), self);
        leaderElectionConfig.setSelf(self);
        leaderElectionConfig.setClusterNodes(cluster);
        cnxMgr = new QuorumCnxManager(leaderElectionConfig);
        cnxMgr.init();
    }

    // send message to quorum cnx manager
    @Test
    public void sendMsgToServer() throws Exception {
        cnxMgr.connectServer(1);

        while (cnxMgr.clientHandler.get(Long.parseLong("1")) == null) {
            System.out.println("retry");
            Thread.sleep(1000);
        }

        RpcClientHandler handler = cnxMgr.clientHandler.get(Long.parseLong("1"));

        handler.send(new Ack());

        while (cnxMgr.recvMsg.peek() == null) {
            Thread.sleep(5000);
        }

        Message recvMsg = cnxMgr.recvMsg.poll();
        System.out.println("msg id: " + recvMsg.getMessageId());
        System.out.println("dest id: " + recvMsg.getDestSid());
    }



}