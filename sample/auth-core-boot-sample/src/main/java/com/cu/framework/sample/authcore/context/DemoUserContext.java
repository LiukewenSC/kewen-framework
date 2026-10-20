package com.cu.framework.sample.authcore.context;

/**
 * 当前请求的身份上下文。
 * <p>
 * auth-core 不负责登录，本示例用最直白的方式把"这是谁"交给请求头 {@code X-Demo-User}：
 * 过滤器在最早的阶段写入、结束时清理，之后所有扩展点实现都从这里取用户名。
 * 这样一条 curl 就能换一个人，不需要会话、不需要登录接口
 *
 * @author kewen
 * @since 2026-10-20
 */
public final class DemoUserContext {

    /** 携带身份的请求头名 */
    public static final String USER_HEADER = "X-Demo-User";
    /** 携带追踪标识的请求头名，用于把本次请求实际下发的语句取回来观察 */
    public static final String TRACE_HEADER = "X-Trace-Id";

    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();
    private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<>();

    private DemoUserContext() {
    }

    /**
     * 写入当前请求的身份
     *
     * @param username 用户名，可为 null（表示未提供身份）
     * @param traceId  追踪标识，可为 null（表示本次不记录语句）
     */
    public static void set(String username, String traceId) {
        USERNAME.set(username);
        TRACE_ID.set(traceId);
    }

    /**
     * 清理当前请求的上下文，必须在请求结束时调用，否则线程复用会把身份串给下一个请求
     */
    public static void clear() {
        USERNAME.remove();
        TRACE_ID.remove();
    }

    /**
     * @return 当前用户名，未提供身份时为 null
     */
    public static String username() {
        return USERNAME.get();
    }

    /**
     * @return 当前追踪标识，未提供时为 null
     */
    public static String traceId() {
        return TRACE_ID.get();
    }
}
