package com.zzsn.trans;

import java.io.File;

public interface TransService {
    /**
     * 转换传入的pdf文件列表，方法内不会校验文件是否合法，请在调用前自行校验
     *
     * @param fileList   pdf文件列表
     * @param outputPath 输出路径
     */
    void transPdfList(File[] fileList, String outputPath);
}
