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
        return HELP_MESSAGE;
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

    private static final String HELP_MESSAGE = "USAGE: plugin [option...] [pluginName...]\n" +
            "  -h  Shows the help message.\n" +
            "  -m  Shows the meta info of specified pluginName\n" +
            "  -s  Shows the service info of specified pluginName\n" +
            "  -d  Shows the detail info of specified pluginName\n" +
            "SAMPLE: plugin -ms plugin-*\n";
}