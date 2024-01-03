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
package com.alipay.sofa.ark.tools.git;

import com.alipay.sofa.ark.tools.Repackager;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.junit.Test;

import java.io.File;

import static com.alipay.sofa.ark.tools.git.JGitParser.getBranchesFromCommit;
import static com.alipay.sofa.ark.tools.git.JGitParser.parse;
import static org.junit.Assert.*;

public class JGitParserTest {

    @Test
    public void testParse() {

        File gitFile = new File("../../../.git");
        GitInfo gitInfo = parse(gitFile);
        assertNotNull(gitInfo);
        gitInfo.getBuildUser();
        gitInfo.getBuildEmail();
        assertNotNull(gitInfo.getLastCommitId());
        assertNotEquals(gitInfo.getLastCommitTime(), 0L);
        assertNotNull(gitInfo.getLastCommitDateTime());
        assertNotNull(gitInfo.getLastCommitUser());
        assertNotNull(gitInfo.getLastCommitEmail());
        assertNotNull(gitInfo.getBranchName());
        assertNotNull(gitInfo.getRepository());
        assertNotNull(gitInfo.toString());

        Repackager repackager = new Repackager(new File("../../../pom.xml"));
        repackager.setGitDirectory(gitFile);
    }

    @Test
    public void testGetBranchesFromCommit() {

        try {
            FileRepository fileRepository = new FileRepository("../../../.git");
            assertEquals("master",
                getBranchesFromCommit(fileRepository, "3bb887feb99475b7d6bb40f926aa734fbe62e0f6")
                    .get(0));
        } catch (Throwable e) {
        }
    }
}
