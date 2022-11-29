import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.netty.EmbeddedServerServiceImpl;
import com.alipay.sofa.ark.spi.model.PluginContext;
import com.alipay.sofa.ark.spi.service.PluginActivator;

import com.alipay.sofa.ark.spi.web.EmbeddedServerService;
import reactor.netty.http.server.HttpServer;
public class NettyPluginActivator implements PluginActivator {

    private EmbeddedServerService embeddedNettyService = new EmbeddedServerServiceImpl();

    @Override
    public void start(PluginContext context) {
        context.publishService(EmbeddedServerService.class, embeddedNettyService);
    }

    @Override
    public void stop(PluginContext context) {
        HttpServer webServer = null;
        if (embeddedNettyService.getEmbedServer() instanceof HttpServer) {
            webServer = (HttpServer) embeddedNettyService.getEmbedServer();
        }
        if (webServer != null) {
            try {
               //webServer.stop();
            } catch (Exception ex) {
                ArkLoggerFactory.getDefaultLogger().error("Unable to stop embedded Netty", ex);
            }
        }
    }
}