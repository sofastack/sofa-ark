/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.alipay.sofa.ark.container.session;

import com.alipay.sofa.ark.spi.service.session.CommandProvider;

/**
 * @author qilong.zql
 * @since 0.5.0
 */
public class PluginCommandProvider implements CommandProvider{

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public String getHelp(String commandMarker) {
        return null;
    }

    @Override
    public boolean validate(String command) {
        return false;
    }

    @Override
    public String handleCommand(String command) {
        return null;
    }
}