package com.kewen.framework.boot.sample.storage.controller;

import com.kewen.framework.common.core.model.Result;
import com.kewen.framework.storage.web.model.FileFillSupport;
import com.kewen.framework.storage.web.model.FileInfo;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * @author kewen
 * @since 2023-04-28
 */
@RestController
@RequestMapping("/storage")
public class StorageTestContorller {


    @GetMapping("/testFillFile")
    public Result testFillFile(Long fileId) {
        FillTest fillTest = new FillTest();
        fillTest.setFileId(fileId);

        fillTest.setInnerFill(
                new InnerFillTest().setFileId(fileId)
        );

        fillTest.setInnerFillTests(
                Arrays.asList(
                        new InnerFillTest().setFileId(fileId),
                        new InnerFillTest().setFileId(fileId),
                        new InnerFillTest().setFileId(fileId)
                ));

        return Result.success(fillTest);
    }

    @Data
    public static class FillTest implements FileFillSupport {

        private Long fileId;

        private FileInfo fileInfo;

        private InnerFillTest innerFill;

        private List<InnerFillTest> innerFillTests;

        @Override
        public Long getFileId() {
            return fileId;
        }

        @Override
        public void setFileInfo(FileInfo result) {
            this.fileInfo = result;
        }
    }

    @Data
    @Accessors(chain = true)
    public static class InnerFillTest implements FileFillSupport {

        Long fileId;

        FileInfo fileInfo;

        @Override
        public Long getFileId() {
            return fileId;
        }

        @Override
        public void setFileInfo(FileInfo result) {
            this.fileInfo = result;
        }
    }

}
