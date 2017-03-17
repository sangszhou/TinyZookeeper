package core.network;

import com.google.common.net.HostAndPort;
import com.typesafe.config.Config;
import core.util.GlobalConfig;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xinszhou on 17/03/2017.
 */
//public class ClusterConfig {
//
//    ConcurrentHashMap<Long, InetSocketAddress> leaderElectionInetaddr;
//
//    public InetSocketAddress getIpAddr(long sid) {
//        return leaderElectionInetaddr.get(sid);
//    }
//
//    public void parseConfig() {
////        Config config = GlobalConfig.getConfig;
////        List<String> nodes = config.getStringList("cluster.leaderElection.nodes");
////
////        nodes.forEach(nodeInfo -> {
////            String[] idHostPort = nodeInfo.split(":");
////            long id = Long.parseLong(idHostPort[0]);
////            HostAndPort addr = HostAndPort.fromString(idHostPort[1] + ":" + idHostPort[2]);
////            leaderElectionInetaddr.put(id, new InetSocketAddress(addr.getHost(), addr.getPort()));
////        });
//
//    }
//}
