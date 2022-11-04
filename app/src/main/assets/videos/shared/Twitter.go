package shared

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"net/url"
	"regexp"
	"strings"
)

func Twitter(uri string, proxy *url.URL) ([]byte, error) {
	id := extractKeyString(uri)
	b, err := getTwitterPage(fmt.Sprintf("https://api.twitter.com/1.1/videos/tweet/config/%s.json", id), proxy)
	if err != nil {
		return nil, err
	}
	ioutil.WriteFile("1.json", b, 0666)
	obj := make(map[string]interface{})

	obj["title"] = id
	obj["url"] = uri
	cover := KeyString(b, "posterImage")
	obj["cover"] = string(cover)
	play := KeyString(b, "playbackUrl")
	obj["play"] = string(play)
	result, err := json.Marshal(obj)
	if err != nil {
		return nil, err
	}
	//fmt.Println(obj)
	//var result []byte
	return result, nil
}

func getTwitterPage(uri string, proxy *url.URL) ([]byte, error) {
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
		r.Header.Set("Authorization", "Bearer AAAAAAAAAAAAAAAAAAAAAIK1zgAAAAAA2tUWuhGZ2JceoId5GwYWU5GspY4%3DUq7gzFoCZs1QfwGoVdvSac3IniczZEYXIcDyumCauIXpcAPorE")
	})
	if err != nil {
		return nil, err
	}
	return result, nil
}
func extractKeyString(uri string) string {
	ret := regexp.MustCompile("(status|statuses)/(\\d+)").FindStringSubmatch(uri)
	return ret[2]
}

// https://twitter.com/i/status/1584276538530627584
func IsTwitterUri(u string) bool {
	return strings.HasPrefix("https://twitter.com/", u)
}
