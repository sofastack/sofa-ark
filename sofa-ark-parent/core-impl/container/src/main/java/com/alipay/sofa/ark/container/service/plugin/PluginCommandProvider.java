/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.alipay.sofa.ark.container.service.plugin;

import com.alipay.sofa.ark.spi.service.session.CommandProvider;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class PluginCommandProvider implements CommandProvider {

    private static final String DESCRIPTION = "Plugin Command Tips:";

    @Override
    public String getHelp() {

    }

    @Override
    public boolean validate(String command) {
        return false;
    }

    @Override
    public String handleCommand(String command) {
        return null;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    private static final String HELP_MESSAGE = "USAGE: plugin [option...] [pluginName...]" +
            "  -h  Shows the help message." +
            "  -m  Shows the meta info of specified pluginName" +
            "  -s  Shows the service info of specified pluginName" +
            "  -d  Shows the ";
}