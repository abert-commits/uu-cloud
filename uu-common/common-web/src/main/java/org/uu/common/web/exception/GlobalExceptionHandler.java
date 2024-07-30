package org.uu.common.web.exception;

import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义异常处理
 *
 * @author Simon
 * @date 2023/11/18
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(IllegalArgumentException.class)
    public <T> RestResult<T> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("非法参数异常，异常原因：{}", e.getMessage(), e);
        e.printStackTrace();
        return RestResult.failure(ResultCode.PARAM_VALID_FAIL, e.getMessage());
    }

    // <1> 处理 form data方式调用接口校验失败抛出的异常
    @ExceptionHandler(BindException.class)
    public RestResult bindExceptionHandler(BindException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        List<String> collect = fieldErrors.stream()
                .map(o -> o.getDefaultMessage())
                .collect(Collectors.toList());
        return RestResult.failure(ResultCode.PARAM_VALID_FAIL, String.valueOf(collect));
    }

    @ExceptionHandler(BizException.class)
    public RestResult bindExceptionHandler(BizException e) {

        log.error("业务异常，异常原因：{}", e.getMessage());
        ResultCode resultCode = e.getResultCode();
        return RestResult.failed(resultCode);
    }

    // <2> 处理 json 请求体调用接口校验失败抛出的异常 参数校验不通过
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public RestResult methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        List<String> collect = fieldErrors.stream()
                .map(o -> o.getDefaultMessage())
                .collect(Collectors.toList());
        return RestResult.failure(ResultCode.PARAM_VALID_FAIL,String.valueOf(collect));

    }

    // <3> 处理单个参数校验失败抛出的异常
//    @ExceptionHandler(ConstraintViolationException.class)
//    public RestResult constraintViolationExceptionHandler(ConstraintViolationException e) {
//        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
//        List<String> collect = constraintViolations.stream()
//                .map(o -> o.getMessage())
//                .collect(Collectors.toList());
//        return RestResult.failed(String.valueOf(collect));
//    }


    /*
     * 处理订单号重复异常
     * */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public RestResult methodArgumentNotValidExceptionHandler(SQLIntegrityConstraintViolationException e) {
        return RestResult.failure(ResultCode.DATA_DUPLICATE_SUBMISSION);
    }

    /*
     * 处理订单号重复异常
     * */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public RestResult methodArgumentNotValidExceptionHandler(DataIntegrityViolationException e) {
        return RestResult.failure(ResultCode.DATA_DUPLICATE_SUBMISSION);
    }

    /*
     * 处理form-data 提交参数 错误异常
     * */
    @ExceptionHandler(ConstraintViolationException.class)
    public RestResult handleValidationExceptions(
            ConstraintViolationException ex) {
        ArrayList<Object> errors = new ArrayList<>();
        ex.getConstraintViolations().forEach((violation) -> {
            String errorMessage = violation.getMessage();
            errors.add(errorMessage);
        });
        return RestResult.failure(ResultCode.PARAM_VALID_FAIL, String.valueOf(errors));
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthorizedException.class)
    public RestResult handleUnauthorizedExceptions(
            UnauthorizedException ex) {
        return RestResult.failure(ex.resultCode);
    }
}
