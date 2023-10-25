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
package com.alipay.sofa.ark.container.service.retrieval;

import com.taobao.text.Decoration;
import com.taobao.text.ui.Element;
import com.taobao.text.ui.TableElement;
import com.taobao.text.ui.TreeElement;
import com.taobao.text.util.RenderUtil;

/**
 * Render Telnet Panel
 *
 * @author yanzhu
 * @since 2.2.4
 */
public class ViewRender {

    /**
     * render class information
     *
     * @param clazz
     * @return
     */
    public static String renderClassInfo(ClassInfoVO clazz) {
        TableElement table = new TableElement().leftCellPadding(1).rightCellPadding(1);
        table
            .row(Element.label("class-info").style(Decoration.bold.bold()),
                Element.label(clazz.getClassInfo()))
            .row(Element.label("code-source").style(Decoration.bold.bold()),
                Element.label(clazz.getCodeSource()))
            .row(Element.label("isInterface").style(Decoration.bold.bold()),
                Element.label("" + clazz.isInterface()))
            .row(Element.label("isAnnotation").style(Decoration.bold.bold()),
                Element.label("" + clazz.isAnnotation()))
            .row(Element.label("isEnum").style(Decoration.bold.bold()),
                Element.label("" + clazz.isEnum()))
            .row(Element.label("container-name").style(Decoration.bold.bold()),
                Element.label("" + clazz.getContainerName()))
            .row(Element.label("simple-name").style(Decoration.bold.bold()),
                Element.label(clazz.getSimpleName()))
            .row(Element.label("modifier").style(Decoration.bold.bold()),
                Element.label(clazz.getModifier()))
            .row(Element.label("super-class").style(Decoration.bold.bold()), drawSuperClass(clazz))
            .row(Element.label("class-loader").style(Decoration.bold.bold()),
                drawClassLoader(clazz.getClassloader()));
        return RenderUtil.render(table);
    }

    private static Element drawSuperClass(ClassInfoVO clazz) {
        return drawTree(clazz.getSuperClass());
    }

    private static Element drawClassLoader(String[] classloaders) {
        return drawTree(classloaders);
    }

    private static Element drawTree(String[] nodes) {
        TreeElement root = new TreeElement();
        TreeElement parent = root;
        for (String node : nodes) {
            TreeElement child = new TreeElement(Element.label(node));
            parent.addChild(child);
            parent = child;
        }
        return root;
    }
}
