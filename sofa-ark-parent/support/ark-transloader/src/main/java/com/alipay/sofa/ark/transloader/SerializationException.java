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
package com.alipay.sofa.ark.transloader;

import com.alipay.sofa.ark.transloader.util.Assert;

public class SerializationException extends RuntimeException {

    /**
     * Required for serialization support.
     *
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 4029025366392702726L;

    /**
     * <p>Constructs a new {@code SerializationException} without specified
     * detail message.</p>
     */
    public SerializationException() {
        super();
    }

    /**
     * <p>Constructs a new {@code SerializationException} with specified
     * detail message.</p>
     *
     * @param msg The error message.
     */
    public SerializationException(final String msg) {
        super((String) Assert.isNotNull(msg));
    }

    /**
     * <p>Constructs a new {@code SerializationException} with specified
     * nested {@code Throwable}.</p>
     *
     * @param cause The {@code Exception} or {@code Error}
     *              that caused this exception to be thrown.
     */
    public SerializationException(final Throwable cause) {
        super((Throwable) Assert.isNotNull(cause));
    }

    /**
     * <p>Constructs a new {@code SerializationException} with specified
     * detail message and nested {@code Throwable}.</p>
     *
     * @param msg   The error message.
     * @param cause The {@code Exception} or {@code Error}
     *              that caused this exception to be thrown.
     */
    public SerializationException(final String msg, final Throwable cause) {
        super((String) Assert.isNotNull(msg), (Throwable) Assert.isNotNull(cause));
    }

}