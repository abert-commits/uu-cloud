package org.uu.common.web.exception;

import lombok.Getter;
import org.uu.common.core.result.ResultCode;

@Getter
public class UnauthorizedException extends RuntimeException {

    public ResultCode resultCode;

    public UnauthorizedException(ResultCode errorCode) {
        super(errorCode.getMsg());
        this.resultCode = errorCode;
    }

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedException(Throwable cause) {
        super(cause);
    }
}
