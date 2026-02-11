package com.kewen.framework.basic.filter;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 自定义过滤器的代理，用于处理请求开始进入时的过滤器
 *  spring security自带的过滤器的order为-100，因此在spring之前的需要大于此
 * @author kewen
 * @since 2024-07-10
 */
@Order(-101)
public class EarlyRequestFilterProxy extends OncePerRequestFilter {

    List<EarlyRequestFilter> filters;

    public EarlyRequestFilterProxy(ObjectProvider<EarlyRequestFilter> earlyRequestFilters) {
        List<EarlyRequestFilter> collect = earlyRequestFilters.orderedStream().collect(Collectors.toList());
        if (!collect.isEmpty()) {
            this.filters = collect;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (filters == null || filters.isEmpty()) {
            filterChain.doFilter(request, response);
        } else {
            BeforeVirtualFilterChain virtualFilterChain = new BeforeVirtualFilterChain(filters, filterChain);
            virtualFilterChain.doFilter(request, response);
        }
    }


    /**
     * 和SpringSecurity类似的过滤器链，可以先于SpringSecurity处理
     */
    static class BeforeVirtualFilterChain implements FilterChain {

        private int pos = 0;
        private final int size;
        List<EarlyRequestFilter> filters;
        FilterChain original;

        public BeforeVirtualFilterChain(List<EarlyRequestFilter> filters, FilterChain original) {
            this.filters = filters;
            this.original = original;
            size = filters.size();
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            if (pos == size){
                original.doFilter(request, response);
            } else {
                EarlyRequestFilter filter = filters.get(pos++);
                //只有HttpServlet才走，其他的就跳过，
                if (response instanceof HttpServletResponse && request instanceof HttpServletRequest) {
                    //需要把自己传进去，方便处理下一个
                    filter.doFilter(((HttpServletRequest) request), ((HttpServletResponse) response),this);
                } else {
                    //啥也不干，递归处理下一个过滤器，其实就是空跑一次，因为ServletRequest基本不变，我们自定义都不会走
                    doFilter(request, response);
                }
            }
        }
    }
}
