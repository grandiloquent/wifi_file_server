package main

import (
	"net/http"
	"net/url"
	"os/exec"
	"strings"

	"./shared"
)

func main() {
	println(exec.Command("C:/Program Files/Google/Chrome/Application/chrome.exe", "http://localhost:8080").Run())
	_ = http.ListenAndServe(":8080", http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.URL.Path == "/" || r.URL.Path == "" {
			http.ServeFile(w, r, "index.html")
			return
		}
		println(r.URL.Path)
		if strings.HasSuffix(r.URL.Path, ".js") {
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
	proxy, _ := url.Parse("http://127.0.0.1:10809")
	b, err := shared.TikTok(q, proxy)
	if err != nil {
		http.NotFound(w, r)
		return true
	}
	shared.WriteJSON(w, b)
	return true
}

// $env:GO111MODULE="auto";go run main.go
