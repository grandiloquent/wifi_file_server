package main

import (
	"net/http"
	"net/url"
	"regexp"

	"./shared"
)

const SupportedFileTypes = "\\.(?:js|html)"

func main() {
	shared.XVideos("https://www.xvideos.com/video73139749/pretty_girl_having_great_fuck_from_her_boyfriend", getProxy())
	// http://localhost:8080
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
func getProxy() *url.URL {
	proxy, _ := url.Parse("http://127.0.0.1:10809")
	return proxy
}

// $env:GO111MODULE="auto";go run main.go
