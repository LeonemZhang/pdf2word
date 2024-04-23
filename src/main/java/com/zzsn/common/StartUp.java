package com.zzsn.common;

import com.pdftron.pdf.PDFNet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * @author 张宗涵
 * @date 2024/4/23
 */
@Component
public class StartUp implements CommandLineRunner {
    @Value("${apryse.license}")
    private String apryseLicense;

    @Value("${apryse.libPath}")
    private String apryseLibPath;

    @Override
    public void run(String... args) {
        PDFNet.initialize(apryseLicense);
        PDFNet.addResourceSearchPath(apryseLibPath);
    }

    @PreDestroy
    public void destroy() {
        PDFNet.terminate();
    }
}
