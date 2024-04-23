package com.zzsn.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultVo<T> {
    private Boolean success;
    private String msg;
    private T data;

    public static <T> ResultVo<T> ofSuccess(T data) {
        return new ResultVo<>(true, "成功", data);
    }

    public static <T> ResultVo<T> ofSuccessMsg(String successMsg) {
        return new ResultVo<>(true, successMsg, null);
    }

    public static <T> ResultVo<T> ofFailure(String errorMsg) {
        return new ResultVo<>(false, errorMsg, null);
    }
}
