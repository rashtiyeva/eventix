package org.eventix.authservice.security;

public class RequestContext {

    private static final ThreadLocal<String> IP = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_AGENT = new ThreadLocal<>();
    private static final ThreadLocal<String> DEVICE_ID = new ThreadLocal<>();

    public static void setIp(String ip) {
        IP.set(ip);
    }

    public static String getIp() {
        return IP.get();
    }

    public static void setUserAgent(String ua) {
        USER_AGENT.set(ua);
    }

    public static String getUserAgent() {
        return USER_AGENT.get();
    }

    public static void clear() {
        IP.remove();
        USER_AGENT.remove();
        DEVICE_ID.remove();
    }

    public static void setDeviceId(String deviceId) {
        DEVICE_ID.set(deviceId);
    }

    public static String getDeviceId() {
        return DEVICE_ID.get();
    }
}
