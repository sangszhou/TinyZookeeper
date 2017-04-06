package core.quorum.leader;

import core.network.client.ClientHandler;
import core.network.client.RpcClientHandler;
import core.network.protocol.Vote;
import core.quorum.QuorumPeer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Created by xinszhou on 22/03/2017.
 */
public class Learner {
    Logger log = LoggerFactory.getLogger(getClass());

    QuorumPeer self;
    LearnerClientHandler clientHandler;
    EventLoopGroup eventLoopGroup;

    private void connectToServer(long sid) {
        Bootstrap b = new Bootstrap();
        b.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new ObjectEncoder());
                        pipeline.addLast(new ObjectDecoder(ClassResolvers
                                .cacheDisabled(getClass().getClassLoader())));
                        pipeline.addLast(new LearnerClientHandler(eventLoopGroup.next()));
                    }
                });

        ChannelFuture channelFuture = b.connect(self.getLeaderElectionConfig().getAddrBySid(sid));
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    // 必须是 channelHandler 的子类
                    clientHandler = channelFuture.channel().pipeline().get(LearnerClientHandler.class);
                } else {
                    Thread.sleep(5000);
                    connectToServer(sid);
                }
            }
        })
    }

    private InetSocketAddress findLeader() {
        Vote currentVote = self.getCurrentVote();
        long leaderId = currentVote.getLeader();
        return self.getLeaderElectionConfig().getAddrBySid(leaderId);
    }

    private void ping() {

    }

    void syncWithLeader() {

    }

    void registerWithLeader() {

    }


}
