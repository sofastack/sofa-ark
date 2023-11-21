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
package com.alipay.sofa.ark.springboot1.web;

import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.service.ArkInject;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.web.EmbeddedServerService;
import org.apache.catalina.*;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.tomcat.util.scan.StandardJarScanFilter;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerException;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import javax.servlet.ServletContainerInitializer;
import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.alipay.sofa.ark.spi.constant.Constants.ROOT_WEB_CONTEXT_PATH;

/**
 * @author qixiaobo
 * @since 2.2.4
 */
public class ArkTomcatEmbeddedServletContainerFactory extends TomcatEmbeddedServletContainerFactory {
    private static final Charset          DEFAULT_CHARSET = StandardCharsets.UTF_8;

    @ArkInject
    private EmbeddedServerService<Tomcat> embeddedServerService;

    @ArkInject
    private BizManagerService             bizManagerService;

    private File                          baseDirectory;

    private String                        protocol        = DEFAULT_PROTOCOL;

    private int                           backgroundProcessorDelay;

    @Override
    public EmbeddedServletContainer getEmbeddedServletContainer(ServletContextInitializer... initializers) {
        if (embeddedServerService == null) {
            Tomcat tomcat = new Tomcat();
            File baseDir = (this.baseDirectory != null) ? this.baseDirectory
                : createTempDir("tomcat");
            tomcat.setBaseDir(baseDir.getAbsolutePath());
            Connector connector = new Connector(this.protocol);
            tomcat.getService().addConnector(connector);
            customizeConnector(connector);
            tomcat.setConnector(connector);
            tomcat.getHost().setAutoDeploy(false);
            configureEngine(tomcat.getEngine());
            for (Connector additionalConnector : this.getAdditionalTomcatConnectors()) {
                tomcat.getService().addConnector(additionalConnector);
            }
            prepareContext(tomcat.getHost(), initializers);
            return getEmbeddedServletContainer(tomcat);
        } else if (embeddedServerService.getEmbedServer() == null) {
            embeddedServerService.setEmbedServer(initEmbedTomcat());
        }
        Tomcat embedTomcat = embeddedServerService.getEmbedServer();
        prepareContext(embedTomcat.getHost(), initializers);

        return getEmbeddedServletContainer(embedTomcat);
    }

    @Override
    public String getContextPath() {
        String contextPath = super.getContextPath();
        if (bizManagerService == null) {
            return contextPath;
        }
        Biz biz = bizManagerService.getBizByClassLoader(Thread.currentThread()
            .getContextClassLoader());
        if (!StringUtils.isEmpty(contextPath)) {
            return contextPath;
        } else if (biz != null) {
            if (StringUtils.isEmpty(biz.getWebContextPath())) {
                return ROOT_WEB_CONTEXT_PATH;
            }
            return biz.getWebContextPath();
        } else {
            return ROOT_WEB_CONTEXT_PATH;
        }
    }

    private Tomcat initEmbedTomcat() {
        Tomcat tomcat = new Tomcat();
        File baseDir = (this.baseDirectory != null) ? this.baseDirectory : createTempDir("tomcat");
        tomcat.setBaseDir(baseDir.getAbsolutePath());
        Connector connector = new Connector(this.protocol);
        tomcat.getService().addConnector(connector);
        customizeConnector(connector);
        tomcat.setConnector(connector);
        tomcat.getHost().setAutoDeploy(false);
        configureEngine(tomcat.getEngine());
        for (Connector additionalConnector : getAdditionalTomcatConnectors()) {
            tomcat.getService().addConnector(additionalConnector);
        }
        return tomcat;
    }

