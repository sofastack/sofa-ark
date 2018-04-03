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
package com.alipay.sofa.ark.support.thread;

/**
 * Isolated {@link ThreadGroup} to capture uncaught exceptions.
 *
 * @author qilong.zql
 * @since 0.1.0
 */
public class IsolatedThreadGroup extends ThreadGroup {

    protected final Object monitor = new Object();

    protected Throwable    exception;

    public IsolatedThreadGroup(String name) {
        super(name);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!(ex instanceof ThreadDeath)) {
            synchronized (this.monitor) {
                this.exception = (this.exception == null ? ex : this.exception);
            }
        }
    }

    public synchronized void rethrowUncaughtException() {
        synchronized (this.monitor) {
            if (this.exception != null) {
                throw new RuntimeException("An exception occurred while running. "
                                           + this.exception.getMessage(), this.exception);
            }
        }
    }
}