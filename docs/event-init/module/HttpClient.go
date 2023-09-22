package module

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
)

type ResultDto struct {
	Code      int32       `json:"code"`
	Success   bool        `json:"success"`
	Message   string      `json:"message"`
	Data      interface{} `json:"data"`
	Timestamp int64       `json:"timestamp"`
}

func HttpPost(api string, body interface{}, host string, token string) []byte {
	var data []byte
	if value, ok := body.(string); ok {
		data = []byte(value)
	} else {
		var err error
		data, err = json.Marshal(body)
		CheckError(err)
	}
	return HttpRequest(http.MethodPost, api, data, host, token)
}

func HttpPut(api string, body interface{}, host string, token string) []byte {
	data, err := json.Marshal(body)
	CheckError(err)
	return HttpRequest(http.MethodPut, api, data, host, token)
}

func HttpGet(api string, host string, token string) []byte {
	return HttpRequest(http.MethodGet, api, nil, host, token)
}

func HttpDelete(api string, host string, token string) []byte {
	return HttpRequest(http.MethodDelete, api, nil, host, token)
}

func HttpRequest(method string, api string, data []byte, host string, token string) []byte {
	config := GetConfig().Spear.event
	if host == "" {
		host = config.Host
	}
	if token == "" {
		token = config.Token
	}
	url := fmt.Sprintf("%s/%s", host, api)
	var req *http.Request
	if nil != data {
		req, _ = http.NewRequest(method, url, bytes.NewReader(data))
	} else {
		req, _ = http.NewRequest(method, url, nil)
	}

	req.Header.Add("Authorization", token)
	req.Header.Add("Content-Type", "application/json")
	client := &http.Client{}
	resp, err := client.Do(req)
	CheckError(err)
	defer resp.Body.Close()
	body, err := ioutil.ReadAll(resp.Body)
	CheckError(err)
	return body
}
