package com.alipay.sofa.ark.container.command;

import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.service.session.CommandProvider;

/**
 * @author joe
 * @version 2018.07.20 11:30
 */
public abstract class AbstractCommandProvider implements CommandProvider {

    @Override
    public boolean validate(String command) {
        if (StringUtils.isEmpty(command)) {
            return false;
        }

        String[] strs = process(command);
        command = strs[0];
        return command.equals(getCommandPre());
    }

    @Override
    public String handleCommand(String command) {
        String[] commands = process(command);
        if (commands.length < 2) {
            return getHelp(command);
        }

        return handleCommandLine(commands);
    }

    @Override
    public String getHelp(String commandMarker) {
        if (getCommandPre().equals(commandMarker)) {
            return getHelp();
        }else{
            return null;
        }
    }

    /**
     * 处理命令行（以空白符分隔）
     * @param commandLine 命令行
     * @return 处理后的每个有效字符
     */
    protected String[] process(String commandLine) {
        return commandLine.trim().split(Constants.SPACE_SPLIT);
    }

    /**
     * 获取该provider可以处理的命令前缀
     * @return 命令前缀
     */
    protected abstract String getCommandPre();

    /**
     * 处理命令行
     * @param commands 命令行
     * @return 处理结果
     */
    protected abstract String handleCommandLine(String[] commands);
}
