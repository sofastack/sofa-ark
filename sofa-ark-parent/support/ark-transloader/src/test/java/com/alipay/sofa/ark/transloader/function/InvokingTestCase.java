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
import com.alipay.sofa.ark.transloader.fixture.WithMapFields;
import com.alipay.sofa.ark.transloader.fixture.WithNonCommonJavaFields;
import com.alipay.sofa.ark.transloader.fixture.WithPrimitiveFields;
import com.alipay.sofa.ark.transloader.fixture.WithSetFields;
import com.alipay.sofa.ark.transloader.fixture.WithStaticFinalFields;
import com.alipay.sofa.ark.transloader.fixture.WithStringField;
import com.alipay.sofa.ark.transloader.invoke.InvocationFieldDescription;
import com.alipay.sofa.ark.transloader.invoke.InvocationMethodDescription;
import org.junit.Assert;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author hanyue
 * @version : InvokingTestCase.java, v 0.1 2022年06月04日 8:12 PM hanyue Exp $
 */
public abstract class InvokingTestCase extends BaseTestCase {

    private final PodamFactory factory = new PodamFactoryImpl();

    protected Object assertDeeplyClonedToOtherClassLoader(NonCommonJavaType original)
                                                                                     throws Exception {
        String originalString = original.toString();
        Object clone = getTransloader().wrap(original).cloneWith(
            IndependentClassLoader.getInstance());
        assertNotSame(original, clone);
        assertEqualExceptForClassLoader(originalString, clone);
        return clone;
    }

    protected void assertInvokeToOtherClassLoader(Object original, Object clone) {
        assertInvokeFieldToOtherClassLoader(original, clone);
        assertInvokeMethodToOtherClassLoader(original, clone);
    }

    protected void assertInvokeFieldToOtherClassLoader(Object original, Object clone) {
        for (Field field : clone.getClass().getDeclaredFields()) {
            InvocationFieldDescription fieldDescription = new InvocationFieldDescription(
                field.getName());
            Object target = getTransloader().wrap(clone).invoke(fieldDescription);
            Object source = getTransloader().wrap(original).invoke(fieldDescription);

            assertInvokeResult(source, target);
        }
    }

    protected void assertInvokeMethodToOtherClassLoader(Object original, Object clone) {
        for (Method method : clone.getClass().getDeclaredMethods()) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            Object[] parameters = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                parameters[i] = factory.manufacturePojo(parameterTypes[i]);
            }
            InvocationMethodDescription methodDescription = new InvocationMethodDescription(method,
                parameters);

            Object target = getTransloader().wrap(clone).invoke(methodDescription);
            Object source = getTransloader().wrap(original).invoke(methodDescription);

