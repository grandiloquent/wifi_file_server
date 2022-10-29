package main

import (
	"archive/zip"
	"encoding/json"
	"fmt"
	"io"
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

func staticFiles(w http.ResponseWriter, r *http.Request) bool {
	var filename string
	if r.URL.Path == "/" {
		filename = "index.html"
	}
	if m, _ := regexp.MatchString("\\.(?:js|css|svg|png|html|jpg)$", r.URL.Path); m {
		filename = r.URL.Path[1:]
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
func createDirectoryIfNotExists(p string) error {
	if _, err := os.Stat(p); os.IsNotExist(err) {
		err := os.MkdirAll(p, 0666)
		if err != nil {
			return err
		}
	}
	return nil
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
func checkFileExists(name string) bool {
	if _, err := os.Stat(name); os.IsNotExist(err) {
		return false
	}
	return true
}
func Unzip(src, dest string) error {
	r, err := zip.OpenReader(src)
	if err != nil {
		return err
	}
	defer func() {
		if err := r.Close(); err != nil {
			panic(err)
		}
	}()

	os.MkdirAll(dest, 0755)

	// Closure to address file descriptors issue with all the deferred .Close() methods
	extractAndWriteFile := func(f *zip.File) error {
		rc, err := f.Open()
		if err != nil {
			return err
		}
		defer func() {
			if err := rc.Close(); err != nil {
				panic(err)
			}
		}()

		path := filepath.Join(dest, f.Name)

		// Check for ZipSlip (Directory traversal)
		if !strings.HasPrefix(path, filepath.Clean(dest)+string(os.PathSeparator)) {
			return fmt.Errorf("illegal file path: %s", path)
		}

		if f.FileInfo().IsDir() {
			os.MkdirAll(path, f.Mode())
		} else {
			os.MkdirAll(filepath.Dir(path), f.Mode())
			f, err := os.OpenFile(path, os.O_WRONLY|os.O_CREATE|os.O_TRUNC, f.Mode())
			if err != nil {
				return err
			}
			defer func() {
				if err := f.Close(); err != nil {
					panic(err)
				}
			}()

			_, err = io.Copy(f, rc)
			if err != nil {
				return err
			}
		}
		return nil
	}

	for _, f := range r.File {
		err := extractAndWriteFile(f)
		if err != nil {
			return err
		}
	}

	return nil
}
