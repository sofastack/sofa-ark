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

import com.alipay.sofa.ark.springboot.condition.ConditionalOnArkEnabled;
import com.alipay.sofa.ark.springboot.processor.ArkEventHandlerProcessor;
import com.alipay.sofa.ark.springboot.processor.ArkServiceInjectProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author qixiaobo
 * @since 2.2.4
 */

@Configuration
@ConditionalOnArkEnabled
@AutoConfigureBefore(name = {
                             "org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration",
                             "org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration" })
public class ArkAutoProcessorConfiguration {
    @Bean
    public static ArkServiceInjectProcessor serviceInjectProcessor() {
        return new ArkServiceInjectProcessor();
    }

    @Bean
    public static ArkEventHandlerProcessor arkEventHandlerProcessor() {
        return new ArkEventHandlerProcessor();
    }
}
