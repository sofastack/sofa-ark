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
package com.alipay.sofa.ark.transloader.function;

import com.alipay.sofa.ark.transloader.DefaultTransloader;
import com.alipay.sofa.ark.transloader.Transloader;
import com.alipay.sofa.ark.transloader.Triangulate;
import com.alipay.sofa.ark.transloader.clone.CloningStrategy;
import com.alipay.sofa.ark.transloader.fixture.IndependentClassLoader;
import com.alipay.sofa.ark.transloader.fixture.WithMapFields;
import com.alipay.sofa.ark.transloader.fixture.WithSetFields;
import junit.extensions.ActiveTestSuite;
import junit.framework.Test;

public class MinimalCloningTest extends CloningTestCase {
    public static Test suite() throws Exception {
        return new ActiveTestSuite(MinimalCloningTest.class);
    }

    public void testDoesNotCloneStrings() throws Exception {
        Object string = Triangulate.anyString();
        assertSame(string,
            getTransloader().wrap(string).cloneWith(IndependentClassLoader.getInstance()));
    }

    public void testClonesObjectsWithSetFields() throws Exception {
        assertDeeplyClonedToOtherClassLoader(new WithSetFields());
    }

    public void testClonesObjectsWithMapFields() throws Exception {
        assertDeeplyClonedToOtherClassLoader(new WithMapFields());
    }

    protected Transloader getTransloader() {
        return new DefaultTransloader(CloningStrategy.MINIMAL);
    }
}
