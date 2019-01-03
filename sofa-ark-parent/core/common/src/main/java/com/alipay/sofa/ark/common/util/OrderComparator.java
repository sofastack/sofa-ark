package com.alipay.sofa.ark.common.util;

import com.alipay.sofa.ark.spi.service.PriorityOrdered;

import java.util.Comparator;

/**
 * {@link Comparator} implementation for {@link PriorityOrdered} objects, sorting
 * by order value ascending.
 *
 * @author qilong.zql
 * @since 0.4.0
 */
public class OrderComparator implements Comparator<PriorityOrdered> {
    @Override
    public int compare(PriorityOrdered o1, PriorityOrdered o2) {
        return Integer.compare(o1.getPriority(), o2.getPriority());
    }
}