package core.network.server;

import core.network.protocol.Message;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by xinszhou on 17/03/2017.
 */
@ChannelHandler.Sharable
public class RpcServerHandler extends SimpleChannelInboundHandler<Message> {

    Logger log = LoggerFactory.getLogger(getClass());

    public LinkedBlockingQueue<Message> recvMessage;

    public RpcServerHandler(LinkedBlockingQueue<Message> recvMessage) {
        this.recvMessage = recvMessage;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log.info("channel registered on server");
        ctx.fireChannelRegistered();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        long msgId = msg.getMessageId();
        System.out.println("message received on server");
        log.info("message received on server " + msg.getDestSid() +
                " message id is " + msg.getMessageId());
        // thread safe?
        recvMessage.add(msg);
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
        log.error(throwable.getMessage(), "in server handler");
        ctx.close();
    }
}
