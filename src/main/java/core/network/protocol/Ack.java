package core.network.protocol;

/**
 * Created by xinszhou on 3/20/17.
 */
public class Ack implements Message {

    @Override
    public long getMessageId() {
        return 0;
    }

    @Override
    public long getDestSid() {
        return 0;
    }

    @Override
    public long getFromSid() {
        return 0;
    }


}
