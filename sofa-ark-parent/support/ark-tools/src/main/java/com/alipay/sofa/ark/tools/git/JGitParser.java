package com.alipay.sofa.ark.tools.git;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import static com.alipay.sofa.ark.spi.constant.Constants.DATE_FORMAT;

public class JGitParser {

    public static GitInfo parse(File dotGitDirectory) {
        try (FileRepository repository = getGitRepository(dotGitDirectory)) {

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
            }

            return gitInfo;
        } catch (Exception exception) {
            return null;
        }
    }

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
            throw new Exception("Could not get HEAD Ref, are you sure have commits in the dotGitDirectory?");
        }
        RevCommit headCommit = revWalk.parseCommit(headObjectId);
        revWalk.markStart(headCommit);
        return headCommit;
    }

    private static FileRepository getGitRepository(File dotGitDirectory) throws Exception {
        if (!dotGitDirectory.exists()) {
            throw new Exception("Could not create git repository. " + dotGitDirectory + " is not exists!");
        }
        Repository repository;
        try {
            repository = new FileRepositoryBuilder()
                    .setGitDir(dotGitDirectory)
                    .readEnvironment() // scan environment GIT_* variables
                    .findGitDir() // scan up the file system tree
                    .build();
        } catch (IOException e) {
            throw new Exception("Could not initialize git repository...", e);
        }

        if (repository == null) {
            throw new Exception("Could not create git repository. Are you sure '" + dotGitDirectory +
                    "' is the valid Git root for your project?");
        }

        return (FileRepository) repository;
    }
}
