package shared

import (
	"bytes"
	"compress/flate"
	"compress/gzip"
	"crypto/tls"
	"io"
	"io/ioutil"
	"net/http"
	"net/url"
	"strings"
	"time"
)

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
func Substring(s, start, end string) string {

	p := strings.Index(s, start)
	if p == -1 {
		return ""
	}
	t := s[p+len(start):]
	p = strings.IndexAny(t, end)
	if p == -1 {
		return ""
	}
	return t[:p]
}
func SubstringAfter(original string, substr string) string {

	index := strings.Index(original, substr)
	if index == -1 {
		return original
	}

	return original[index+len(substr):]
}
func SubstringAfterLast(original string, substr string) string {

	index := strings.LastIndex(original, substr)
	if index == -1 {
		return original
	}

	return original[index+len(substr):]
}
func SubstringBefore(original string, substr string) string {

	index := strings.Index(original, substr)
	if index == -1 {
		return original
	}

	return original[:index]
}
func SubstringBeforeLast(original string, substr string) string {

	index := strings.LastIndex(original, substr)
	if index == -1 {
		return original
	}

	return original[:index]
}
func SubstringBytes(s, start, end []byte) []byte {

	p := bytes.Index(s, start)
	if p == -1 {
		return nil
	}
	t := s[p+len(start):]
	p = bytes.Index(t, end)
	if p == -1 {
		return nil
	}
	return t[:p]
}
