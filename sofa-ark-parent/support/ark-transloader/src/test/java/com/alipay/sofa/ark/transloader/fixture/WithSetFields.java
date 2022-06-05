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
package com.alipay.sofa.ark.transloader.fixture;

import com.alipay.sofa.ark.transloader.Triangulate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

// TODO find a way to clone HashSets and TreeSets without serialization so that non-Serializable Objects can be put into them!
/*
 * All elements must be Serializable only because the this$0 field for anonymous classes is final (and
 * pre-Java-5-JVMs prevent setting any final fields, including instance ones, even by reflection). This affects all
 * HashSets and TreeSets because their implementation in the Sun JRE is backed by a Map which has a keySet field
 * which is assigned an anonymous class.
 */
public class WithSetFields extends NonCommonJavaObject {

    private Set set             = new TreeSet(new ToStringComparator());
    {
        set.add(Triangulate.anyInteger());
        set.add(Triangulate.anyString());
        set.add(Triangulate.anyInteger());
    }

    private Set customSet       = new OrderedToStringHashSet();
    {
        customSet.add(Triangulate.anyString());
        customSet.add(Triangulate.anyInteger());
        customSet.add(Triangulate.anyString());
    }

    private Set empty           = Collections.EMPTY_SET;
    private Set unmodifiable    = Collections.unmodifiableSet(set);
    private Set synchronizedSet = Collections.synchronizedSet(set);
    private Set singelton       = Collections.singleton(Triangulate.anyInteger());

    private static class ToStringComparator implements Comparator, Serializable {
        public int compare(Object o1, Object o2) {
            return o1.toString().compareTo(o2.toString());
        }
    }

    private static class OrderedToStringHashSet extends HashSet {
        public String toString() {
            List presentationList = new ArrayList(this);
            Collections.sort(presentationList, new ToStringComparator());
            return presentationList.toString();
        }
    }
}
