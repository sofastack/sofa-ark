package com.alipay.sofa.ark.spi.replay;

import org.junit.Test;

import static com.alipay.sofa.ark.spi.replay.ReplayContext.*;
import static org.junit.Assert.assertEquals;

public class ReplayContextTest {

    @Test
    public void testAllMethods() {

        assertEquals(null, get());

        set("0.0");
        assertEquals("0.0", get());

        unset();
        assertEquals(null, get());

        setPlaceHolder();
        assertEquals(null, get());

        set("2.0");
        setPlaceHolder();
        assertEquals(PLACEHOLDER, get());

        clearPlaceHolder();
        assertEquals("2.0", get());

        clearPlaceHolder();
        assertEquals("2.0", get());
    }
}
