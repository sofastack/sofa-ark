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
package com.alipay.sofa.ark.springboot.web;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.NamingException;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.naming.ContextBindings;

import org.springframework.boot.web.embedded.tomcat.ConnectorStartFailedException;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.util.Assert;

/**
 * NOTE: Tomcat instance will start immediately when create ArkTomcatWebServer object.
 *
 * @author Brian Clozel
 * @author Kristine Jetzke
 * @author 0.6.0
 * @since 2.0.0
 */
public class ArkTomcatWebServer implements WebServer {

    private static final Log                logger            = LogFactory
                                                                  .getLog(TomcatWebServer.class);

    private static final AtomicInteger      containerCounter  = new AtomicInteger(-1);

    private final Object                    monitor           = new Object();

    private final Map<Service, Connector[]> serviceConnectors = new HashMap<>();

    private final Tomcat                    tomcat;

    private final boolean                   autoStart;

    private volatile boolean                started;

    private Thread                          awaitThread;

    private Tomcat                          arkEmbedTomcat;

    /**
     * Create a new {@link ArkTomcatWebServer} instance.
     * @param tomcat the underlying Tomcat server
     */
    public ArkTomcatWebServer(Tomcat tomcat) {
        this(tomcat, true);
    }

    /**
     * Create a new {@link TomcatWebServer} instance.
     * @param tomcat the underlying Tomcat server
     * @param autoStart if the server should be started
     */
    public ArkTomcatWebServer(Tomcat tomcat, boolean autoStart) {
        Assert.notNull(tomcat, "Tomcat Server must not be null");
        this.tomcat = tomcat;
        this.autoStart = autoStart;
        initialize();
    }

    public ArkTomcatWebServer(Tomcat tomcat, boolean autoStart, Tomcat arkEmbedTomcat) {
        this(tomcat, autoStart);
        this.arkEmbedTomcat = arkEmbedTomcat;
    }

    private void initialize() throws WebServerException {
        logger.info("Tomcat initialized with port(s): " + getPortsDescription(false));
        synchronized (this.monitor) {
            try {
                addInstanceIdToEngineName();

                Context context = findContext();
                context.addLifecycleListener((event) -> {
                    if (context.equals(event.getSource())
                            && Lifecycle.START_EVENT.equals(event.getType())) {
                        // Remove service connectors so that protocol binding doesn't
                        // happen when the service is started.
                        removeServiceConnectors();
                    }
                });

                // Start the server to trigger initialization listeners
                this.tomcat.start();

                // We can re-throw failure exception directly in the main thread
                rethrowDeferredStartupExceptions();

                try {
                    ContextBindings.bindClassLoader(context, context.getNamingToken(),
                            Thread.currentThread().getContextClassLoader());
                }
                catch (NamingException ex) {
                    // Naming is not enabled. Continue
                }

                // Unlike Jetty, all Tomcat threads are daemon threads. We create a
                // blocking non-daemon to stop immediate shutdown
                startDaemonAwaitThread();
            }
            catch (Exception ex) {
                stopSilently();
                throw new WebServerException("Unable to start embedded Tomcat", ex);
            }
        }
    }

    private Context findContext() {
        for (Container child : this.tomcat.getHost().findChildren()) {
            if (child instanceof Context) {
                if (child.getParentClassLoader().equals(
                    Thread.currentThread().getContextClassLoader())) {
                    return (Context) child;
                }
            }
        }
        throw new IllegalStateException("The host does not contain a Context");
    }

    private void addInstanceIdToEngineName() {
        int instanceId = containerCounter.incrementAndGet();
        if (instanceId > 0) { // We already have a tomcat container, so just return the existing tomcat.
            Engine engine = this.tomcat.getEngine();
            engine.setName(engine.getName() + "-" + instanceId);
        }
    }

    private void removeServiceConnectors() {
        for (Service service : this.tomcat.getServer().findServices()) {
            Connector[] connectors = service.findConnectors().clone();
            this.serviceConnectors.put(service, connectors);
            for (Connector connector : connectors) {
                service.removeConnector(connector);
            }
        }
    }

    private void rethrowDeferredStartupExceptions() throws Exception {
        Container[] children = this.tomcat.getHost().findChildren();
        for (Container container : children) {
            // just to check current biz status
            if (container.getParentClassLoader() == Thread.currentThread().getContextClassLoader()) {
                if (!LifecycleState.STARTED.equals(container.getState())) {
                    throw new IllegalStateException(container + " failed to start");
                }
            }
        }
    }

