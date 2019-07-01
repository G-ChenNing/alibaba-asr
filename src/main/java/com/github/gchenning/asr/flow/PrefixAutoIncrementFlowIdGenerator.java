package com.github.gchenning.asr.flow;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自动增长的流程id计数器
 */
public class PrefixAutoIncrementFlowIdGenerator implements FlowIdGenerator {

    private final String prefix;

    private AtomicInteger counter = new AtomicInteger(0);

    @Override
    public String generate() {
        return "flow:" + LocalDate.now() + ":" + prefix + ":" + counter.incrementAndGet();
    }

    public PrefixAutoIncrementFlowIdGenerator(String prefix) {
        if (StringUtils.isBlank(prefix)) {
            throw new IllegalArgumentException("请传入指定的流程ID前缀");
        }
        this.prefix = prefix;
    }


}
