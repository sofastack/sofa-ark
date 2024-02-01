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

import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.util.EnvironmentUtils;
import com.alipay.sofa.ark.container.session.handler.ArkCommandHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.concurrent.Executor;

import static com.alipay.sofa.ark.spi.constant.Constants.CHANNEL_QUIT;
import static com.alipay.sofa.ark.spi.constant.Constants.LOCAL_HOST;

/**
 * @author qilong.zql
 * @since 1.0.0
 */
public class NettyTelnetServer {

    private int             port;
    private ServerBootstrap serverBootstrap;
    private EventLoopGroup  bossGroup;
    private EventLoopGroup  workerGroup;
    private Channel         channel;

    public NettyTelnetServer(int port, Executor executor) {
        this.port = port;
        bossGroup = new NioEventLoopGroup(1, executor);
        workerGroup = new NioEventLoopGroup(1, executor);
    }

    public void open() throws InterruptedException {
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
            .handler(new LoggingHandler(LogLevel.INFO)).childHandler(new NettyTelnetInitializer());
        channel = serverBootstrap.bind(port).sync().channel();
    }

    public void close() {
        channel.close();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    static class NettyTelnetInitializer extends ChannelInitializer<SocketChannel> {
        private static StringDecoder DECODER = new StringDecoder();
        private static StringEncoder ENCODER = new StringEncoder();

        @Override
        protected void initChannel(SocketChannel channel) throws Exception {
            if (EnvironmentUtils.isOpenSecurity()) {
                if (!channel.remoteAddress().getHostName().equals(LOCAL_HOST)) {
                    return;
                }
            }
            ChannelPipeline pipeline = channel.pipeline();
            pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
            pipeline.addLast(ENCODER);
            pipeline.addLast(DECODER);
            pipeline.addLast(new NettyTelnetHandler());
        }
    }

    static class NettyTelnetHandler extends SimpleChannelInboundHandler<String> {

        private static ArkCommandHandler arkCommandHandler = new ArkCommandHandler();

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            ctx.write(arkCommandHandler.promptMessage());
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ArkLoggerFactory.getDefaultLogger()
                .error("Error occurs in netty telnet server.", cause);
            super.exceptionCaught(ctx, cause);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            if (CHANNEL_QUIT.contains(msg)) {
                ctx.channel().close();
                return;
            }
            ctx.write(arkCommandHandler.responseMessage(msg));
            ctx.flush();
        }
    }
}