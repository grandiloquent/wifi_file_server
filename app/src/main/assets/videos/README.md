## 运行

http://localhost:8089

```shell
$env:GO111MODULE="auto";go run main.go
```

## 代码

```javascript
(() => {
    const strings = `Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9
Accept-Encoding: gzip, deflate, br
Accept-Language: en
Cache-Control: no-cache
Connection: keep-alive
Cookie: session_token=40f854299f21048auyJuQXvAFWmzAvY4qqURWFYhASNg9PF2EJ3rHv2fzeYEBL525e5ZNb-41tIeKg0f3Z7SkY59SKPuGRmcUb0nBERK6MqtyuM7NIKfdhF3SBAIjWEAeCL2FTGg9U0uodrce3ceaWiFdzFmgoq1LWtUqCR3bzNZF1_ZAk9AwPOOa2bxW3c-B7Zaql2R5gwaH6nb; last_views=%5B%2273139749-1667574082%22%5D; session_ath=black; html5_pref=%7B%22SQ%22%3Afalse%2C%22MUTE%22%3Afalse%2C%22VOLUME%22%3A1%2C%22FORCENOPICTURE%22%3Afalse%2C%22FORCENOAUTOBUFFER%22%3Afalse%2C%22FORCENATIVEHLS%22%3Afalse%2C%22PLAUTOPLAY%22%3Atrue%2C%22CHROMECAST%22%3Afalse%2C%22EXPANDED%22%3Afalse%2C%22FORCENOLOOP%22%3Afalse%7D
Host: www.xvideos.com
Pragma: no-cache
sec-ch-ua: "Google Chrome";v="95", "Chromium";v="95", ";Not A Brand";v="99"
sec-ch-ua-mobile: ?0
sec-ch-ua-platform: "Windows"
Sec-Fetch-Dest: document
Sec-Fetch-Mode: navigate
Sec-Fetch-Site: none
Sec-Fetch-User: ?1
Upgrade-Insecure-Requests: 1
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36`;
    const lines = strings.split('\n');
    const buf = [];
    for (const line of lines) {
        const index = line.indexOf(':');
        buf.push(`r.Header.Set("${line.substring(0, index).trim()}","${line.substring(index + 1).trim().replaceAll('"', '\\"')}");`)
    }
    console.log(buf.join('\n'))
})()
```

## 版本控制

```shell
git add .; git commit -m "更新" ; git push
```

## Github

- https://github.com/iawia002/lux
- https://github.com/kkdai/youtube
- https://github.com/wxbool/video-srt-windows