package com.alipay.sofa.ark.container.command;

import java.util.Set;

import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;

/**
 * 插件查询命令行提供者
 *
 * @author joe
 * @version 2018.07.20 11:45
 */
public class PluginCommandProvider extends AbstractCommandProvider {
    private static String        COMMAND_PRE = "plugin";
    private PluginManagerService service     = ArkServiceContainerHolder.getContainer()
        .getService(PluginManagerService.class);

    @Override
    protected String getCommandPre() {
        return COMMAND_PRE;
    }

    @Override
    protected String handleCommandLine(String[] commands) {
        String command = commands[1];
        StringBuilder sb = new StringBuilder();

        switch (command) {
            case "list":
                if (commands.length == 2) {
                    sb.append("plugins :\n").append(getPrint(service.getAllPluginNames(), "\t"));
                    break;
                }
            case "info":
                if (commands.length == 3) {
                    Plugin plugin = service.getPluginByName(commands[2]);
                    if (plugin == null) {
                        sb.append(String.format("plugin %s not found", commands[2]));
                    } else {
                        sb.append(getPrint(plugin));
                    }
                    break;
                }
            default:
                sb.append("命令错误\n");
                sb.append(getHelp());
                break;
        }
        return sb.toString();
    }

    @Override
    public String getHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("plugin list : list ark container all plugins name.\n");
        sb.append("plugin info [plugin-name] : list plugin info.\n");
        return sb.toString();
    }

    private String getPrint(Plugin plugin) {
        StringBuilder sb = new StringBuilder();
        sb.append(plugin.getPluginName()).append(" :").append("\n");
        sb.append("\t").append("group-id : ").append(plugin.getPluginName()).append("\n");
        sb.append("\t").append("artifact-id :").append(plugin.getPluginName()).append("\n");
        sb.append("\t").append("version : ").append(plugin.getPluginName()).append("\n");
        sb.append("\t").append("priority : ").append(plugin.getPluginName()).append("\n");
        sb.append("\t").append("activator : ").append(plugin.getPluginName()).append("\n");
        sb.append("\t").append("export-packages : ").append("\n");
        sb.append(getPrint(plugin.getExportPackages(), "\t\t"));
        sb.append("\t").append("export-classes : ").append("\n");
        sb.append(getPrint(plugin.getExportClasses(), "\t\t"));
        sb.append("\t").append("import-packages : ").append("\n");
        sb.append(getPrint(plugin.getImportPackages(), "\t\t"));
        sb.append("\t").append("import-classes : ").append("\n");
        sb.append(getPrint(plugin.getImportClasses(), "\t\t"));
        sb.append("\t").append("import-resources : ").append("\n");
        sb.append(getPrint(plugin.getImportResources(), "\t\t"));
        sb.append("\t").append("export-resources : ").append("\n");
        sb.append(getPrint(plugin.getExportResources(), "\t\t"));
        sb.append("\t").append("export-index : ").append("\n");
        sb.append(getPrint(plugin.getExportIndex(), "\t\t"));
        return sb.toString();
    }

    private String getPrint(Set<String> set, String tab) {
        if (set.isEmpty()) {
            return "\tno plugin exist";
        }
        StringBuilder sb = new StringBuilder();
        for (String str : set) {
            sb.append(tab).append(str).append("\n");
        }
        return sb.toString();
    }
}
