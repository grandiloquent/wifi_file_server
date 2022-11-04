package shared

import (
	"fmt"
	"net/http"
	"net/url"
	"strings"
)

func TikTok(uri string, proxy *url.URL) ([]byte, error) {
	// https://www.tiktok.com/@sonyakisa8/video/7156942763492166913?is_from_webapp=1&sender_device=pc&web_id=7146322967022372354
	uri = fmt.Sprintf("https://www.tikwm.com/api/?count=12&cursor=0&web=1&hd=1&url=%s", uri)
	result, err := Fetch(uri, nil, proxy, func(r *http.Request) {
		r.Header.Set("Accept", "application/json, text/javascript, */*; q=0.01")
		r.Header.Set("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
		r.Header.Set("sec-ch-ua", "\"Chromium\";v=\"104\", \" Not A;Brand\";v=\"99\", \"Google Chrome\";v=\"104\"")
		r.Header.Set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36")
	})
	if err != nil {
		return nil, err
	}
	return result, nil
}

// https://www.tiktok.com/@ryanbakery/video/7162083541365312795?is_from_webapp=1&sender_device=pc&web_id=7146322967022372354
func IsTikTokUri(u string) bool {
	fmt.Println(u, strings.HasPrefix("https://www.tiktok.com", u))
	return strings.HasPrefix(u, "https://www.tiktok.com")
}
