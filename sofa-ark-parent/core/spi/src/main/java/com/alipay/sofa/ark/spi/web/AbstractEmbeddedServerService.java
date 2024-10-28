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
package com.alipay.sofa.ark.spi.web;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractEmbeddedServerService<T> implements EmbeddedServerService<T> {
    private Map<Integer, T> servers = new ConcurrentHashMap<>();

    @Override
    public T getEmbedServer(int port) {
        return servers.get(port);
    }

    @Override
    public boolean putEmbedServer(int port, T container) {
        if (container == null) {
            return false;
        }
        return servers.putIfAbsent(port, container) == null;
    }

    @Override
    public Iterator<T> iterator() {
        return servers.values().iterator();
    }
}