            assertInvokeResult(source, target);
        }
    }

    private void assertInvokeResult(Object source, Object target) {
        ClassLoader targetClassLoader = target.getClass().getClassLoader();
        ClassLoader sourceClassLoader = source.getClass().getClassLoader();

        if (targetClassLoader == null) {
            Assert.assertNull(sourceClassLoader);
        } else if (source.toString().contains(targetClassLoader.toString())) {
            assertEqualExceptForClassLoader(source.toString(), target);
        }
        if (!source.getClass().isPrimitive()) {
            assertNotSame(target, source);
        } else {
            assertEquals(target, source);
        }
    }

    protected abstract Transloader getTransloader();

    public void testClonesObjectsWithPrimitiveFields() throws Exception {
        WithPrimitiveFields withPrimitiveFields = new WithPrimitiveFields();
        Object clone = assertDeeplyClonedToOtherClassLoader(withPrimitiveFields);
        assertInvokeToOtherClassLoader(withPrimitiveFields, clone);
    }

    public void testClonesObjectsNotOfCommonJavaTypes() throws Exception {
        NonCommonJavaObject nonCommonJavaObject = new NonCommonJavaObject();
        Object clone = assertDeeplyClonedToOtherClassLoader(nonCommonJavaObject);
        assertInvokeToOtherClassLoader(nonCommonJavaObject, clone);
    }

    public void testClonesObjectsWithFieldsOfCommonJavaTypes() throws Exception {
        WithStringField withStringField = new WithStringField(Triangulate.anyString());
        Object clone = assertDeeplyClonedToOtherClassLoader(withStringField);
        assertInvokeToOtherClassLoader(withStringField, clone);
    }

    public void testClonesObjectsWithFieldsNotOfCommonJavaTypes() throws Exception {
        WithNonCommonJavaFields withNonCommonJavaFields = new WithNonCommonJavaFields(
            new WithStringField(Triangulate.anyString()));
        Object clone = assertDeeplyClonedToOtherClassLoader(withNonCommonJavaFields);
        assertInvokeToOtherClassLoader(withNonCommonJavaFields, clone);
    }

    public void testClonesObjectsWithArrayFields() throws Exception {
        WithArrayFields withArrayFields = new WithArrayFields();
        Object clone = assertDeeplyClonedToOtherClassLoader(withArrayFields);
        assertInvokeToOtherClassLoader(withArrayFields, clone);
    }

    public void testClonesFieldsThroughoutHierarchies() throws Exception {
        HiearchyWithFieldsBottom hiearchyWithFieldsBottom = new HiearchyWithFieldsBottom(
            new NonCommonJavaObject(), Triangulate.anyInt(), Triangulate.anyString(),
            Triangulate.eitherBoolean());
        Object clone = assertDeeplyClonedToOtherClassLoader(hiearchyWithFieldsBottom);
        assertInvokeToOtherClassLoader(hiearchyWithFieldsBottom, clone);
    }

    public void testClonesObjectsOfSerializableAnonymousClasses() throws Exception {
        SerializableWithAnonymousClassFields serializableWithAnonymousClassFields = new SerializableWithAnonymousClassFields(
            Triangulate.anyInteger());
        Object clone = assertDeeplyClonedToOtherClassLoader(serializableWithAnonymousClassFields);
        assertInvokeToOtherClassLoader(serializableWithAnonymousClassFields, clone);
    }

    public void testClonesObjectsWithListFields() throws Exception {
        WithListFields withListFields = new WithListFields();
        Object clone = assertDeeplyClonedToOtherClassLoader(withListFields);
        assertInvokeToOtherClassLoader(withListFields, clone);
    }

    public void testClonesObjectsWithFinalFields() throws Exception {
        WithFinalFields withFinalFields = new WithFinalFields(Triangulate.anyString());
        Object clone = assertDeeplyClonedToOtherClassLoader(withFinalFields);
        assertInvokeToOtherClassLoader(withFinalFields, clone);
    }

    public void testClonesObjectsWithSetFields() throws Exception {
        WithSetFields withSetFields = new WithSetFields();
        Object clone = getTransloader().wrap(withSetFields).cloneWith(
            IndependentClassLoader.getInstance());
        assertNotSame(withSetFields, clone);
        assertInvokeToOtherClassLoader(withSetFields, clone);
    }

    public void testClonesObjectsWithMapFields() throws Exception {
        WithMapFields withMapFields = new WithMapFields();
        Object clone = getTransloader().wrap(withMapFields).cloneWith(
            IndependentClassLoader.getInstance());
        assertNotSame(withMapFields, clone);
        assertInvokeToOtherClassLoader(withMapFields, clone);
    }

    public void testClonesObjectsWithStaticFinalFields() throws Exception {
        WithStaticFinalFields withStaticFinalFields = new WithStaticFinalFields();
        Object clone = assertDeeplyClonedToOtherClassLoader(withStaticFinalFields);
        assertInvokeToOtherClassLoader(withStaticFinalFields, clone);

        Class<?> aClass = IndependentClassLoader.getInstance().loadClass(
            WithStaticFinalFields.class.getName());
        for (Field field : aClass.getDeclaredFields()) {
            InvocationFieldDescription invocationFieldDescription = new InvocationFieldDescription(
                field.getName());

            Object source = getTransloader().wrap(WithStaticFinalFields.class).invoke(
                invocationFieldDescription);
            Object target = getTransloader().wrap(aClass).invoke(invocationFieldDescription);
            assertInvokeResult(source, target);
        }

        for (Method method : clone.getClass().getDeclaredMethods()) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            Object[] parameters = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                parameters[i] = factory.manufacturePojo(parameterTypes[i]);
            }
            InvocationMethodDescription methodDescription = new InvocationMethodDescription(method,
                parameters);

            Object target = getTransloader().wrap(WithStaticFinalFields.class).invoke(
                methodDescription);
            Object source = getTransloader().wrap(aClass).invoke(methodDescription);

            assertInvokeResult(source, target);
        }
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
        SelfAndParentReferencingChild selfAndParentReferencingChild = new SelfAndParentReferencingChild(
            Triangulate.anyString(), new SelfAndChildReferencingParent(Triangulate.anyString()));
        Object clone = assertDeeplyClonedToOtherClassLoader(selfAndParentReferencingChild);
        assertInvokeToOtherClassLoader(selfAndParentReferencingChild, clone);
    }
}