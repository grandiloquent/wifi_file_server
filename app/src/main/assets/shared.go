package main

import (
	"encoding/json"
	"net/http"
)

func writeJSON(w http.ResponseWriter, data interface{}) {
	buffer, err := json.Marshal(data)
	if err != nil {
		http.Error(w, err.Error(), 500)
		return
	}
	w.Header().Set("Content-Type", "application/json; charset=utf-8")
	w.Write(buffer)
}
