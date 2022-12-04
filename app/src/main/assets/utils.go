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

func listDirectory(fullname string) ([]map[string]interface{}, error) {
	entries, err := os.ReadDir(fullname)
	if err != nil {
		return nil, err
	}
	var array []map[string]interface{}
	for i := 0; i < len(entries); i++ {
		obj := make(map[string]interface{})
		obj["parent"] = fullname
		obj["name"] = entries[i].Name()
		obj["isDir"] = entries[i].IsDir()
		f, _ := entries[i].Info()
		obj["length"] = f.Size()
		array = append(array, obj)
	}
	return array, nil
}

func deleteFiles(w http.ResponseWriter, r *http.Request) bool {
	if r.URL.Path != "/api/delete" {
		return false
	}
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
	w.Write([]byte("Success"))
	return true
}
func listFiles(w http.ResponseWriter, r *http.Request) bool {
	if r.URL.Path == "/api/files" {
		p := r.URL.Query().Get("path")
		if len(p) == 0 {
			p = "C:/Users/Administrator/Desktop"
		}
		if r.URL.Query().Get("isDir") == "0" {
			if strings.HasSuffix(p, ".html") {
				b, err := ioutil.ReadFile(p)
				if err != nil {
					http.NotFound(w, r)
					return true
				}
				r.Header.Set("Content-Type", "text/html")
				w.Write(b)
			} else {
				w.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename="%s"`, path.Base(p)))
				http.ServeFile(w, r, p)
			}
			return true
		}
		data, err := listDirectory(p)
		if err != nil {
			http.Error(w, err.Error(), 500)
			return true
		}
		writeJSON(w, data)

		return true
	} else if r.URL.Path == "/api/tidy" {
		p := r.URL.Query().Get("path")
		if len(p) == 0 {
			http.NotFound(w, nil)
			return true
		}
		err := tidyFiles(p)
		if err != nil {
			return false
		}
		return true
	}
	return false
}
func moveFiles(w http.ResponseWriter, r *http.Request) bool {
	if r.URL.Path != "/api/move" {
		return false
	}
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
	parent := r.URL.Query().Get("dst")
	for _, p := range paths {
		os.Rename(p, path.Join(parent, path.Base(p)))
	}
	w.Write([]byte("Success"))
	return true
}
func newFile(w http.ResponseWriter, r *http.Request) bool {
	if r.URL.Path != "/api/newfile" {
		return false
	}
	src := r.URL.Query().Get("src")
	if _, err := os.Stat(src); os.IsNotExist(err) {
		w.Header().Set("Content-Type", "text/plain; charset=utf-8")
		w.WriteHeader(404)
		fmt.Fprintf(w, "%s", src)
		return true
	}
	dst := r.URL.Query().Get("dst")
	dst = path.Join(src, dst)
	if _, err := os.Stat(dst); os.IsNotExist(err) {
		f, err := os.Create(dst)
		if err != nil {
			w.Header().Set("Content-Type", "text/plain; charset=utf-8")
			w.WriteHeader(404)
			fmt.Fprintf(w, "%s", src)
			return true
		}
		defer f.Close()
		w.Write([]byte("Success"))
		return true
	}
	w.Header().Set("Content-Type", "text/plain; charset=utf-8")
	w.WriteHeader(404)
	fmt.Fprintf(w, "%s", src)
	return true
}
func newFolder(w http.ResponseWriter, r *http.Request) bool {
	if r.URL.Path != "/api/newfolder" {
		return false
	}
	src := r.URL.Query().Get("src")
	if _, err := os.Stat(src); os.IsNotExist(err) {
		w.Header().Set("Content-Type", "text/plain; charset=utf-8")
		w.WriteHeader(404)
		fmt.Fprintf(w, "%s", src)
		return true
	}
	dst := r.URL.Query().Get("dst")
	dst = path.Join(src, dst)
	if _, err := os.Stat(dst); os.IsNotExist(err) {
		os.MkdirAll(dst, 0777)
		w.Write([]byte("Success"))
		return true
	}
	w.Header().Set("Content-Type", "text/plain; charset=utf-8")
	w.WriteHeader(404)
	fmt.Fprintf(w, "%s", src)
	return true
}
func renameFile(w http.ResponseWriter, r *http.Request) bool {
	if r.URL.Path != "/api/rename" {
		return false
	}
	src := r.URL.Query().Get("src")
	if _, err := os.Stat(src); os.IsNotExist(err) {
		w.Header().Set("Content-Type", "text/plain; charset=utf-8")
		w.WriteHeader(404)
		fmt.Fprintf(w, "%s", src)
		return true
	}
	dst := r.URL.Query().Get("dst")
	parent := path.Dir(src)
	dst = path.Join(parent, dst)
	if _, err := os.Stat(dst); os.IsNotExist(err) {
		os.Rename(src, dst)
		w.Write([]byte("Success"))
		return true
	}
	w.Header().Set("Content-Type", "text/plain; charset=utf-8")
	w.WriteHeader(404)
	fmt.Fprintf(w, "%s", src)
	return true
}
func staticFiles(w http.ResponseWriter, r *http.Request) bool {
	var filename string
	if r.URL.Path == "/" {
		filename = "files/index.html"
	}
	if strings.HasSuffix(r.URL.Path, "svg") {
		filename = r.URL.Path[1:]
	} else if m, _ := regexp.MatchString("\\.(?:js|css|png|html|jpg)$", r.URL.Path); m {
		filename = "files/" + r.URL.Path[1:]
	}
	if len(filename) > 0 {
		if !checkFileExists(filename) {
			referer := r.Header.Get("Referer")
			if len(referer) > 0 {
				u, err := url.Parse(referer)
				if err == nil && len(filename) > 3 {
					filename = path.Dir(u.Query().Get("path")) + "/" + filename[3:]
				}
			}
		}
		http.ServeFile(w, r, filename)
		return true
	}
	return false
}
func tidyFiles(p string) error {
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
