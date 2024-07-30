package org.uu.common.core.result;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageReturn;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel(description = "KYC统一接口返回数据")
public class KycRestResult<T> implements Serializable {

    @ApiModelProperty(value = "业务状态码, 1: 成功  其他为失败")
    private String code;

    @ApiModelProperty(value = "返回数据")
    private T data;

    @ApiModelProperty(value = "提示信息")
    private String msg;

    @ApiModelProperty(value = "记录总条数")
    private Integer total;

    public static <T> KycRestResult<T> ok() {
        return ok(null);
    }


    public static <T> KycRestResult<T> ok(T data, Long total) {
        KycRestResult<T> result = new KycRestResult<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(ResultCode.SUCCESS.getMsg());
        result.setData(data);
        result.setTotal(total.intValue());
        return result;
    }

    public static <T> KycRestResult<T> ok(T data) {
        ResultCode rce = ResultCode.SUCCESS;
        if (data instanceof Boolean && Boolean.FALSE.equals(data)) {
            rce = ResultCode.SYSTEM_EXECUTION_ERROR;
        }
        return result(rce, data);
    }

    public static <T> KycRestResult<T> failed() {
        return result(ResultCode.SYSTEM_EXECUTION_ERROR.getCode(), ResultCode.SYSTEM_EXECUTION_ERROR.getMsg(), null);
    }

    public static <T> KycRestResult<T> failed(String msg) {
        return result(ResultCode.SYSTEM_EXECUTION_ERROR.getCode(), msg, null);
    }

    public static <T> KycRestResult<T> failed(ResultCode resultCode) {
        return result(resultCode.getCode(), resultCode.getMsg(), null);
    }

    public static <T> KycRestResult<T> relogin(ResultCode resultCode) {
        return result(ResultCode.RELOGIN.getCode(), resultCode.getMsg(), null);
    }

    /**
     * 错误返回
     *
     * @param resultCode
     * @return {@link KycRestResult}<{@link T}>
     */
    public static <T> KycRestResult<T> failure(ResultCode resultCode) {
        return result(resultCode.getCode(), resultCode.getMsg(), null);
    }

    /**
     * 错误返回 自定义msg
     *
     * @param resultCode
     * @return {@link KycRestResult}<{@link T}>
     */
    public static <T> KycRestResult<T> failure(ResultCode resultCode, String msg) {
        return result(resultCode.getCode(), msg, null);
    }

    /**
     * 错误返回 返回data
     *
     * @param resultCode
     * @param data
     * @return {@link KycRestResult}<{@link T}>
     */
    public static <T> KycRestResult<T> failure(ResultCode resultCode, T data) {
        return result(resultCode.getCode(), resultCode.getMsg(), data);
    }

    private static <T> KycRestResult<T> result(ResultCode resultCode, T data) {
        return result(resultCode.getCode(), resultCode.getMsg(), data);
    }

    private static <T> KycRestResult<T> result(ResultCode resultCode) {
        return result(resultCode.getCode(), resultCode.getMsg(), null);
    }


    public static <T> KycRestResult<T> result(String code, String msg, T data) {
        KycRestResult<T> result = new KycRestResult<>();
        result.setCode(code);
        result.setData(data);
        result.setMsg(msg);
        return result;
    }

    public static <T> KycRestResult<T> i18nFailed(String key) {
        return result(ResultCode.SYSTEM_EXECUTION_ERROR.getCode(), ResultCode.SYSTEM_EXECUTION_ERROR.getMsg(), null);
    }

    public static <T> KycRestResult page(PageReturn<T> page) {
        KycRestResult<List<T>> result = new KycRestResult<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setData(page.getList());
        result.setMsg(ResultCode.SUCCESS.getMsg());
        result.setTotal(page.getTotal().intValue());
        return result;
    }


}
