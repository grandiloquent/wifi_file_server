package main

import (
	"net/http"
)

func main() {
	_ = http.ListenAndServe(":8000", http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if staticFiles(w, r) {
			return
		}
		if listFiles(w, r) {
			return
		}
		if deleteFiles(w, r) {
			return
		}
		if moveFiles(w, r) {
			return
		}
		if renameFile(w, r) {
			return
		}
		if newFolder(w, r) {
			return
		}
		if newFile(w, r) {
			return
		}
		if unzip(w, r) {
			return
		}
		if r.URL.Path == "/api/save" {
			saveHandler(w, r)
			return
		}
		http.NotFound(w, r)
	}))
}

//  go run main.go shared.go utils.go srt.go
