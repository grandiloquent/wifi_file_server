package shared

import (
	"../brotli"
	"bytes"
	"compress/flate"
	"compress/gzip"
	"crypto/tls"
	"io"
	"io/ioutil"
	"net/http"
	"net/url"
	"regexp"
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
	case "br": // go get https://github.com/andybalholm/brotli
		reader = brotli.NewReader(res.Body)
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
func KeyString(buf []byte, key string) string {
	re := regexp.MustCompile("\"" + key + "\"")
	loc := re.FindIndex(buf)
	if len(loc) == 0 {
		return ""
	}
	if loc[0] > 0 && buf[loc[0]-1] != '\\' {
		buf = buf[loc[1]:]
		r := regexp.MustCompile(":\\s*?null\\s*?[,\\]}]")
		loc = r.FindIndex(buf)
		if len(loc) > 0 && loc[0] == 0 {

			return ""
		}

		r = regexp.MustCompile(":\\s*?\"(.*?[^\\\\])\"\\s*?[,\\]}]")
		loc = r.FindSubmatchIndex(buf)

		if len(loc) == 0 || loc[0] != 0 {
			return ""
		}
		return string(buf[loc[2]:loc[3]])
	}
	return ""
	//for i := 0; i < len(buf); i++ {
	//	if buf[i] == '"' {
	//		if i > 0 && buf[i-1] == '\\' {
	//			continue
	//		} else {
	//			if bytes.HasPrefix(buf[i+1:], []byte(key)) && buf[i+1+len(key)] == '"' {
	//				buf = buf[i+1+len(key)+1:]
	//				break
	//			} else {
	//				buf = buf[i:]
	//			}
	//		}
	//	}
	//}
	//
	//var start = -1
	//
	//for i := 0; i < len(buf); i++ {
	//	if buf[i] == '"' {
	//		if i > 0 && buf[i-1] == '\\' {
	//			continue
	//		} else {
	//			start = i
	//			break
	//		}
	//	}
	//}
	//if start == -1 {
	//	return ""
	//}
	//var end = -1
	//for i := start + 1; i < len(buf); i++ {
	//	if buf[i] == '"' {
	//		if i > 0 && buf[i-1] == '\\' {
	//			continue
	//		} else {
	//			end = i
	//			break
	//		}
	//	}
	//}
	//if end == -1 {
	//	return ""
	//}
	//return string(buf[start+1 : end])
}
func WriteFile(filename string, data []byte) {
	ioutil.WriteFile(filename, data, 0666)
}

func SplitLastItem(s string) string {
	lines := regexp.MustCompile(`"(, ")*`).Split(s, -1)
	for i := len(lines) - 1; i > -1; i-- {
		if len(strings.TrimSpace(lines[i])) > 0 {
			return lines[i]
		}
	}
	return ""
}