    @Override
    public void setBaseDirectory(File baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    /**
     * The Tomcat protocol to use when create the {@link Connector}.
     *
     * @param protocol the protocol
     * @see Connector#Connector(String)
     */
    @Override
    public void setProtocol(String protocol) {
        AssertUtils.isFalse(StringUtils.isEmpty(protocol), "Protocol must not be empty");
        this.protocol = protocol;
    }

    @Override
    public void setBackgroundProcessorDelay(int delay) {
        this.backgroundProcessorDelay = delay;
    }

    private void configureEngine(Engine engine) {
        engine.setBackgroundProcessorDelay(this.backgroundProcessorDelay);
        for (Valve valve : getEngineValves()) {
            engine.getPipeline().addValve(valve);
        }
    }

    @Override
    protected void postProcessContext(Context context) {
        ((WebappLoader) context.getLoader())
            .setLoaderClass("com.alipay.sofa.ark.web.embed.tomcat.ArkTomcatEmbeddedWebappClassLoader");
    }

    @Override
    protected void prepareContext(Host host, ServletContextInitializer[] initializers) {
        if (host.getState() == LifecycleState.NEW) {
            super.prepareContext(host, initializers);
        } else {
            File documentRoot = getValidDocumentRoot();
            StandardContext context = new StandardContext();
            if (documentRoot != null) {
                context.setResources(new StandardRoot(context));
            }
            context.setName(getContextPath());
            context.setDisplayName(getDisplayName());
            context.setPath(getContextPath());
            File docBase = (documentRoot != null) ? documentRoot : createTempDir("tomcat-docbase");
            context.setDocBase(docBase.getAbsolutePath());
            context.addLifecycleListener(new Tomcat.FixContextListener());
            context.setParentClassLoader(Thread.currentThread().getContextClassLoader());
            resetDefaultLocaleMapping(context);
            addLocaleMappings(context);
            context.setUseRelativeRedirects(false);
            configureTldSkipPatterns(context);
            WebappLoader loader = new WebappLoader(context.getParentClassLoader());
            loader
                .setLoaderClass("com.alipay.sofa.ark.web.embed.tomcat.ArkTomcatEmbeddedWebappClassLoader");
            loader.setDelegate(true);
            context.setLoader(loader);
            if (isRegisterDefaultServlet()) {
                addDefaultServlet(context);
            }
            if (shouldRegisterJspServlet()) {
                addJspServlet(context);
                addJasperInitializer(context);
            }
            context.addLifecycleListener(new StaticResourceConfigurer(context));
            ServletContextInitializer[] initializersToUse = mergeInitializers(initializers);
            context.setParent(host);
            configureContext(context, initializersToUse);
            host.addChild(context);
        }
    }

    /**
     * Override Tomcat's default locale mappings to align with other servers. See
     * {@code org.apache.catalina.util.CharsetMapperDefault.properties}.
     *
     * @param context the context to reset
     */
    private void resetDefaultLocaleMapping(StandardContext context) {
        context.addLocaleEncodingMappingParameter(Locale.ENGLISH.toString(),
            DEFAULT_CHARSET.displayName());
        context.addLocaleEncodingMappingParameter(Locale.FRENCH.toString(),
            DEFAULT_CHARSET.displayName());
    }

    private void addLocaleMappings(StandardContext context) {
        for (Map.Entry<Locale, Charset> entry : getLocaleCharsetMappings().entrySet()) {
            context.addLocaleEncodingMappingParameter(entry.getKey().toString(), entry.getValue()
                .toString());
        }
    }

    private void configureTldSkipPatterns(StandardContext context) {
        StandardJarScanFilter filter = new StandardJarScanFilter();
        filter.setTldSkip(StringUtils.collectionToCommaDelimitedString(getTldSkipPatterns()));
        context.getJarScanner().setJarScanFilter(filter);
    }

    private void addDefaultServlet(Context context) {
        Wrapper defaultServlet = context.createWrapper();
        defaultServlet.setName("default");
        defaultServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
        defaultServlet.addInitParameter("debug", "0");
        defaultServlet.addInitParameter("listings", "false");
        defaultServlet.setLoadOnStartup(1);
        // Otherwise the default location of a Spring DispatcherServlet cannot be set
        defaultServlet.setOverridable(true);
        context.addChild(defaultServlet);
        context.addServletMappingDecoded("/", "default");
    }

    private void addJspServlet(Context context) {
        Wrapper jspServlet = context.createWrapper();
        jspServlet.setName("jsp");
        jspServlet.setServletClass(getJspServlet().getClassName());
        jspServlet.addInitParameter("fork", "false");
        for (Map.Entry<String, String> initParameter : getJspServlet().getInitParameters()
            .entrySet()) {
            jspServlet.addInitParameter(initParameter.getKey(), initParameter.getValue());
        }
        jspServlet.setLoadOnStartup(3);
        context.addChild(jspServlet);
        addServletMapping(context, "*.jsp", "jsp");
        addServletMapping(context, "*.jspx", "jsp");
    }

    @SuppressWarnings("deprecation")
    private void addServletMapping(Context context, String pattern, String name) {
        context.addServletMapping(pattern, name);
    }

    private void addJasperInitializer(StandardContext context) {
        try {
            ServletContainerInitializer initializer = (ServletContainerInitializer) ClassUtils
                .forName("org.apache.jasper.servlet.JasperInitializer", null).newInstance();
            context.addServletContainerInitializer(initializer, null);
        } catch (Exception ex) {
            // Probably not Tomcat 8
        }
    }

    final class StaticResourceConfigurer implements LifecycleListener {

        private final Context context;

        private StaticResourceConfigurer(Context context) {
            this.context = context;
        }

        @Override
        public void lifecycleEvent(LifecycleEvent event) {
            if (event.getType().equals(Lifecycle.CONFIGURE_START_EVENT)) {
                addResourceJars(getUrlsOfJarsWithMetaInfResources());
            }
        }

        private void addResourceJars(List<URL> resourceJarUrls) {
            for (URL url : resourceJarUrls) {
                String path = url.getPath();
                if (path.endsWith(".jar") || path.endsWith(".jar!/")) {
                    String jar = url.toString();
                    if (!jar.startsWith("jar:")) {
                        // A jar file in the file system. Convert to Jar URL.
                        jar = "jar:" + jar + "!/";
                    }
                    addResourceSet(jar);
                } else {
                    addResourceSet(url.toString());
                }
            }
        }

        private void addResourceSet(String resource) {
            try {
                if (isInsideNestedJar(resource)) {
                    // It's a nested jar but we now don't want the suffix because Tomcat
                    // is going to try and locate it as a root URL (not the resource
                    // inside it)
                    resource = resource.substring(0, resource.length() - 2);
                }
                URL url = new URL(resource);
                String path = "/META-INF/resources";
                this.context.getResources().createWebResourceSet(
                    WebResourceRoot.ResourceSetType.RESOURCE_JAR, "/", url, path);
            } catch (Exception ex) {
                // Ignore (probably not a directory)
            }
        }

        private boolean isInsideNestedJar(String dir) {
            return dir.indexOf("!/") < dir.lastIndexOf("!/");
        }
    }

    @Override
    protected TomcatEmbeddedServletContainer getTomcatEmbeddedServletContainer(Tomcat tomcat) {
        throw new EmbeddedServletContainerException(
            "not support to invoke getTomcatEmbeddedServletContainer", null);
    }

    protected EmbeddedServletContainer getEmbeddedServletContainer(Tomcat tomcat) {
        return new ArkTomcatEmbeddedServletContainer(tomcat, getPort() >= 0, tomcat);
    }

}
