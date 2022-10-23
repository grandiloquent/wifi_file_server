package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"net/url"
	"os"
	"path"
	"path/filepath"
	"regexp"
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
			w.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename="%s"`, path.Base(p)))
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
		err := moveFiles(p)
		if err != nil {
			return false
		}
		return true
	}
	return false
}

func staticFiles(w http.ResponseWriter, r *http.Request) bool {
	var filename string
	if r.URL.Path == "/" {
		filename = "index.html"
	}
	if m, _ := regexp.MatchString("\\.(?:js|css|svg|png)$", r.URL.Path); m {
		filename = r.URL.Path[1:]
	}
	referer := r.Header.Get("Referer")
	if len(referer) > 0 && strings.Contains(referer, ".html&") {
		u, err := url.Parse(referer)
		if err == nil && len(filename) > 3 {
			filename = path.Dir(u.Query().Get("path")) + "/" + filename[3:]
		}
	}
	if len(filename) > 0 {
		fmt.Println(filename)
		http.ServeFile(w, r, filename)
		return true
	}
	return false
}
func moveFiles(p string) error {
	fs, err := os.ReadDir(p)
	if err != nil {
		return err
	}
	for _, f := range fs {
		if !f.IsDir() {
			ext := filepath.Ext(f.Name())

			if len(ext) == 0 {
				ext = ".unknown"
			}

			ext = strings.ToUpper(ext)

			ext = path.Join(p, ext)
			err := createDirectoryIfNotExists(ext)
			if err != nil {
				return err
			}
			err = os.Rename(path.Join(p, f.Name()), path.Join(ext, f.Name()))
			if err != nil {
				return err
			}
		}
	}
	return nil
}
func createDirectoryIfNotExists(p string) error {
	if _, err := os.Stat(p); os.IsNotExist(err) {
		err := os.MkdirAll(p, 0666)
		if err != nil {
			return err
		}
	}
	return nil
}
