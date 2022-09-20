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
package com.alipay.sofa.ark.tools.test;

import com.alipay.sofa.ark.tools.Repackager;
import com.alipay.sofa.ark.tools.git.GitInfo;
import com.alipay.sofa.ark.tools.git.JGitParser;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class JGitParserTest {

    @Test
    public void testParse() {
        File gitFile = new File("../../../.git");
        GitInfo gitInfo = JGitParser.parse(gitFile);
        Assert.assertNotNull(gitInfo);
        Assert.assertNotNull(gitInfo.getBuildUser());
        Assert.assertNotNull(gitInfo.getBuildEmail());
        Assert.assertNotNull(gitInfo.getLastCommitId());
        Assert.assertNotEquals(gitInfo.getLastCommitTime(), 0L);
        Assert.assertNotNull(gitInfo.getLastCommitDateTime());
        Assert.assertNotNull(gitInfo.getLastCommitUser());
        Assert.assertNotNull(gitInfo.getLastCommitEmail());
        Assert.assertNotNull(gitInfo.getBranchName());
        Assert.assertNotNull(gitInfo.getRepository());
        Assert.assertNotNull(gitInfo.toString());

        Repackager repackager = new Repackager(new File("../../../pom.xml"));
        repackager.setGitDirectory(gitFile);

    }
}
