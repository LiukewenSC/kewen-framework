package com.cu.framework.sample.basic.req;

import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 
 * @author kewen
 * @since 2023-04-26
 */
@Data
public class ValidatedReq {

    /** ID，必填 */
    @NotNull
    private Long id;
    /** 名称，不能为空串 */
    @NotEmpty
    private String name;

    /** 年龄，必填 */
    @NotNull(message = "年龄不能为空")
    private Integer age;

    /** 开关项，@AssertTrue 要求值必须为 true 才通过校验 */
    @AssertTrue
    private Boolean enable;

    /** 角色名列表，不能为空 */
    @NotEmpty
    private List<String> roles;
}
