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

import com.alipay.sofa.ark.exception.ArkRuntimeException;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: yuanyuan
 */
public class ArkCompositeReactorHttpHandlerAdapter extends ReactorHttpHandlerAdapter {

    private Map<String, ReactorHttpHandlerAdapter> bizReactorHttpHandlerAdapters = new ConcurrentHashMap<>();

    public ArkCompositeReactorHttpHandlerAdapter(HttpHandler httpHandler) {
        super(httpHandler);
    }

    public void registerBizReactorHttpHandlerAdapter(String contextPath,
                                                     ReactorHttpHandlerAdapter reactorHttpHandlerAdapter) {
        ReactorHttpHandlerAdapter old = bizReactorHttpHandlerAdapters.putIfAbsent(contextPath,
            reactorHttpHandlerAdapter);
        if (old != null) {
            throw new ArkRuntimeException("Duplicated context path");
        }
    }

    public void unregisterBizReactorHttpHandlerAdapter(String contextPath) {
        bizReactorHttpHandlerAdapters.remove(contextPath);
    }

    @Override
    public Mono<Void> apply(HttpServerRequest reactorRequest, HttpServerResponse reactorResponse) {
        String uri = reactorRequest.uri();
        for (Map.Entry<String, ReactorHttpHandlerAdapter> entry : bizReactorHttpHandlerAdapters
            .entrySet()) {
            if (uri.startsWith(entry.getKey())) {
                ReactorHttpHandlerAdapter adapter = entry.getValue();
                return adapter.apply(reactorRequest, reactorResponse);
            }
        }
        return super.apply(reactorRequest, reactorResponse);
    }

    //    private static String resolveRequestUri(HttpServerRequest request) {
    //        String uri = request.uri();
    //        for (int i = 0; i < uri.length(); i++) {
    //            char c = uri.charAt(i);
    //            if (c == '/' || c == '?' || c == '#') {
    //                break;
    //            }
    //            if (c == ':' && (i + 2 < uri.length())) {
    //                if (uri.charAt(i + 1) == '/' && uri.charAt(i + 2) == '/') {
    //                    for (int j = i + 3; j < uri.length(); j++) {
    //                        c = uri.charAt(j);
    //                        if (c == '/' || c == '?' || c == '#') {
    //                            return uri.substring(j);
    //                        }
    //                    }
    //                    return "";
    //                }
    //            }
    //        }
    //        return uri;
    //    }
}
