package com.kewen.framework.sample.config;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SentinelConfig implements CommandLineRunner {

    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }


    @Override
    public void run(String... args) throws Exception {

        initFlowRule();
        initDegradeRule();

    }
    private void initDegradeRule() {
        List<DegradeRule> rules = new ArrayList<>();
        DegradeRule rule = new DegradeRule("degradeRule")
                //熔断策略，支持慢调用比例/异常比例/异常数策略
                .setGrade(CircuitBreakerStrategy.SLOW_REQUEST_RATIO.getType())
                //   慢调用比例模式下为慢调用临界 RT（即最大的响应时间，超出该值计为慢调用 ）；异常比例/异常数模式下为对应的阈值
                //   当资源的每秒异常总数占通过量的比值超过阈值（DegradeRule 中的 count）之后，资源进入降级状态，
                //即在接下的时间窗口（DegradeRule 中的 timeWindow，以 s 为单位）之内，对这个方法的调用都会自动地返回。
                .setCount(500)
                // 熔断时长，单位为 s
                .setTimeWindow(5)
                //熔断触发的最小请求数，请求数小于该值时即使异常比率超出阈值也不会熔断（1.7.0 引入）
                .setMinRequestAmount(5)
                //统计时长（单位为 ms），如 60*1000 代表分钟级（1.8.0 引入）
                .setStatIntervalMs(1000)
                // 慢调用比例阈值，仅慢调用比例模式有效（1.8.0 引入）
                .setSlowRatioThreshold(1)
                ;
        rules.add(rule);

        DegradeRuleManager.loadRules(rules);
        System.out.println("Degrade rule loaded: " + rules);
    }

    private void initFlowRule() {
        List<FlowRule> rules = new ArrayList<FlowRule>();
        FlowRule rule = new FlowRule();
        rule.setResource("flowRule");
        rule.setGrade(RuleConstant.FLOW_GRADE_THREAD);
        rule.setCount(3);
        //rule.setLimitApp("default");
        rules.add(rule);


        /*FlowRule feignRule = new FlowRule();

        feignRule.setResource("GET:http://nocos-provider-sample/hello/hello");
        feignRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        feignRule.setCount(1);
        rules.add(feignRule);*/

        FlowRuleManager.loadRules(rules);
    }
}
