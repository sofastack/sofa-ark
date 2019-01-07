/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.alipay.sofa.ark.spi.command;

/**
 * @author qilong.zql
 * @since 0.5.0
 */
public interface Command {

    /**
     * Get Command Prefix Marker.
     * @return
     */
    String getCommandMarker();

    /**
     * Get Command Help message, usually command schema.
     * @return
     */
    String getCommandHelp();

    /**
     * Process Command
     * @return
     * @throws Throwable
     */
    String process() throws Throwable;

}