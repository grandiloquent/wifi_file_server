package shared

import (
	"bytes"
	"compress/flate"
	"compress/gzip"
	"crypto/tls"
	"fmt"
	"io"
	"io/ioutil"
	"net/http"
	"net/url"
	"time"
)

func TikTok(uri string, prxoy *url.URL) ([]byte, error) {
	// https://www.tiktok.com/@sonyakisa8/video/7156942763492166913?is_from_webapp=1&sender_device=pc&web_id=7146322967022372354
	uri = fmt.Sprintf("https://www.tikwm.com/api/?count=12&cursor=0&web=1&hd=1&url=%s", uri)
	result, err := Fetch(uri, nil, prxoy, func(r *http.Request) {
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
func Fetch(url string, buffer []byte, prxoy *url.URL, header func(r *http.Request)) ([]byte, error) {
	transport := &http.Transport{
		Proxy:               http.ProxyURL(prxoy),
		DisableCompression:  true,
		TLSHandshakeTimeout: 3 * time.Second,
		TLSClientConfig:     &tls.Config{InsecureSkipVerify: true},
	}
	client := &http.Client{
		Transport: transport,
		Timeout:   30 * time.Second,
	}
	var req *http.Request
	var err error
	if buffer == nil {
		req, err = http.NewRequest("GET", url, nil)
	} else {
		body := bytes.NewReader(buffer)
		req, err = http.NewRequest("POST", url, body)
	}
	if err != nil {
		return nil, err
	}
	req.Header.Set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36")
	req.Header.Set("Accept-Language", "zh-CN,zh;q=0.9")
	if header != nil {
		header(req)
	}
	res, err := client.Do(req)
	if err != nil || res == nil {
		return nil, err
	}
	defer res.Body.Close() // nolint

	var reader io.ReadCloser
	switch res.Header.Get("Content-Encoding") {
	case "gzip":
		reader, _ = gzip.NewReader(res.Body)
	case "deflate":
		reader = flate.NewReader(res.Body)
	default:
		reader = res.Body
	}
	defer reader.Close() // nolint
	body, err := ioutil.ReadAll(reader)
	if err != nil && err != io.EOF {
		return nil, err
	}
	return body, nil
}
func WriteJSON(w http.ResponseWriter, b []byte) {
	w.Header().Set("Content-Type", "application/json")
	w.Write(b)
}
