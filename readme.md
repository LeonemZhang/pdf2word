# 运行说明

默认只能运行在windows环境，因为apryse需要本地组件，项目里只有windows的本地工具。

使用时先修改application.yml，修改apryse的license和libPath。

license需要自己去 [apryse官网](https://docs.apryse.com/documentation/java/get-started/integration/windows/)
申请一个。

libPath 项目里自带了一个，请修改为自己`resource/lib`
所在位置的绝对路径。也可以从 [apryse 结构化输出模块](https://docs.apryse.com/documentation/java/info/modules/#structured-output-module)
下载一个，从而运行在其他系统上

<br>

# 使用说明

使用swagger触发任务运行。打开localhost:8080/doc.html。调试接口传入pdf文件所在上层目录的绝对路径。程序会自动处理该目录下的所有pdf文件（不会处理子目录）。

# 项目说明

调研了很多pdf转docx的库，最终选择了apryse.pdf。原因是apryse.pdf转换的docx文件格式最接近原pdf文件。

pdf文件转docx使用apryse.pdf库
https://docs.apryse.com/documentation/java/

docx文件合并使用spire.doc库
https://www.e-iceblue.cn/Introduce/Spire-Doc-JAVA.html

apryse需要自己申请license，一个试用license的有效期是40天。试用license每次只能转换6页pdf。

所以需要spire.doc来合并docx文件。spire.doc用的是免费版，目前处理过的最大pdf有50MB，有250页左右，还没有遇到过问题。

