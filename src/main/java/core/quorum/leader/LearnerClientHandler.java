package core.quorum.leader;

import core.network.client.ClientHandler;
import core.network.protocol.DefaultFutureMessage;
import core.network.protocol.FutureMessage;
import core.network.protocol.Message;
import core.util.JSONUtils;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by xinszhou on 22/03/2017.
 */
//@todo what if handler call send() when client is not ready?
public class LearnerClientHandler extends SimpleChannelInboundHandler<Message> implements ClientHandler {
    Logger logger = LoggerFactory.getLogger(getClass());

    Channel channel;
    EventLoop eventLoop;
    LinkedBlockingQueue<FutureMessage> inFlightQueue = new LinkedBlockingQueue<>();

    public LearnerClientHandler(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        channel = ctx.channel();
    }

    // Message's in/out
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if (inFlightQueue.size() == 0) {
            logger.error("no waiting response message in inFlightQueue but response received " +
                    JSONUtils.toJson(msg));
        }

        FutureMessage inFlightMessage = inFlightQueue.poll();

        if (inFlightMessage.getMessageId() != msg.getMessageId()) {
            logger.error("message request and response not matching, request: " +
                    JSONUtils.toJson(inFlightMessage) + " response " +
                    JSONUtils.toJson(msg));
        }

        logger.info("request message match response message, request message \n" +
                JSONUtils.toJson(inFlightMessage) + " \n response message \n" +
                JSONUtils.toJson(msg)
        );

        inFlightMessage.getResponse().setSuccess(msg);
    }

    @Override
    public Future<Message> send(Message msg) {
        FutureMessage fMsg = new DefaultFutureMessage(msg, eventLoop);
        try {
            inFlightQueue.put(fMsg);
            channel.writeAndFlush(msg);
        } catch (Exception e) {
            logger.error("failed to get data", e);
        }

        return fMsg.getResponse();
    }

    @Override
    public void asyncSend(Message msg) {

    }
}
