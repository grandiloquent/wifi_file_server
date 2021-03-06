# Wifi文件服务器

无需USB线，轻松传输手机数据。

通过Wifi在手机、电脑、平板等智能设备上共享文件。

该程序大部分使用C/C++语言编写，低耗能高效率

使用方法：

* **没有WIFI也可以使用**，打开手机热点，其他设备连接即可
* 拖拽文件到页面即可上传文件到手机
* 文件上传后存放的目录：`/storage/emulated/0/Download`
* 服务器开启后，好友可播放手机上的视频、音乐或图片，高速又便携

<img src="images/1.jpg" width="33.333%">
<img src="images/3.jpg" width="33.333%">

![](images/2.jpg)

## 下载

* [国内](https://lucidu.cn/article/jbqfmd)
* [GitHub](https://github.com/grandiloquent/wifi_file_server/releases)

## 限制

* 安卓新版本无法访问其他程序的数据，无法访问路径 `/storage/emulated/0/Android/data`
* 如开启 VPN 程序，通过局域网 IP 无法连接该程序

## 第三方

* https://github.com/yhirose/cpp-httplib
* https://github.com/ricmoo/QRCode