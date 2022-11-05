package shared

import (
	"encoding/json"
	"fmt"
	"net/http"
	"net/url"
	"strings"
)

func Eporner(uri string, proxy *url.URL) ([]byte, error) {
	b, err := getEpornerPage(uri, proxy)
	if err != nil {
		return nil, err
	}
	obj := make(map[string]interface{})

	title := SubstringBytes(b, []byte("\"name\": \""), []byte("\","))
	obj["title"] = string(title)
	obj["url"] = uri
	cover := SubstringBytes(b, []byte("\"thumbnailUrl\":"), []byte("]"))
	obj["cover"] = SplitLastItem(string(cover))
	play := SubstringBytes(b, []byte("<div class=\"dloaddivcol\">"), []byte("</div>"))
	temporary := SubstringAfterLast(string(play), "<a href=\"")
	temporary = SubstringBefore(temporary, "\"")
	obj["play"] = fmt.Sprintf("%s.com%s", SubstringBefore(uri, ".com/"), temporary)
	result, err := json.Marshal(obj)
	if err != nil {
		return nil, err
	}
	return result, nil
}

func getEpornerPage(uri string, proxy *url.URL) ([]byte, error) {
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

// https://www.eporner.com/video-xzZGrFuOJ2y/can-i-suddenly-fuck-you-at-home-decensored/
func IsEpornerUri(u string) bool {
	return strings.HasPrefix(u, "https://www.eporner.com")
}

/*

func tryEporner(w http.ResponseWriter, q string) bool {
	if !shared.IsEpornerUri(q) {
		return false
	}
	b, err := shared.Eporner(q, getProxy())
	if err != nil {
		http.NotFound(w, nil)
		return true
	}
	shared.WriteJSON(w, b)
	return true
}
if tryEporner(w, q) {
		return true
	}
*/
