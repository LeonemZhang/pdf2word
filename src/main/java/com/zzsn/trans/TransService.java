package com.zzsn.trans;

import com.zzsn.common.ResultVo;

public interface TransService {
    ResultVo<Void> transFromLocal(String localPath);
}
