package com.zzsn.trans;

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
import com.zzsn.common.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class TransServiceImpl implements TransService {
    @Value("${apryse.license}")
    private String apryseLicense;

    @Value("${apryse.libPath}")
    private String apryseLibPath;

    @Override
    public ResultVo<Void> transFromLocal(String localPath) {
        File output;
        File logFile = null;
        try {
            output = FileUtil.file(localPath, DateUtil.format(DateUtil.date(), "yyyy-MM-dd_HH-mm-ss"));
            FileUtil.mkdir(output);
            logFile = FileUtil.touch(FileUtil.file(output, "任务日志.txt"));

            File file = new File(localPath);
            Assert.isTrue(file.exists(), "目录不存在");
            final File[] files = file.listFiles();
            Assert.notNull(files, "目录下不存在文件");
            Assert.isTrue(CollectionUtil.isNotEmpty(Arrays.asList(files)), "目录下不存在文件");

            PDFNet.initialize(apryseLicense);
            PDFNet.addResourceSearchPath(apryseLibPath);

            if (!StructuredOutputModule.isModuleAvailable()) {
                FileUtil.appendString("pdf转换word模块不可用，任务失败", logFile, Charset.defaultCharset());
                log.error("pdf转换word模块不可用");
                return ResultVo.ofFailure("pdf转换word模块不可用");
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
            return ResultVo.ofFailure(e.getMessage());
        }

        PDFNet.terminate();
        return ResultVo.ofSuccessMsg("转换成功，输出目录为：" + output.getAbsolutePath());
    }

    /**
     * 用spire.doc库合并word文件
     *
     * @param files      需要合并的文件列表
     * @param outputFile 合并后的文件
     */
    private void mergeWordFiles(List<File> files, File outputFile) {
        final File headFile = files.get(0);
        Document deadDocument = new Document(headFile.getAbsolutePath());

        for (int i = 1; i < files.size(); i++) {
            deadDocument.insertTextFromFile(files.get(i).getAbsolutePath(), FileFormat.Docx);
        }
        deadDocument.saveToFile(outputFile.getAbsolutePath(), FileFormat.Docx);
    }

    /**
     * 删除临时文件
     *
     * @param tempFileList 临时文件列表
     */
    private void deleteFiles(List<File> tempFileList) {
        for (File file : tempFileList) {
            FileUtil.del(file);
        }
    }
}