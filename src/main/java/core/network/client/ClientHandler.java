package core.network.client;

import core.network.protocol.Message;

import java.util.concurrent.Future;

/**
 * Created by xinszhou on 22/03/2017.
 */
// how to ensure first in first out?
public interface ClientHandler {

    Future<Message> send(Message msg);

    void asyncSend(Message msg);
}
