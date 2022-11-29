package com.alipay.sofa.ark.netty;


import com.alipay.sofa.ark.spi.web.EmbeddedServerService;
import reactor.netty.http.server.HttpServer;

public class EmbeddedServerServiceImpl implements EmbeddedServerService<HttpServer> {
    private HttpServer httpServer;
    private Object lock = new Object();

    @Override
    public HttpServer getEmbedServer() {
        return httpServer;
    }

    @Override
    public void setEmbedServer(HttpServer httpServer) {
        if (this.httpServer == null) {
            synchronized (lock) {
                if (this.httpServer == null) {
                    this.httpServer = httpServer;
                }
            }
        }
    }
}