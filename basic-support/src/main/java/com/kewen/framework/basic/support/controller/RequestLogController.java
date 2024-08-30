package com.kewen.framework.basic.support.controller;


import com.kewen.framework.basic.model.Result;
import com.kewen.framework.basic.support.request.persistent.database.RequestLogService;
import com.kewen.framework.basic.support.request.persistent.database.TodayStatisticResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 
 * @author kewen
 * @since 2023-04-27
 */
@RestController
@RequestMapping("/request")
public class RequestLogController {

    @Autowired
    RequestLogService requestLogService;

    /**
     * 访问统计
     * @return
     */
    @GetMapping("/visitStatistic")
    public Result visitStatistic(@RequestParam(value = "startDate",required = false) LocalDate startDate,
                                 @RequestParam(value = "endDate",required = false) LocalDate endDate){
        if (startDate==null){
            startDate=LocalDate.now();
        }
        if (endDate==null){
            endDate=LocalDate.now();
        }
        List<TodayStatisticResp> resp = requestLogService.visitStatistic(startDate,endDate);

        return Result.success(resp);
    }

}
