package org.uu.common.core.excption;

/**
 * WebSocket鉴权失败 异常
 *
 * @author Simon
 * @date 2023/11/16
 */
public class WebSocketAuthException extends RuntimeException {
    public WebSocketAuthException(String message) {
        super(message);
    }
}
