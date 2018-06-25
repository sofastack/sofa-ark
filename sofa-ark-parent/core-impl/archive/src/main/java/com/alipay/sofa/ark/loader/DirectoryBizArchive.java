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
package com.alipay.sofa.ark.loader;

import com.alipay.sofa.ark.spi.archive.Archive;
import com.alipay.sofa.ark.spi.archive.BizArchive;
import com.alipay.sofa.ark.spi.constant.Constants;

import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import static com.alipay.sofa.ark.spi.constant.Constants.*;

/**
 * Ark Biz Module Directory
 *
 * @author qilong.zql
 * @since 0.1.0
 */
public class DirectoryBizArchive implements BizArchive {

    public static final String MOCK_IDE_ARK_BIZ_NAME         = "Startup In IDE";
    public static final String MOCK_IDE_ARK_BIZ_VERSION      = "Mock version";
    public static final String MOCK_IDE_ARK_BIZ_MAIN_CLASS   = "Mock Main Class";
    public static final int    MOCK_IDE_BIZ_STARTUP_PRIORITY = 0;

    private final String       className;

    private final String       methodName;

    private final String       methodDescription;

    private final URL[]        urls;

    private final Manifest     manifest                      = new Manifest();

    public DirectoryBizArchive(String className, String methodName, String methodDescription,
                               URL[] urls) {
        this.className = (className == null ? MOCK_IDE_ARK_BIZ_MAIN_CLASS : className);
        this.methodName = methodName;
        this.methodDescription = methodDescription;
        manifest.getMainAttributes().putValue(MAIN_CLASS_ATTRIBUTE, this.className);
        manifest.getMainAttributes().putValue(PRIORITY_ATTRIBUTE,
            String.valueOf(MOCK_IDE_BIZ_STARTUP_PRIORITY));
        manifest.getMainAttributes().putValue(ARK_BIZ_NAME, MOCK_IDE_ARK_BIZ_NAME);
        manifest.getMainAttributes().putValue(ARK_BIZ_VERSION, MOCK_IDE_ARK_BIZ_VERSION);
        this.urls = urls;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodDescription() {
        return methodDescription;
    }

    @Override
    public URL[] getUrls() {
        return this.urls;
    }

    @Override
    public boolean isEntryExist(EntryFilter filter) {
        return filter.matches(new Entry() {
            @Override
            public boolean isDirectory() {
                return false;
            }

            @Override
            public String getName() {
                return Constants.ARK_BIZ_MARK_ENTRY;
            }
        });
    }

    @Override
    public URL getUrl() {
        throw new RuntimeException("unreachable invocation.");
    }

    @Override
    public Manifest getManifest() {
        return manifest;
    }

    @Override
    public List<Archive> getNestedArchives(EntryFilter filter) {
        throw new RuntimeException("unreachable invocation.");
    }

    @Override
    public Archive getNestedArchive(Entry entry) {
        if (Constants.ARK_BIZ_MARK_ENTRY.equals(entry.getName())) {
            return new NoopBizArchive();
        }
        throw new RuntimeException("unreachable invocation.");
    }

    @Override
    public InputStream getInputStream(ZipEntry zipEntry) {
        throw new RuntimeException("unreachable invocation.");
    }

    @Override
    public Iterator<Entry> iterator() {
        throw new RuntimeException("unreachable invocation.");
    }

    class NoopBizArchive extends JarBizArchive {
        public NoopBizArchive() {
            super(null);
        }
    }
}