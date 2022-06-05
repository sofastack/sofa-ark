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
package com.alipay.sofa.ark.transloader.clone.reflect;

import com.alipay.sofa.ark.transloader.util.Assert;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * A utility for traversing through entire object graphs which may contain cyclic references, without thereby entering
 * an endless loop. The implementation is thread-safe.
 *
 * @author hanyue
 * @version : CyclicReferenceSafeTraverser.java, v 0.1 2022年06月04日 9:44 AM hanyue Exp $
 */
public class CyclicReferenceSafeTraverser {
    private final ThreadLocal referenceHistoryForThread = new ThreadLocal();

    /**
     * Executes the given the traversal over the current location in the object graph if it has not already been
     * traversed in the current journey through the graph.
     *
     * @param traversal the traversal to execute
     * @param currentObjectInGraph the location in the object graph over which to perform the traversal
     * @return the result of performing the traversal
     * @throws Exception can throw any <code>Exception</code> from the traversal itself
     */
    public Object performWithoutFollowingCircles(Traversal traversal, Object currentObjectInGraph)
                                                                                                  throws Exception {
        Assert.areNotNull(traversal, currentObjectInGraph);
        Map referenceHistory = getReferenceHistory();
        if (referenceHistory.containsKey(currentObjectInGraph)) {
            return referenceHistory.get(currentObjectInGraph);
        }
        try {
            referenceHistory.put(currentObjectInGraph, null);
            Object result = traversal.traverse(currentObjectInGraph, referenceHistory);
            return result;
        } finally {
            referenceHistory.remove(currentObjectInGraph);
        }
    }

    private Map getReferenceHistory() {
        Map referenceHistory = (Map) referenceHistoryForThread.get();
        if (referenceHistory == null) {
            referenceHistoryForThread.set(referenceHistory = new IdentityHashMap());
        }
        return referenceHistory;
    }

    /**
     * The callback interface for implementing a traversal over an object graph.
     *
     * @author Jeremy Wales
     */
    public interface Traversal {
        /**
         * Performs some logic on the given current location in the object graph. May update the given reference history
         * to associate the given current object with the result of traversing it so that the result can be reused next
         * time the same object is encountered in the same journey over the graph.
         *
         * @param currentObjectInGraph the location in the object graph over which to perform the traversal
         * @param referenceHistory     the history of objects already traversed and the results of traversing them
         * @return the result of traversing <code>currentObjectInGraph</code>
         * @throws Exception can throw any <code>Exception</code> depending on the implementation
         */
        Object traverse(Object currentObjectInGraph, Map referenceHistory) throws Exception;
    }
}