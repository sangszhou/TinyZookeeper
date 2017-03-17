package core.network.config;

/**
 * Created by xinszhou on 17/03/2017.
 */
public class Node {
    String host;
    int port;
    long sid;

    public Node(String host, int port, long sid) {
        this.host = host;
        this.port = port;
        this.sid = sid;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public long getSid() {
        return sid;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSid(long sid) {
        this.sid = sid;
    }
}
