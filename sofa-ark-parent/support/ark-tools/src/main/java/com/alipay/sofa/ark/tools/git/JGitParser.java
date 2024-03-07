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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.alipay.sofa.ark.spi.constant.Constants.DATE_FORMAT;
import static org.eclipse.jgit.lib.Constants.MASTER;

public class JGitParser {

    public static GitInfo parse(File gitDirectory) {
        try (FileRepository repository = getGitRepository(gitDirectory)) {

            GitInfo gitInfo = new GitInfo();

            FileBasedConfig config = repository.getConfig();
            String remoteUrl = config.getString("remote", "origin", "url");
            String userName = config.getString("user", null, "name");
            String userEmail = config.getString("user", null, "email");
            String branchName = repository.getBranch();
            gitInfo.setRepository(remoteUrl);
            gitInfo.setBuildUser(userName);
            gitInfo.setBuildEmail(userEmail);
            gitInfo.setBranchName(branchName);

            RevCommit lastCommit = getLastCommit(repository);
            if (lastCommit != null) {
                String lastCommitId = lastCommit.getId().getName();
                long lastCommitTime = lastCommit.getCommitterIdent().getWhen().getTime();
                String lastCommitUser = lastCommit.getAuthorIdent().getName();
                String lastCommitEmail = lastCommit.getAuthorIdent().getEmailAddress();
                String commitDateTime = new SimpleDateFormat(DATE_FORMAT).format(lastCommitTime);

                gitInfo.setLastCommitId(lastCommitId);
                gitInfo.setLastCommitUser(lastCommitUser);
                gitInfo.setLastCommitEmail(lastCommitEmail);
                gitInfo.setLastCommitTime(lastCommitTime);
                gitInfo.setLastCommitDateTime(commitDateTime);

                if (lastCommitId.equals(branchName)) {
                    gitInfo.setBranchName(StringUtils.join(
                            getBranchesFromCommit(repository, lastCommitId), ","));
                }
            }

            return gitInfo;
        } catch (Exception exception) {
            return null;
        }
    }

    static List<String> getBranchesFromCommit(FileRepository repository, String lastCommitId) throws GitAPIException {

        List<Ref> refs = Git.wrap(repository).branchList()
                .setListMode(ListBranchCommand.ListMode.REMOTE)
                .setContains(lastCommitId)
                .call();
        return refs.stream()
                .filter(ref -> !ref.isSymbolic())
                .map(Ref::getName)
                .map(repository::shortenRemoteBranchName)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .sorted(MASTER_FIRST_COMPARATOR)
                .collect(Collectors.toList());
    }

    public static final Comparator<String> MASTER_FIRST_COMPARATOR = (o1, o2) -> MASTER.equals(o1) ? -1 : 1;

    private static RevCommit getLastCommit(Repository repository) throws Exception {
        RevWalk revWalk = new RevWalk(repository);
        Ref headCommitReference = repository.getRefDatabase().findRef("HEAD");

        ObjectId headObjectId;
        if (headCommitReference != null) {
            headObjectId = headCommitReference.getObjectId();
        } else {
            headObjectId = repository.resolve("HEAD");
        }

        if (headObjectId == null) {
            throw new Exception(
                "Could not get HEAD Ref, are you sure have commits in the gitDirectory?");
        }
        RevCommit headCommit = revWalk.parseCommit(headObjectId);
        revWalk.markStart(headCommit);
        return headCommit;
    }

    private static FileRepository getGitRepository(File gitDirectory) throws Exception {
        if (gitDirectory == null || !gitDirectory.exists()) {
            throw new Exception("Could not create git repository. " + gitDirectory
                                + " is not exists!");
        }
        Repository repository;
        try {
            repository = new FileRepositoryBuilder().setGitDir(gitDirectory).readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .build();
        } catch (IOException e) {
            throw new Exception("Could not initialize git repository...", e);
        }

        if (repository == null) {
            throw new Exception("Could not create git repository. Are you sure '" + gitDirectory
                                + "' is the valid Git root for your project?");
        }

        return (FileRepository) repository;
    }
}
