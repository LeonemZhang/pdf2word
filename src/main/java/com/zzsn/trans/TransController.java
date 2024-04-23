package com.zzsn.trans;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import com.pdftron.pdf.StructuredOutputModule;
import com.zzsn.common.ResultVo;
import com.zzsn.common.StringWrapper;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.Arrays;

@RestController
@RequestMapping("/api")
@Api(tags = "转换接口")
@Slf4j
@RequiredArgsConstructor
public class TransController {
    private final TransService transService;

    @Operation(summary = "本地转换", description = "传入pdf文件所在的上层目录的绝对路径，如D:\\pdf\\test.pdf，传入D:\\pdf即可")
    @PostMapping("/transFromLocal")
    public ResultVo<Void> transFromLocal(@RequestBody StringWrapper req) {
        try {
            if (!StructuredOutputModule.isModuleAvailable()) {
                return ResultVo.ofFailure("PDF转换模块不可用");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        final File folder = new File(req.getValue());
        Assert.isTrue(folder.exists(), "目录不存在");
        File[] pdfFiles = folder.listFiles();
        Assert.notNull(pdfFiles, "目录下不存在文件");
        pdfFiles = Arrays.stream(pdfFiles).filter(file -> !file.isDirectory() && FileUtil.getType(file).equals("pdf")).toArray(File[]::new);
        Assert.notEmpty(pdfFiles, "目录下不存在pdf文件");

        final File outputFolder = FileUtil.file(req.getValue(), DateUtil.format(DateUtil.date(), "yyyy-MM-dd_HH-mm-ss"));
        FileUtil.mkdir(outputFolder);

        transService.transPdfList(pdfFiles, outputFolder.getAbsolutePath());

        return ResultVo.ofSuccessMsg("转换任务开始运行");
    }
}
