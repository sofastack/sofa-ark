package com.alipay.sofa.ark.spi.event.biz;

import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.event.AbstractArkEvent;
import com.alipay.sofa.ark.spi.model.Biz;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: BizFailedEvent.java, v 0.1 2024年03月21日 10:42 立蓬 Exp $
 */
public class BizFailedEvent extends AbstractArkEvent<Biz> {
    Throwable throwable;

    public BizFailedEvent(Biz source, Throwable e) {
        super(source);
        this.throwable = e;
        this.topic = Constants.BIZ_EVENT_TOPIC_BIZ_FAILED;
    }
}