package core.network.protocol;

import core.util.JSONUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by xinszhou on 21/03/2017.
 */
public class NotificationTest {

    @Test
    public void serialize()  throws Exception {
        Notification notif = new Notification(1, 1, 2, 2, 1, new ZXID(3, 1));

        System.out.println(JSONUtils.toJson(notif));
    }

}