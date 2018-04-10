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
package com.alipay.sofa.ark.sample.springbootdemo;

import com.alipay.sofa.ark.sample.common.SampleClassExported;
import com.alipay.sofa.ark.sample.SampleClassNotExported;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * A sample spring boot web project repackage as ark fat jar.
 *
 * @author qilong.zql
 * @since 0.1.0
 */
@ImportResource({ "classpath*:META-INF/spring/service.xml" })
@SpringBootApplication
public class SpringbootDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootDemoApplication.class, args);
        SampleClassExported.hello();
        SampleClassNotExported.hello();
        // exported resources can be found twice, one from BizClassloader, another from PluginClassloader
        getResources("Sample_Resource_Exported");
        // not-exported resources can only found once from BizClassloader
        getResources("Sample_Resource_Not_Exported");

    }

    public static void getResources(String resourceName) {
        try {
            Enumeration<URL> urls = SpringbootDemoApplication.class.getClassLoader().getResources(
                resourceName);

            while (urls.hasMoreElements()) {
                System.out.println(resourceName + " found: " + urls.nextElement());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}