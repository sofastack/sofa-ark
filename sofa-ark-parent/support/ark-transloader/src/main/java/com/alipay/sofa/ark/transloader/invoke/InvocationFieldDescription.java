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
package com.alipay.sofa.ark.transloader.invoke;

import com.alipay.sofa.ark.transloader.util.Assert;

/**
 * Describes a field by its name, declaring filed name
 *
 * @author hanyue
 * @version : InvocationFieldDescription.java, v 0.1 2022年06月04日 3:22 PM hanyue Exp $
 */
public class InvocationFieldDescription {
    private final String filedName;

    /**
     * Constructs a <code>InvocationFieldDescription</code> with the given declaring <code>Class</code>, field name
     *
     * @param filedName the <code>Field</code> that declares the name
     */
    public InvocationFieldDescription(String filedName) {
        Assert.isNotNull(filedName);
        this.filedName = filedName;
    }

    /**
     * Getter method for property <tt>filedName</tt>.
     *
     * @return property value of filedName
     */
    public String getFiledName() {
        return filedName;
    }
}