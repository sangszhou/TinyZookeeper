package core.network.client;

import core.network.protocol.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;


/**
 * Created by xinszhou on 17/03/2017.
 */
public class RpcClientHandler extends SimpleChannelInboundHandler<Message> {

    Logger log = LoggerFactory.getLogger(getClass());

    private Channel channel;

    public LinkedBlockingQueue<Message> recvMessage;

    public RpcClientHandler(LinkedBlockingQueue<Message> queue) {
        recvMessage = queue;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        channel = ctx.channel();
    }

    public void send(Message message) {
        log.info("send message " + message.getMessageId() + " to dest " + message.getDestSid());
        channel.writeAndFlush(message);
    }

    // ideally, client won't receive any response, response alwasy received in server
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        long msgId = msg.getMessageId();
        log.info("message received on server " + msg.getDestSid() + " message id is " + msg.getMessageId());
        recvMessage.add(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
        log.info("Client caught exception");
        throwable.printStackTrace();
        ctx.close();
    }


}
