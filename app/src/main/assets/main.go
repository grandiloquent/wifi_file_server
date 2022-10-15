package main

import (
	"encoding/json"
	"net/http"
	"os"
	"strings"
)

func main() {
	_ = http.ListenAndServe(":8089", http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if staticFiles(w, r) {
			return
		}
		if listFiles(w, r) {
			return
		}
		http.NotFound(w, r)
	}))
}

func listFiles(w http.ResponseWriter, r *http.Request) bool {

	if r.URL.Path == "/api/files" {

		p := r.URL.Query().Get("path")
		if len(p) == 0 {
			p = "C:/Users/Administrator/Desktop"
		}
		if r.URL.Query().Get("isDir") == "0" {
			http.ServeFile(w, r, p)
			return true
		}
		entries, err := os.ReadDir(p)
		if err != nil {
			http.NotFound(w, r)
		} else {
			var array []map[string]interface{}
			for i := 0; i < len(entries); i++ {
				obj := make(map[string]interface{})
				obj["parent"] = p
				obj["name"] = entries[i].Name()
				obj["isDir"] = entries[i].IsDir()
				f, _ := entries[i].Info()
				obj["length"] = f.Size()
				array = append(array, obj)
			}

			b, err := json.Marshal(array)
			if err != nil {
				http.NotFound(w, r)
			} else {
				w.Write(b)
			}
		}
		return true
	}
	return false
}

func staticFiles(w http.ResponseWriter, r *http.Request) bool {
	var filename string
	//var mimeType string
	if r.URL.Path == "/" {
		filename = "index.html"
		//	mimeType = "text/html"
	}
	if strings.HasSuffix(r.URL.Path, ".js") || strings.HasSuffix(r.URL.Path, ".svg") {
		filename = r.URL.Path[1:]
	}
	if len(filename) > 0 {
		http.ServeFile(w, r, filename)
		return true
	}
	return false
}
