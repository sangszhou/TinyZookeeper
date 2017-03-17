package core.network;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

/**
 * Created by xinszhou on 17/03/2017.
 */
@RunWith(JUnit4.class)
public class QuorumCnxManagerTest {
    QuorumCnxManager cnxMgr = new QuorumCnxManager();

    @Before
    public void init() throws Exception {
        System.out.println("init");
        cnxMgr.init();
    }

    @Test
    public void test() throws Exception {
        System.out.println("how");
        Thread.sleep(3000090);
    }



}