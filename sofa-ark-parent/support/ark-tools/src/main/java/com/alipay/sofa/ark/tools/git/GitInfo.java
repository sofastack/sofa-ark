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

public class GitInfo {

    private String buildUser;
    private String buildEmail;
    private String lastCommitId;
    private long   lastCommitTime;
    private String lastCommitDateTime;
    private String lastCommitUser;
    private String lastCommitEmail;
    private String branchName;
    private String repository;

    public String getBuildUser() {
        return buildUser;
    }

    public void setBuildUser(String buildUser) {
        this.buildUser = buildUser;
    }

    public String getBuildEmail() {
        return buildEmail;
    }

    public void setBuildEmail(String buildEmail) {
        this.buildEmail = buildEmail;
    }

    public String getLastCommitId() {
        return lastCommitId;
    }

    public void setLastCommitId(String lastCommitId) {
        this.lastCommitId = lastCommitId;
    }

    public long getLastCommitTime() {
        return lastCommitTime;
    }

    public void setLastCommitTime(long lastCommitTime) {
        this.lastCommitTime = lastCommitTime;
    }

    public String getLastCommitUser() {
        return lastCommitUser;
    }

    public void setLastCommitUser(String lastCommitUser) {
        this.lastCommitUser = lastCommitUser;
    }

    public String getLastCommitEmail() {
        return lastCommitEmail;
    }

    public void setLastCommitEmail(String lastCommitEmail) {
        this.lastCommitEmail = lastCommitEmail;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getLastCommitDateTime() {
        return lastCommitDateTime;
    }

    public void setLastCommitDateTime(String lastCommitDateTime) {
        this.lastCommitDateTime = lastCommitDateTime;
    }

    @Override
    public String toString() {
        return "GitInfo{" + "buildUser='" + buildUser + '\'' + ", buildEmail='" + buildEmail + '\''
               + ", lastCommitId='" + lastCommitId + '\'' + ", lastCommitTime=" + lastCommitTime
               + ", lastCommitDateTime='" + lastCommitDateTime + '\'' + ", lastCommitUser='"
               + lastCommitUser + '\'' + ", lastCommitEmail='" + lastCommitEmail + '\''
               + ", branchName='" + branchName + '\'' + ", repository='" + repository + '\'' + '}';
    }
}
