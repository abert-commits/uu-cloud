package org.uu.auth.comm.exception;

import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.DisabledResponse;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.core.result.UnauthorizedResponse;
import org.uu.common.web.exception.UnauthorizedException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
@Order(-1)
public class AuthExceptionHandler {
    /**
     * 用户不存在
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(UsernameNotFoundException.class)
    public RestResult handleUsernameNotFoundException(UsernameNotFoundException e) {
        return RestResult.failure(ResultCode.USERNAME_OR_PASSWORD_ERROR);
    }

    /**
     * 用户名和密码异常
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(InvalidGrantException.class)
    public RestResult handleInvalidGrantException(InvalidGrantException e) {
        return RestResult.failure(ResultCode.USERNAME_OR_PASSWORD_ERROR);
    }

    /**
     * 用户名和密码异常
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(InvalidClientException.class)
    public RestResult handleInvalidGrantException(InvalidClientException e) {
        return RestResult.failure(ResultCode.CLIENT_AUTHENTICATION_FAILED);
    }


    /**
     * 账户异常(禁用、锁定、过期)
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({InternalAuthenticationServiceException.class})
    public ResponseEntity handleInternalAuthenticationServiceException(InternalAuthenticationServiceException e) {
//        return RestResult.failure(ResultCode.RELOGIN, e.getMessage());
        if (e.getCause().getClass().equals(DisabledException.class)) {
            return ResponseEntity.status(200).body(DisabledResponse.create());
        }
        return ResponseEntity.status(401).body(UnauthorizedResponse.create());
    }

    /**
     * token 无效或已过期
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({InvalidTokenException.class})
    public ResponseEntity handleInvalidTokenExceptionException(InvalidTokenException e) {

//        return RestResult.failure(ResultCode.RELOGIN, e.getMessage());

        return ResponseEntity.status(401).body(UnauthorizedResponse.create());
    }

    /**
     * token 无效或已过期
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({NoSuchClientException.class})
    public ResponseEntity noSuchClientException(NoSuchClientException e) {
//        return RestResult.failure(ResultCode.RELOGIN, e.getMessage());

        return ResponseEntity.status(401).body(UnauthorizedResponse.create());
    }

    /**
     * token 无效或已过期
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler({DisabledException.class})
    public RestResult disabledException(DisabledException e) {
        return RestResult.failure(ResultCode.valueOf("1111"));
    }
}
