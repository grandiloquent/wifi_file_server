package main

import (
	"net/http"
	"net/url"
	"regexp"

	"./shared"
)

const SupportedFileTypes = "\\.(?:js|html)"

func main() {
	shared.Twitter("https://twitter.com/i/status/1584276538530627584", getProxy())
	_ = http.ListenAndServe(":8089", http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.URL.Path == "/" || r.URL.Path == "" {
			http.ServeFile(w, r, "index.html")
			return
		}
		if b, _ := regexp.MatchString(SupportedFileTypes, r.URL.Path); b {
			http.ServeFile(w, r, r.URL.Path[1:])
			return
		}
		if downloadVideo(w, r) {
			return
		}
		http.NotFound(w, r)
	}))
}

func downloadVideo(w http.ResponseWriter, r *http.Request) bool {
	if r.URL.Path != "/api/videos" {
		return false
	}
	q := r.URL.Query().Get("q")
	if len(q) == 0 {
		http.NotFound(w, r)
		return true
	}
	if tryTikTok(w, q) {
		return true
	}
	if tryXVideos(w, q) {
		return true
	}
	if tryTwitter(w, q) {
		return true
	}
	return true
}

func tryTikTok(w http.ResponseWriter, q string) bool {
	if !shared.IsTikTokUri(q) {
		return false
	}
	b, err := shared.TikTok(q, getProxy())
	if err != nil {
		http.NotFound(w, nil)
		return true
	}
	shared.WriteJSON(w, b)
	return true
}

func tryXVideos(w http.ResponseWriter, q string) bool {
	if !shared.IsXVideosUri(q) {
		return false
	}
	b, err := shared.XVideos(q, getProxy())
	if err != nil {
		http.NotFound(w, nil)
		return true
	}
	shared.WriteJSON(w, b)
	return true
}

func tryTwitter(w http.ResponseWriter, q string) bool {
	if !shared.IsTwitterUri(q) {
		return false
	}
	b, err := shared.Twitter(q, getProxy())
	if err != nil {
		http.NotFound(w, nil)
		return true
	}
	shared.WriteJSON(w, b)
	return true
}

func getProxy() *url.URL {
	proxy, _ := url.Parse("http://127.0.0.1:10809")
	return proxy
}
