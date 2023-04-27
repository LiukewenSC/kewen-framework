package com.kewen.framework.sample.controller;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 
 * @author kewen
 * @since 2023-04-27
 */
@Data
public class LocalDateParam {
    LocalDate localDate;
    LocalDateTime localDateTime;
}
