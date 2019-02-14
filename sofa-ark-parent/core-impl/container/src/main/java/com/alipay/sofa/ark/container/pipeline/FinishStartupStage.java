/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.alipay.sofa.ark.container.pipeline;

import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.event.ArkEvent;
import com.alipay.sofa.ark.spi.pipeline.PipelineContext;
import com.alipay.sofa.ark.spi.pipeline.PipelineStage;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
@Singleton
public class FinishStartupStage implements PipelineStage {
    @Inject
    private EventAdminService eventAdminService;

    @Override
    public void process(PipelineContext pipelineContext) throws ArkRuntimeException {
        eventAdminService.sendEvent(new ArkEvent() {
            @Override
            public String getTopic() {
                return Constants.ARK_EVENT_TOPIC_AFTER_FINISH_STARTUP_STAGE;
            }
        });
    }
}