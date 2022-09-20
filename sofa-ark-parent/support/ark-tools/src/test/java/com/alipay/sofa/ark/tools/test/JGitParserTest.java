package com.alipay.sofa.ark.tools.test;

import com.alipay.sofa.ark.tools.git.GitInfo;
import com.alipay.sofa.ark.tools.git.JGitParser;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class JGitParserTest {

    @Test
    public void testParse() {
        GitInfo gitInfo = JGitParser.parse(new File("../../..//.git"));
        Assert.assertNotNull(gitInfo);
        Assert.assertNotNull(gitInfo.getBranchName());

    }
}
