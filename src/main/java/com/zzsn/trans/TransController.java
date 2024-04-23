package com.zzsn.trans;

import com.zzsn.common.ResultVo;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class TransController {
    private final TransService transService;

    @Operation(summary = "本地转换", description = "传入pdf文件所在的上层目录的绝对路径，如D:/pdf/test.pdf，传入D:/pdf即可")
    @GetMapping("/transFromLocal")
    public ResultVo<Void> path(@RequestParam("path") String path) {
        return transService.transFromLocal(path);
    }
}
