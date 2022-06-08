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
import com.alipay.sofa.ark.transloader.clone.CloningStrategy;
import com.alipay.sofa.ark.transloader.clone.reflect.CyclicReferenceSafeTraverser;
import com.alipay.sofa.ark.transloader.clone.reflect.CyclicReferenceSafeTraverser.Traversal;
import com.alipay.sofa.ark.transloader.clone.reflect.FieldDescription;
import com.alipay.sofa.ark.transloader.clone.reflect.FieldReflector;
import com.alipay.sofa.ark.transloader.fixture.NonCommonJavaType;
import junit.extensions.ActiveTestSuite;
import junit.framework.Test;

import java.util.Map;

// TODO minimal clones of Sets and Maps can be compared by Strings but maximal clones cannot without NullPointerExceptions, so find out why
public class MaximalCloningTest extends CloningTestCase {
    private static CyclicReferenceSafeTraverser CYCLIC_REFERENCE_TRAVERSER = new CyclicReferenceSafeTraverser();

    public static Test suite() throws Exception {
        return new ActiveTestSuite(MaximalCloningTest.class);
    }

    protected Object assertDeeplyClonedToOtherClassLoader(NonCommonJavaType original)
            throws Exception {
        Object clone = super.assertDeeplyClonedToOtherClassLoader(original);
        assertDeeplyNotTheSame(original, clone);
        return clone;
    }

    private void assertDeeplyNotTheSame(final Object original, final Object clone) throws Exception {
        // TODO there's a pattern here and elsewhere of safely traversing fields, so abstract it if possible
        Traversal notSameTraversal = new Traversal() {
            public Object traverse(Object currentObjectInGraph, Map referenceHistory)
                    throws Exception {
                assertNotSame(original, clone);
                FieldReflector originalReflector = new FieldReflector(original);
                FieldReflector cloneReflector = new FieldReflector(clone);
                FieldDescription[] fieldDescriptions = originalReflector
                        .getAllInstanceFieldDescriptions();
                for (int i = 0; i < fieldDescriptions.length; i++) {
                    FieldDescription description = fieldDescriptions[i];
                    if (!description.isPrimitive()) {
                        assertDeeplyNotTheSame(originalReflector.getValue(description),
                                cloneReflector.getValue(description));
                    }
                }
                return null;
            }
        };
        CYCLIC_REFERENCE_TRAVERSER.performWithoutFollowingCircles(notSameTraversal, original);
    }

    protected Transloader getTransloader() {
        return new DefaultTransloader(CloningStrategy.MAXIMAL);
    }
}
