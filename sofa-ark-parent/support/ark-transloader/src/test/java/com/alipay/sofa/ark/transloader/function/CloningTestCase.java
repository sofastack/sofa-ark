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

import com.alipay.sofa.ark.transloader.BaseTestCase;
import com.alipay.sofa.ark.transloader.Transloader;
import com.alipay.sofa.ark.transloader.Triangulate;
import com.alipay.sofa.ark.transloader.fixture.HiearchyWithFieldsBottom;
import com.alipay.sofa.ark.transloader.fixture.IndependentClassLoader;
import com.alipay.sofa.ark.transloader.fixture.NonCommonJavaObject;
import com.alipay.sofa.ark.transloader.fixture.NonCommonJavaType;
import com.alipay.sofa.ark.transloader.fixture.SelfAndChildReferencingParent;
import com.alipay.sofa.ark.transloader.fixture.SelfAndParentReferencingChild;
import com.alipay.sofa.ark.transloader.fixture.SerializableWithAnonymousClassFields;
import com.alipay.sofa.ark.transloader.fixture.WithArrayFields;
import com.alipay.sofa.ark.transloader.fixture.WithFinalFields;
import com.alipay.sofa.ark.transloader.fixture.WithListFields;
import com.alipay.sofa.ark.transloader.fixture.WithNonCommonJavaFields;
import com.alipay.sofa.ark.transloader.fixture.WithPrimitiveFields;
import com.alipay.sofa.ark.transloader.fixture.WithStaticFinalFields;
import com.alipay.sofa.ark.transloader.fixture.WithStringField;

public abstract class CloningTestCase extends BaseTestCase {

    protected Object assertDeeplyClonedToOtherClassLoader(NonCommonJavaType original)
                                                                                     throws Exception {
        String originalString = original.toString();
        Object clone = getTransloader().wrap(original).cloneWith(
            IndependentClassLoader.getInstance());
        assertNotSame(original, clone);
        assertEqualExceptForClassLoader(originalString, clone);
        return clone;
    }

    protected abstract Transloader getTransloader();

    public void testClonesObjectsWithPrimitiveFields() throws Exception {
        assertDeeplyClonedToOtherClassLoader(new WithPrimitiveFields());
    }

    public void testClonesObjectsNotOfCommonJavaTypes() throws Exception {
        assertDeeplyClonedToOtherClassLoader(new NonCommonJavaObject());
    }

    public void testClonesObjectsWithFieldsOfCommonJavaTypes() throws Exception {
        assertDeeplyClonedToOtherClassLoader(new WithStringField(Triangulate.anyString()));
    }

    public void testClonesObjectsWithFieldsNotOfCommonJavaTypes() throws Exception {
        assertDeeplyClonedToOtherClassLoader(new WithNonCommonJavaFields(new WithStringField(
            Triangulate.anyString())));
    }

    public void testClonesObjectsWithArrayFields() throws Exception {
        assertDeeplyClonedToOtherClassLoader(new WithArrayFields());
    }

    public void testClonesFieldsThroughoutHierarchies() throws Exception {
        assertDeeplyClonedToOtherClassLoader(new HiearchyWithFieldsBottom(
            new NonCommonJavaObject(), Triangulate.anyInt(), Triangulate.anyString(),
            Triangulate.eitherBoolean()));
    }

    public void testClonesObjectsOfSerializableAnonymousClasses() throws Exception {
        assertDeeplyClonedToOtherClassLoader(new SerializableWithAnonymousClassFields(
            Triangulate.anyInteger()));
    }

    public void testClonesObjectsWithListFields() throws Exception {
        assertDeeplyClonedToOtherClassLoader(new WithListFields());
    }

    public void testClonesObjectsWithFinalFields() throws Exception {
        assertDeeplyClonedToOtherClassLoader(new WithFinalFields(Triangulate.anyString()));
    }

    public void testClonesObjectsWithStaticFinalFields() throws Exception {
        assertDeeplyClonedToOtherClassLoader(new WithStaticFinalFields());
    }

    public void testClonesAllFieldsWithCircularReferences() throws Exception {
        cloneWithCircularReferences();
    }

    public void testClonesAllFieldsWithCircularReferencesConcurrently() throws Exception {
        cloneWithCircularReferences();
    }

    public void testClonesAllFieldsWithCircularReferencesYetMoreConcurrently() throws Exception {
        cloneWithCircularReferences();
    }

    private void cloneWithCircularReferences() throws Exception {
        assertDeeplyClonedToOtherClassLoader(new SelfAndParentReferencingChild(
            Triangulate.anyString(), new SelfAndChildReferencingParent(Triangulate.anyString())));
    }
}