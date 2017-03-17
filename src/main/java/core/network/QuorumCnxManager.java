package core.network;

import core.network.config.LeaderElectionConfig;
import core.network.protocol.Message;
import core.network.server.RpcServerHandler;
import core.util.GlobalConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import core.network.client.RpcClientHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

/**
 * Created by xinszhou on 17/03/2017.
 */
public class QuorumCnxManager {

    Logger log = LoggerFactory.getLogger(getClass());

    public LinkedBlockingQueue<Message> recvMsg = new LinkedBlockingQueue<>();
    ConcurrentHashMap<Long, RpcClientHandler> clientHandler = new ConcurrentHashMap<>();
    ;
    LeaderElectionConfig leaderElectionConfig = new LeaderElectionConfig();

    private static ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(4);
    EventLoopGroup group = new NioEventLoopGroup(4);


    public void init() throws Exception {

        leaderElectionConfig.load(GlobalConfig.getConfig);
        initServer();
        // wait how long?
        connectAllServers(leaderElectionConfig.getAllSids());
    }

    // client connection

    public void connectAllServers(List<Long> sids) {

        sids.forEach(sid -> {
            if (sid != leaderElectionConfig.getSelfInfo().getSid())
                connectServer(sid);
        });
    }

    // with retry?
    public void connectServer(long sid) {
        if (clientHandler.get(sid) != null) {
            log.info("client handler for id " + sid + " has already been established");
            return;
        }

        threadPoolExecutor.submit(() -> {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new RpcClientHandler(recvMsg));
                        }
                    });
            ChannelFuture channelFuture = b.connect(leaderElectionConfig.getBySid(sid));
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {

                    if (future.isSuccess()) {
                        log.info("connected to server sid: " + sid);
                        RpcClientHandler handler = channelFuture.channel().pipeline().get(RpcClientHandler.class);
                        clientHandler.put(sid, handler);
                    } else {
                        long backoffTime = 3000;
                        Thread.sleep(3000);
                        connectServer(sid);

                        log.error("connect to server failed, retry after 1000 second");
                    }


                }
            });
        });
    }

    public void send(long sid, Message msg) throws Exception {
        if (clientHandler.get(sid) == null) {
            throw new Exception("cannot send message to sid " + sid + ", client connection not established");
        }
        clientHandler.get(sid).send(msg);
    }

    // server establish
    private void initServer() throws Exception {

        RpcServerHandler serverHandler = new RpcServerHandler(recvMsg);

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(serverHandler);
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        // blocking start server
        ChannelFuture future = bootstrap.bind(leaderElectionConfig.getSelfInfo().getHost(),
                leaderElectionConfig.getSelfInfo().getPort()).sync();

    }

}