    private void startDaemonAwaitThread() {
        awaitThread = new Thread("container-" + (containerCounter.get())) {

            @Override
            public void run() {
                getTomcat().getServer().await();
            }

        };
        awaitThread.setContextClassLoader(Thread.currentThread().getContextClassLoader());
        awaitThread.setDaemon(false);
        awaitThread.start();
    }

    @Override
    public void start() throws WebServerException {
        synchronized (this.monitor) {
            if (this.started) {
                return;
            }

            Context context = findContext();
            try {
                addPreviouslyRemovedConnectors();
                this.tomcat.getConnector();
                checkThatConnectorsHaveStarted();
                this.started = true;
                logger.info("Tomcat started on port(s): " + getPortsDescription(true)
                            + " with context path '" + getContextPath() + "'");
            } catch (ConnectorStartFailedException ex) {
                stopSilently();
                throw ex;
            } catch (Exception ex) {
                throw new WebServerException("Unable to start embedded Tomcat server", ex);
            } finally {
                ContextBindings.unbindClassLoader(context, context.getNamingToken(), getClass()
                    .getClassLoader());
            }
        }
    }

    void checkThatConnectorsHaveStarted() {
        checkConnectorHasStarted(this.tomcat.getConnector());
        for (Connector connector : this.tomcat.getService().findConnectors()) {
            checkConnectorHasStarted(connector);
        }
    }

    private void checkConnectorHasStarted(Connector connector) {
        if (LifecycleState.FAILED.equals(connector.getState())) {
            throw new ConnectorStartFailedException(connector.getPort());
        }
    }

    public void stopSilently() {
        stopContext();
        try {
            stopTomcatIfNecessary();
        } catch (LifecycleException ex) {
            // Ignore
        }
    }

    private void stopContext() {
        Context context = findContext();
        getTomcat().getHost().removeChild(context);
    }

    private void stopTomcatIfNecessary() throws LifecycleException {
        if (tomcat != arkEmbedTomcat) {
            tomcat.destroy();
        }
        awaitThread.stop();
    }

    void addPreviouslyRemovedConnectors() {
        Service[] services = this.tomcat.getServer().findServices();
        for (Service service : services) {
            Connector[] connectors = this.serviceConnectors.get(service);
            if (connectors != null) {
                for (Connector connector : connectors) {
                    service.addConnector(connector);
                    if (!this.autoStart) {
                        stopProtocolHandler(connector);
                    }
                }
                this.serviceConnectors.remove(service);
            }
        }
    }

    private void stopProtocolHandler(Connector connector) {
        try {
            connector.getProtocolHandler().stop();
        } catch (Exception ex) {
            logger.error("Cannot pause connector: ", ex);
        }
    }

    Map<Service, Connector[]> getServiceConnectors() {
        return this.serviceConnectors;
    }

    @Override
    public void stop() throws WebServerException {
        synchronized (this.monitor) {
            boolean wasStarted = this.started;
            try {
                this.started = false;
                try {
                    stopContext();
                    stopTomcatIfNecessary();
                } catch (Throwable ex) {
                    // swallow and continue
                }
            } catch (Exception ex) {
                throw new WebServerException("Unable to stop embedded Tomcat", ex);
            } finally {
                if (wasStarted) {
                    containerCounter.decrementAndGet();
                }
            }
        }
    }

    private String getPortsDescription(boolean localPort) {
        StringBuilder ports = new StringBuilder();
        for (Connector connector : this.tomcat.getService().findConnectors()) {
            if (ports.length() != 0) {
                ports.append(' ');
            }
            int port = localPort ? connector.getLocalPort() : connector.getPort();
            ports.append(port).append(" (").append(connector.getScheme()).append(')');
        }
        return ports.toString();
    }

    @Override
    public int getPort() {
        Connector connector = this.tomcat.getConnector();
        if (connector != null) {
            return connector.getLocalPort();
        }
        return 0;
    }

    private String getContextPath() {
        return findContext().getPath();
    }

    /**
     * Returns access to the underlying Tomcat server.
     * @return the Tomcat server
     */
    public Tomcat getTomcat() {
        return this.tomcat;
    }

}
