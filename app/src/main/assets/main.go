package main

import (
	"encoding/json"
	"io/ioutil"
	"net/http"
	"os"
	"path"
	"path/filepath"
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
	println(r.URL.Path)
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
	} else if r.URL.Path == "/api/delete" {
		var paths []string
		buf, err := ioutil.ReadAll(r.Body)
		if err != nil {
			http.NotFound(w, nil)
			return true
		}
		err = json.Unmarshal(buf, &paths)
		if err != nil {
			http.NotFound(w, nil)
			return true
		}
		for _, p := range paths {
			os.RemoveAll(p)
		}
		w.Write([]byte("Sucess"))
		return true
	} else if r.URL.Path == "/api/tidy" {
		p := r.URL.Query().Get("path")
		if len(p) == 0 {
			http.NotFound(w, nil)
			return true
		}
		fs, err := os.ReadDir(p)
		if err != nil {
			println(err.Error())
			http.NotFound(w, nil)
			return true
		}
		for _, f := range fs {
			if !f.IsDir() {
				ext := filepath.Ext(f.Name())

				if len(ext) == 0 {
					ext = ".unknown"
				}

				ext = strings.ToUpper(ext)

				ext = path.Join(p, ext)
				if _, err = os.Stat(ext); os.IsNotExist(err) {
					os.MkdirAll(ext, 0666)
				}
				os.Rename(path.Join(p, f.Name()), path.Join(ext, f.Name()))
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
