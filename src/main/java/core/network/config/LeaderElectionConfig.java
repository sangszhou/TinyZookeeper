package core.network.config;

import com.google.common.net.HostAndPort;
import com.typesafe.config.Config;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by xinszhou on 17/03/2017.
 */
public class LeaderElectionConfig {
    Node self;

    //including self
    Map<Long, Node> clusterNodes = new HashMap<>();

    public List<Long> getAllSids() {
        List<Long> sids = new LinkedList<>();

        clusterNodes.entrySet().forEach(entry -> sids.add(entry.getValue().getSid()));

        return sids;
    }

    public InetSocketAddress getBySid(long sid) {
        Node target = clusterNodes.get(sid);
        if (target == null) {
            return null;
        }

        return new InetSocketAddress(target.host, target.port);
    }

    public Node getSelfInfo() {
        return self;
    }

    public void load(Config globalConfig) {
        Config le = globalConfig.getConfig("cluster.leaderElection");
        self = new Node(le.getString("host"), le.getInt("port"), le.getLong("sid"));

        List<String> nodes = le.getStringList("nodes");

        nodes.forEach(nodeInfo -> {
            String[] idHostPort = nodeInfo.split(":");
            long sid = Long.parseLong(idHostPort[0]);
            clusterNodes.put(sid, new Node(idHostPort[1], Integer.parseInt(idHostPort[2]), sid));
        });
    }

}
