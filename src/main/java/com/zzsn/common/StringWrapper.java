package com.zzsn.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "字符串包装类")
@Getter
@Setter
public class StringWrapper {
    @Schema(description = "字符串值")
    private String value;
}
