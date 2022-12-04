package main

import (
	"archive/zip"
	"bufio"
	"bytes"
	"encoding/json"
	"errors"
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

	"golang.org/x/net/html"
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
		http.NotFound(w, r)
	}))
}

func checkFileExists(name string) bool {
	if _, err := os.Stat(name); os.IsNotExist(err) {
		return false
	}
	return true
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
			writeError(w, err)
		} else {
			writeJSON(w, data)
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
func SrtScanner(data []byte, atEOF bool) (advance int, token []byte, err error) {
	for i := 0; i < len(data); i++ {
		if i < len(data)-1 && string(data[i:i+2]) == "\n\n" {
			return i + 2, data[:i+2], nil
		}
		if i < len(data)-3 && string(data[i:i+4]) == "\r\n\r\n" {
			return i + 4, data[:i+4], nil
		}
	}
	if atEOF && len(data) != 0 {
		return len(data), data, nil
	}
	// Golang v1.6
	// There is one final token to be delivered, which may be the empty string.
	// Returning bufio.ErrFinalToken here tells Scan there are no more tokens after this
	// but does not trigger an error to be returned from Scan itself.
	//return 0, data, fmt.Errorf("bufio.ErrFinalToken")
	return 0, nil, nil
}

func ConvertTimeToWebVtt(t string) (string, error) {
	t = strings.Replace(t, ",", ".", -1)
	timing := strings.Split(t, " --> ")
	for i, v := range timing {
		if len(v) < 3 {
			return "", errors.New("Invalid time line")
		}
		if v[:3] == "00:" {
			timing[i] = v[3:]
		}
	}
	return strings.Join(timing, " --> "), nil
}

func SrtToWebVtt(l string) (string, error) {
	l = strings.Replace(l, "\r\n", "\n", -1)
	lines := strings.SplitN(l, "\n", 3)
	if len(lines) < 2 {
		return "", errors.New("Invalid line format")
	}
	var err error
	lines = lines[1:]
	lines[0], err = ConvertTimeToWebVtt(lines[0])
	if err != nil {
		return "", err
	}
	lines[1], err = cleanTags(lines[1])
	if err != nil {
		return lines[1], err
	}
	return fmt.Sprintf("%s\n%s", lines[0], lines[1]), nil
}

// cleanTags replaces invalid tags with <b> and encodes characters such &
// into html entities (e.g. &amp;) to avoid conflicts with VTT annotations.
// TODO: Parse font tags and replace them with Cue STYLE annotations.
func cleanTags(s string) (string, error) {
	// https://developer.mozilla.org/en-US/docs/Web/API/WebVTT_API#Cue_payload_text_tags
	// Only the following tags are the HTML tags allowed in a cue
	// and require opening and closing tags.
	// Class tag (<c></c>)
	// Italics tag (<i></i>)
	// Bold tag (<b></b>)
	// Underline tag (<u></u>)
	// Ruby tag (<ruby></ruby>)
	// Ruby text tag (<rt></rt>)
	// Voice tag (<v></v>)
	// Lang
	doc, err := html.Parse(strings.NewReader(s))
	if err != nil {
		return "", errors.New("cleanTags-0: " + err.Error())
	}
	var f func(*html.Node)
	f = func(n *html.Node) {
		switch n.Type {
		case html.ElementNode:
			switch n.Data {
			case "html", "head", "body":
				// These are injected automatically by the html package.
				// We remove them before return
			case "b", "c", "i", "ruby", "rt", "<u>":
				// Only <v> and <lang> can have an annotation.
				n.Attr = nil
			case "v", "lang":
			default:
				// if the tag is not allowed it is converted to "<b>"
				n.Data = "b"
				n.Attr = nil
			}
		case html.TextNode:
			// html.Render already escapes the text nodes
			//n.Data = template.HTMLEscapeString(n.Data)
		}
		for c := n.FirstChild; c != nil; c = c.NextSibling {
			f(c)
		}
	}
	f(doc)
	buf := bytes.NewBuffer(nil)
	if err = html.Render(buf, doc); err != nil {
		return "", err
	}
	bs := strings.TrimPrefix(buf.String(), "<html><head></head><body>")
	bs = strings.TrimSuffix(bs, "</body></html>")
	return bs, nil
}

type Reader struct {
	s *bufio.Scanner
	b bytes.Buffer
	p int64
}

func NewReader(reader io.Reader) (*Reader, error) {
	r := new(Reader)
	r.p = 0
	r.s = bufio.NewScanner(reader)
	r.s.Split(SrtScanner)
	return r, nil
}

type Err struct {
	Err []error
}

func (e *Err) Error() string {
	var s string
	for k := range e.Err {
		s += fmt.Sprintf("%#v\n", e.Err[k].Error())
	}
	return s
}

// WriteTo writes data to w until the buffer is drained
// Any error encountered during the write is also returned.
// Scanning errors are returned into an error of type Err
func (r *Reader) WriteTo(w io.Writer) (n int, err error) {
	n, err = w.Write([]byte("WEBVTT\n\n"))
	if err != nil {
		return
	}
	var e Err
	for r.s.Scan() {
		l := r.s.Text()
		l, err = SrtToWebVtt(l)
		if err != nil {
			e.Err = append(e.Err, err)
			// skip line.
			continue
		}
		var i int
		i, err = w.Write([]byte(l))
		if err != nil {
			return n, err
		}
		n += i
	}
	if e.Err != nil {
		if r.s.Err() != nil {
			e.Err = append(e.Err, r.s.Err())
		}
		return n, &e
	}
	return n, r.s.Err()
}

func (r *Reader) Read(p []byte) (n int, e error) {
	var buf bytes.Buffer
	if r.p == 0 {
		buf.WriteString("WEBVTT\n\n")
	} else {
		buf.Write(r.b.Bytes())
		r.b.Reset()
	}

	for buf.Len() < len(p) && r.s.Scan() {
		l := r.s.Text()
		l, e = SrtToWebVtt(l)
		if e != nil {
			return 0, e
		}
		buf.WriteString(l)
	}

	/*if err := scanner.Err(); err != nil {
		fmt.Printf("Invalid input: %s", err)
	}*/

	n = copy(p, buf.Bytes())
	r.p = r.p + int64(n)
	r.b.Write(buf.Bytes()[n:])
	if n == 0 {
		return n, io.EOF
	}
	return n, nil
}

//=====================================================
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

//  go run main.go shared.go utils.go
