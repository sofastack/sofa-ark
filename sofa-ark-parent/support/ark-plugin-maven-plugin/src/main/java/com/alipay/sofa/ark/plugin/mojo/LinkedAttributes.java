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
package com.alipay.sofa.ark.plugin.mojo;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;

/**
 * @author qilong.zql
 * @since 0.1.0
 */
public class LinkedAttributes extends Attributes {

    private LinkedHashMap<Object, Object> linkedHashMap = new LinkedHashMap<>(11);

    @Override
    public Object put(Object name, Object value) {
        linkedHashMap.put(name, value);
        return super.put(name, value);
    }

    @Override
    public Object remove(Object name) {
        linkedHashMap.remove(name);
        return super.remove(name);
    }

    @Override
    public Set<Entry<Object, Object>> entrySet() {
        return linkedHashMap.entrySet();
    }

    public void writeMain(DataOutputStream out) throws IOException {
        // write out the *-Version header first, if it exists
        String vername = Name.MANIFEST_VERSION.toString();
        String version = getValue(vername);
        if (version == null) {
            vername = Name.SIGNATURE_VERSION.toString();
            version = getValue(vername);
        }

        if (version != null) {
            out.writeBytes(vername + ": " + version + "\r\n");
        }

        // write out all attributes except for the version
        // we wrote out earlier
        Iterator it = entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry) it.next();
            String name = e.getKey().toString();
            if ((version != null) && !(name.equalsIgnoreCase(vername))) {

                StringBuffer buffer = new StringBuffer(name);
                buffer.append(": ");

                String value = (String) e.getValue();
                if (value != null) {
                    byte[] vb = value.getBytes("UTF8");
                    value = new String(vb, 0, 0, vb.length);
                }
                buffer.append(value);

                buffer.append("\r\n");
                LinkedManifest.make72Safe(buffer);
                out.writeBytes(buffer.toString());
            }
        }
        out.writeBytes("\r\n");
    }

}