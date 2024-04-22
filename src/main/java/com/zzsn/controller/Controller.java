package com.zzsn.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import com.pdftron.pdf.Convert;
import com.pdftron.pdf.PDFDoc;
import com.pdftron.pdf.PDFNet;
import com.pdftron.pdf.StructuredOutputModule;
import com.spire.doc.Document;
import com.spire.doc.FileFormat;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author 张宗涵
 * @date 2024/4/20
 */
@RestController
@RequestMapping("/test")
@Slf4j
public class Controller {
    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @Operation(summary = "path", description = "传入从/share开始的目录路径，如/share/xx公司/2020年报告")
    @GetMapping("/upload")
    public Object path(@RequestParam("path") String path) {
        File file = new File(path);
        Assert.isTrue(file.exists(), "目录不存在");
        final File[] files = file.listFiles();
        Assert.notNull(files, "目录下不存在文件");
        Assert.isTrue(CollectionUtil.isNotEmpty(Arrays.asList(files)), "目录下不存在文件");

        final File output = FileUtil.file(path, DateUtil.format(DateUtil.date(), "yyyy-MM-dd_HH-mm-ss"));
        FileUtil.mkdir(output);
        final File logFile = FileUtil.touch(FileUtil.file(output, "任务日志.txt"));

        PDFNet.initialize("demo:leonemzhang@gmail.com:7fea6f0e0200000000c32149cb880899b8eb151cb3cdf2c10567ca11bb");
        PDFNet.addResourceSearchPath("C:\\Users\\EDY\\code space\\pdf2word\\src\\main\\resources\\lib");

        try {
            if (!StructuredOutputModule.isModuleAvailable()) {
                FileUtil.appendString("pdf转换word模块不可用，任务失败", logFile, Charset.defaultCharset());
                log.error("pdf转换word模块不可用");
                return "pdf转换word模块不可用";
            }
            for (final File one : files) {
                if (one.isDirectory() || !FileUtil.getType(one).equals("pdf")) {
                    FileUtil.appendString(DateUtil.now() + "  文件:" + one.getName() + "，不是pdf文件\n", logFile, Charset.defaultCharset());
                    log.warn("文件:{}，不是pdf文件，已跳过处理该文件", one.getName());
                    continue;
                }

                log.info("开始转换文件：{}", one.getName());
                FileUtil.appendString(DateUtil.now() + "  文件：" + one.getName() + "，开始转换\n", logFile, Charset.defaultCharset());
                String sourcePath = one.getAbsolutePath();

                final int pageCount;
                try (PDFDoc doc = new PDFDoc(sourcePath)) {
                    pageCount = doc.getPageCount();
                }
                int batchNum = 1;
                List<File> tempFileList = new ArrayList<>();
                String fileNameWithoutExt = one.getName().replace(".pdf", "");
                for (int i = 1; i <= pageCount; i += 6) {
                    log.info("开始转换文件:{}，第{}页到第{}页", one.getName(), i, Math.min(i + 5, pageCount));
                    String outputPath = FileUtil.file(output, fileNameWithoutExt + "_" + batchNum++ + ".docx").getAbsolutePath();
                    Convert.WordOutputOptions options = new Convert.WordOutputOptions();
                    options.setPages(i, Math.min(i + 5, pageCount));
                    Convert.toWord(sourcePath, outputPath, options);
                    tempFileList.add(new File(outputPath));
                    log.info("文件:{}，第{}页到第{}页转换成功", one.getName(), i, Math.min(i + 5, pageCount));
                }

                log.info("开始合并文件：{}", one.getName());
                mergeWordFiles(tempFileList, FileUtil.file(output, fileNameWithoutExt + ".docx"));
                deleteFiles(tempFileList);
                FileUtil.appendString(DateUtil.now() + "  文件：" + one.getName() + "，转换成功\n", logFile, Charset.defaultCharset());
                log.info("文件：{}，转换成功", one.getName());
            }


        } catch (Exception e) {
            log.error("转换失败：", e);
            FileUtil.appendString("转换失败：" + e.getMessage() + "\n", logFile, Charset.defaultCharset());
            return e.getMessage();
        }

        PDFNet.terminate();
        return output.getAbsolutePath();
    }


    public void mergeWordFiles(List<File> files, File outputFile) {
        final File headFile = files.get(0);
        Document document = new Document(headFile.getAbsolutePath());

        for (int i = 1; i < files.size(); i++) {
            document.insertTextFromFile(files.get(i).getAbsolutePath(), FileFormat.Docx);
        }
        document.saveToFile(outputFile.getAbsolutePath(), FileFormat.Docx);
    }

    private void deleteFiles(List<File> tempFileList) {
        for (File file : tempFileList) {
            FileUtil.del(file);
        }
    }
}
