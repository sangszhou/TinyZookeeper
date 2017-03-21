package core.network;

import com.google.common.annotations.VisibleForTesting;
import core.network.config.LeaderElectionConfig;
import core.network.protocol.Message;
import core.network.protocol.Notification;
import core.network.protocol.ZXID;
import core.network.server.RpcServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import core.network.client.RpcClientHandler;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
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

    // may not ready
    @VisibleForTesting
    public ConcurrentHashMap<Long, RpcClientHandler> clientHandler = new ConcurrentHashMap<>();

    LeaderElectionConfig leaderElectionConfig;

    private static ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(4);

    ConcurrentHashMap<Long, LinkedBlockingQueue<Message>> toSentMessages = new ConcurrentHashMap<>();

    EventLoopGroup group = new NioEventLoopGroup(4);

    public QuorumCnxManager(LeaderElectionConfig leaderElectionConfig) {
        this.leaderElectionConfig = leaderElectionConfig;
    }

    public void init() throws Exception {
//        leaderElectionConfig.load(GlobalConfig.getConfig);
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
                            pipeline.addLast(new ObjectEncoder());
                            pipeline.addLast(new ObjectDecoder(ClassResolvers
                                    .cacheDisabled(getClass().getClassLoader())));
                            pipeline.addLast(new RpcClientHandler(recvMsg));
                        }
                    });

            ChannelFuture channelFuture = b.connect(leaderElectionConfig.getAddrBySid(sid));

            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        log.info("connected to server sid: " + sid);
                        RpcClientHandler handler = channelFuture.channel().pipeline().get(RpcClientHandler.class);
//                        handler.send(new Notification(1, 2, 3, 4, new ZXID(-1, -1)));
                        clientHandler.put(sid, handler);
                        // start send thread for this connection

                        toSentMessages.putIfAbsent(sid, new LinkedBlockingQueue<>());
                        SendWorker sendWorker = new SendWorker(handler, toSentMessages.get(sid));
                        new Thread(sendWorker, "send-worker-thread-for-sid:"+sid).start();
                    } else {
                        Thread.sleep(3000);
                        // repeatly connect to server
                        connectServer(sid);
                        log.error(leaderElectionConfig.getSelf().getSid() +
                                " connect to server " + sid +  " failed, retry after 1000 second");
                    }
                }
            });
        });
    }

    // @todo need add queue, in case the dest server is not online
    // queue can maintain sending list
    public void send(long sid, Message msg) {
        log.info("add message to queue of sid: " + sid);
        toSentMessages.putIfAbsent(sid, new LinkedBlockingQueue<>());
        toSentMessages.get(sid).add(msg);
    }

    class SendWorker implements Runnable {
        // ready when created
        RpcClientHandler rpcClientHandler;
        LinkedBlockingQueue<Message> queue;
        Message lastMsg;

        public SendWorker(RpcClientHandler clientChannel, LinkedBlockingQueue<Message> queue) {
            this.rpcClientHandler = clientChannel;
            this.queue = queue;
        }

        @Override
        public void run() {
            log.info("send worker run() triggered");

            while (true) {
                try {
                    Message newMsg = queue.poll(5, TimeUnit.SECONDS);
                    if(newMsg != null) {
                        log.info("send worker send message to sid: " + newMsg.getDestSid());
                        lastMsg = newMsg;
                        rpcClientHandler.send(newMsg);
                    } else {
                        if (lastMsg != null) {
                            rpcClientHandler.send(lastMsg);
                        } else {
                            log.info("send worker has no message to send");
                        }
                    }
                } catch (InterruptedException exp) {
                    log.error("interrupted", exp);
                }

            }
        }
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
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new ObjectEncoder());
                        pipeline.addLast(new ObjectDecoder(ClassResolvers
                                .cacheDisabled(getClass().getClassLoader())));
                        pipeline.addLast(serverHandler);
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        // blocking start server
        ChannelFuture future = bootstrap.bind(leaderElectionConfig.getSelfInfo().getHost(),
                leaderElectionConfig.getSelfInfo().getPort()).sync();

        // this method will block, so don't use it when server in dedicated thread
//        future.channel().closeFuture().sync();
    }

}
