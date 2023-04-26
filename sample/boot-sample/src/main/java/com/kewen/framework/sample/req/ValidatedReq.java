package com.kewen.framework.sample.req;

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

    @NotNull
    private Long id;
    @NotEmpty
    private String name;

    @NotNull(message = "年龄不能为空")
    private Integer age;

    @AssertTrue
    private Boolean enable;

    @NotEmpty
    private List<String> roles;
}
