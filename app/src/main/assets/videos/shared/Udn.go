package shared

import (
	"encoding/json"
	"fmt"
	"net/http"
	"net/url"
	"strings"
)

func Udn(uri string, proxy *url.URL) ([]byte, error) {
	uri = fmt.Sprintf("https://video.udn.com/embed/news/%s", SubstringAfterLast(uri, "/"))
	b, err := getUdnPage(uri, proxy)
	if err != nil {
		return nil, err
	}
	obj := make(map[string]interface{})

	title := SubstringBytes(b, []byte("<title>"), []byte("</title>"))
	obj["title"] = string(title)
	obj["url"] = uri
	cover := SubstringBytes(b, []byte("poster: '"), []byte("'"))
	obj["cover"] = string(cover)
	//play := SubstringBytes(b, []byte("setVideoUrlHigh('"), []byte("');"))
	//obj["play"] = string(play)
	cdn := fmt.Sprintf("http%s", string(SubstringBytes(b, []byte("mp4: '"), []byte("'"))))
	println(cdn)
	b, err = Fetch(cdn, nil, proxy, func(r *http.Request) {
		r.Header.Set("Referer", uri)
	})
	WriteFile("5.json", b)
	result, err := json.Marshal(obj)
	if err != nil {
		return nil, err
	}
	return result, nil
}

func getUdnPage(uri string, proxy *url.URL) ([]byte, error) {
	result, err := Fetch(uri, nil, proxy, func(r *http.Request) {
		r.Header.Set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		r.Header.Set("Accept-Encoding", "gzip, deflate, br")
		r.Header.Set("Accept-Language", "en")
		r.Header.Set("Cache-Control", "no-cache")
		r.Header.Set("Connection", "keep-alive")
		r.Header.Set("Pragma", "no-cache")
		r.Header.Set("sec-ch-ua", "\"Google Chrome\";v=\"95\", \"Chromium\";v=\"95\", \";Not A Brand\";v=\"99\"")
		r.Header.Set("sec-ch-ua-mobile", "?0")
		r.Header.Set("sec-ch-ua-platform", "\"Windows\"")
		r.Header.Set("Sec-Fetch-Dest", "document")
		r.Header.Set("Sec-Fetch-Mode", "navigate")
		r.Header.Set("Sec-Fetch-Site", "none")
		r.Header.Set("Sec-Fetch-User", "?1")
		r.Header.Set("Upgrade-Insecure-Requests", "1")
		r.Header.Set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36")
	})
	if err != nil {
		return nil, err
	}
	return result, nil
}

// https://video.udn.com/news/1251480
func IsUdnUri(u string) bool {
	return strings.HasPrefix(u, "https://video.udn.com")
}

/*

func tryUdn(w http.ResponseWriter, q string) bool {
	if !shared.IsUdnUri(q) {
		return false
	}
	b, err := shared.Udn(q, getProxy())
	if err != nil {
		http.NotFound(w, nil)
		return true
	}
	shared.WriteJSON(w, b)
	return true
}
if tryUdn(w, q) {
		return true
	}
*/
