package com.zzsn.trans;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.pdftron.pdf.Convert;
import com.pdftron.pdf.PDFDoc;
import com.spire.doc.Document;
import com.spire.doc.FileFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TransServiceImpl implements TransService {
    @Resource(name = "threadPoolTaskExecutor")
    private ThreadPoolTaskExecutor threadPool;

    @Override
    public void transPdfList(File[] fileList, String outputPath) {
        final TimeInterval timer = new TimeInterval();
        final File logFile = FileUtil.touch(FileUtil.file(outputPath, "任务日志.txt"));

        threadPool.submit(() -> {
            try {
                for (final File one : fileList) {
                    timer.start();
                    log.info("开始转换文件：{}", one.getName());
                    FileUtil.appendString(DateUtil.now() + "  开始转换文件：" + one.getName() + "\n", logFile, Charset.defaultCharset());
                    final String sourceFileAbsPath = one.getAbsolutePath();
                    final String fileNameWithoutExt = one.getName().replace(".pdf", "");

                    final int pageCount;
                    try (PDFDoc doc = new PDFDoc(sourceFileAbsPath)) {
                        pageCount = doc.getPageCount();
                    }

                    int batchNum = 1;
                    List<File> tempFileList = new ArrayList<>();
                    for (int i = 1; i <= pageCount; i += 6) {
                        final File tempOutputFile = FileUtil.file(outputPath, StrUtil.format("{}_{}.docx", fileNameWithoutExt, batchNum++));
                        Convert.WordOutputOptions options = new Convert.WordOutputOptions();
                        options.setPages(i, Math.min(i + 5, pageCount));
                        Convert.toWord(sourceFileAbsPath, tempOutputFile.getAbsolutePath(), options);
                        tempFileList.add(tempOutputFile);
                        log.info("文件:{}，第{}页到第{}页转换成功", one.getName(), i, Math.min(i + 5, pageCount));
                    }

                    log.info("开始合并文件：{}", one.getName());
                    mergeWordFiles(tempFileList, FileUtil.file(outputPath, fileNameWithoutExt + ".docx"));
                    deleteFiles(tempFileList);
                    FileUtil.appendString(StrUtil.format("{}  文件：{}，转换成功，耗时：{}\n\n", DateUtil.now(), one.getName(), timer.intervalPretty()), logFile, Charset.defaultCharset());
                    log.info("文件：{}，转换成功，耗时：{}", one.getName(), timer.intervalPretty());
                }
                log.info("全部文件转换完成，任务结束");
                FileUtil.appendString("全部文件转换完成，任务结束\n", logFile, Charset.defaultCharset());
            } catch (Exception e) {
                log.error("转换失败：", e);
                FileUtil.appendString("转换失败：" + e.getMessage() + "\n", logFile, Charset.defaultCharset());
            }
        });
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
