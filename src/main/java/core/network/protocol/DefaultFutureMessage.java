package core.network.protocol;

import io.netty.channel.EventLoop;
import io.netty.util.concurrent.DefaultProgressivePromise;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;

/**
 * Created by xinszhou on 22/03/2017.
 */
public class DefaultFutureMessage implements FutureMessage {
    Promise<Message> futureReply;
    Message request;


    public DefaultFutureMessage(Message request, EventLoop eventLoop) {
        futureReply = new DefaultPromise<>(eventLoop);
        this.request = request;
    }

    @Override
    public Promise<Message> getResponse() {
        return futureReply;
    }

    @Override
    public long getMessageId() {
        return request.getMessageId();
    }

    @Override
    public long getDestSid() {
        return request.getDestSid();
    }

    @Override
    public long getFromSid() {
        return request.getFromSid();
    }
}
