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
package com.alipay.sofa.ark.springboot;

import com.alipay.sofa.ark.springboot.endpoint.IntrospectBizEndpointForSpring1;
import com.alipay.sofa.ark.springboot.processor.ArkEventHandlerProcessor;
import com.alipay.sofa.ark.springboot.processor.ArkServiceInjectProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
@Configuration
public class ArkAutoConfiguration {
    @Bean
    public static ArkServiceInjectProcessor serviceInjectProcessor() {
        return new ArkServiceInjectProcessor();
    }

    @Bean
    public static ArkEventHandlerProcessor arkEventHandlerProcessor() {
        return new ArkEventHandlerProcessor();
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.boot.actuate.endpoint.AbstractEndpoint")
    public static IntrospectBizEndpointForSpring1 introspectBizEndpointForSpring1() {
        return new IntrospectBizEndpointForSpring1();
    }
}