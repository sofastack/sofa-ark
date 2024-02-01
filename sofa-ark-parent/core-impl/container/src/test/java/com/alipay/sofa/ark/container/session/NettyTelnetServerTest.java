/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.ark.container.session;

import com.alipay.sofa.ark.container.service.ArkServiceContainer;
import com.alipay.sofa.ark.container.session.NettyTelnetServer.NettyTelnetHandler;
import com.alipay.sofa.ark.container.session.NettyTelnetServer.NettyTelnetInitializer;
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;

import static com.alipay.sofa.ark.common.util.EnvironmentUtils.getProperty;
import static com.alipay.sofa.ark.common.util.EnvironmentUtils.setProperty;
import static com.alipay.sofa.ark.container.service.ArkServiceContainerHolder.getContainer;
import static com.alipay.sofa.ark.container.service.ArkServiceContainerHolder.setContainer;
import static com.alipay.sofa.ark.spi.constant.Constants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class NettyTelnetServerTest {

    private ArkServiceContainer originalArkServiceContainer;

    private String              originalSecurityEnable;

    private String              originalTelnetPort;

    @Before
    public void before() {
        originalArkServiceContainer = getContainer();
        originalSecurityEnable = getProperty(TELNET_SERVER_SECURITY_ENABLE);
        originalTelnetPort = getProperty(TELNET_PORT_ATTRIBUTE);
    }

    @After
    public void after() {
        setContainer(originalArkServiceContainer);
        setProperty(TELNET_SERVER_SECURITY_ENABLE,
            originalSecurityEnable != null ? originalSecurityEnable : "");
        setProperty(TELNET_PORT_ATTRIBUTE, originalTelnetPort != null ? originalTelnetPort : "");
    }

    @Test
    public void testNettyTelnetInitializer() throws Exception {

        SocketChannel socketChannel = mock(SocketChannel.class);
        ChannelPipeline pipeline = mock(ChannelPipeline.class);
        when(socketChannel.pipeline()).thenReturn(pipeline);

        InetSocketAddress inetSocketAddress = mock(InetSocketAddress.class);
        when(socketChannel.remoteAddress()).thenReturn(inetSocketAddress);
        when(inetSocketAddress.getHostName()).thenReturn(LOCAL_HOST + "1");
        setContainer(mock(ArkServiceContainer.class));

        NettyTelnetInitializer nettyTelnetInitializer = new NettyTelnetInitializer();
        nettyTelnetInitializer.initChannel(socketChannel);

        setProperty(TELNET_SERVER_SECURITY_ENABLE, "true");
        nettyTelnetInitializer.initChannel(socketChannel);
        verify(pipeline, times(4)).addLast(any());
    }

    @Test
    public void testNettyTelnetHandler() throws Exception {

        setContainer(mock(ArkServiceContainer.class));
        NettyTelnetHandler nettyTelnetHandler = new NettyTelnetHandler();
        ChannelHandlerContext context = mock(ChannelHandlerContext.class);
        when(context.channel()).thenReturn(mock(Channel.class));

        nettyTelnetHandler.channelActive(context);
        nettyTelnetHandler.exceptionCaught(context, new Exception());
        nettyTelnetHandler.channelRead0(context, "");
        nettyTelnetHandler.channelRead0(context, "q");
    }

    @Test(expected = ArkRuntimeException.class)
    public void testStandardTelnetServerImplWithInvalidNumber() {
        setProperty(TELNET_PORT_ATTRIBUTE, "a");
        new StandardTelnetServerImpl();
    }
}
