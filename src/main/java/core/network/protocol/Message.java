package core.network.protocol;

import java.io.Serializable;

/**
 * Created by xinszhou on 17/03/2017.
 */
public interface Message extends Serializable {

    long serialVersionUID = 1L;

    long getMessageId();

    long getDestSid();

}
