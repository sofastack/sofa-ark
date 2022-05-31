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
package com.alipay.sofa.ark.springboot.condition;

import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class OnSpringBootVersion extends SpringBootCondition {
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> springBootVersion = metadata
            .getAnnotationAttributes(ConditionalOnSpringBootVersion.class.getCanonicalName());
        if (springBootVersion == null || springBootVersion.get("version") == null) {
            return new ConditionOutcome(false, "No specified spring boot version.");
        }
        ConditionalOnSpringBootVersion.Version version = (ConditionalOnSpringBootVersion.Version) springBootVersion
            .get("version");
        if (ConditionalOnSpringBootVersion.Version.ANY.equals(version)) {
            return new ConditionOutcome(true, "Conditional on Any Spring Boot.");
        } else if (ConditionalOnSpringBootVersion.Version.OneX.equals(version)) {
            String bootVersion = SpringBootVersion.getVersion();
            if (null != bootVersion && bootVersion.startsWith("1")) {
                return new ConditionOutcome(true, "Conditional on OneX Spring Boot.");
            } else {
                return new ConditionOutcome(false, "Conditional on OneX Spring Boot.");
            }
        } else if (ConditionalOnSpringBootVersion.Version.TwoX.equals(version)) {
            String bootVersion = SpringBootVersion.getVersion();
            if (null != bootVersion && bootVersion.startsWith("2")) {
                return new ConditionOutcome(true, "Conditional on TwoX Spring Boot.");
            } else {
                return new ConditionOutcome(false, "Conditional on TwoX Spring Boot.");
            }
        }
        throw new IllegalStateException("Error Spring Boot Version.");
    }
}