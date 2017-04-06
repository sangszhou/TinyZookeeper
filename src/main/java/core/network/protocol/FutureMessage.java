package core.network.protocol;

import io.netty.util.concurrent.Promise;

/**
 * Created by xinszhou on 22/03/2017.
 */
public interface FutureMessage extends Message {
    Promise<Message> getResponse();
}
