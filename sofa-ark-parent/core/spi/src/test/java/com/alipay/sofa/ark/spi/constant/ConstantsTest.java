package com.alipay.sofa.ark.spi.constant;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.alipay.sofa.ark.spi.constant.Constants.*;
import static java.lang.Class.forName;
import static org.junit.Assert.assertEquals;

public class ConstantsTest {

    @Test
    public void testAllMethods() throws Exception {
        forName("com.alipay.sofa.ark.spi.constant.Constants");
        List<String> channelQuits = new ArrayList<>();
        channelQuits.add("quit");
        channelQuits.add("q");
        channelQuits.add("exit");
        assertEquals(channelQuits, CHANNEL_QUIT);
        assertEquals(new String(new byte[] { (byte) 13, (byte) 10 }), TELNET_STRING_END);
        assertEquals("", DEFAULT_PROFILE);
    }
}
