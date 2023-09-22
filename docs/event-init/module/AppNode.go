package module

import (
	"encoding/json"
	"fmt"
)

type NodeResult struct {
	Code    int32       `json:"code"`
	Message string      `json:"message"`
	Data    interface{} `json:"data"`
}

type NodeResults struct {
	Code    int32         `json:"code"`
	Message string        `json:"message"`
	Data    []interface{} `json:"data"`
}

type FindNodeDto struct {
	Action   string `json:"action"`
	Category string `json:"category"`
	Name     string `json:"name"`
	Version  int32  `json:"version"`
}

// const eventHost = "http://localhost:8080"

func GetNodes(host string) []interface{} {
	api := "api/v1/workflow/nodes?businessLine=flowCenter"
	body := HttpGet(api, host, "")
	var result NodeResults
	err := json.Unmarshal(body, &result)
	CheckError(err)
	// fmt.Println(result)
	return result.Data
}

func GetNodeDetail(host string, nodes []FindNodeDto) interface{} {
	api := "api/v1/workflow/nodes/find-by-node-info"
	data := make(map[string]interface{})
	data["nodeInfos"] = nodes
	body := HttpPost(api, data, host, "")
	var result NodeResult
	err := json.Unmarshal(body, &result)
	CheckError(err)
	// fmt.Println(result)
	return result.Data
}

func parseVersion(version interface{}) int32 {
	if value, ok := version.(float64); ok {
		version = value
	} else if values, ok := version.([]interface{}); ok {
		version = values[len(values)-1]
	}
	return int32(version.(float64))
}

func SyncNode(sourceHost string, host string, token string, sort int, node interface{}) bool {
	// defer waitGroup.Done()
	item := node.(map[string]interface{})
	name := item["name"].(string)
	identifier := name
	if identifierValue, ok := item["identifier"].(string); ok {
		identifier = identifierValue
	}
	version := parseVersion(item["version"])
	// fmt.Println(name, version)
	findNode := &FindNodeDto{
		Name:    name,
		Version: version,
	}
	var query []FindNodeDto = []FindNodeDto{*findNode}
	detail := GetNodeDetail(sourceHost, query)
	if details, ok := detail.([]interface{}); ok {
		if len(details) > 0 {
			data := details[0].(map[string]interface{})
			data["version"] = parseVersion(data["version"])
			data["identifier"] = identifier
			data["sort"] = sort
			body := HttpPost("manage/app/node", data, host, token)
			var result ResultDto
			err := json.Unmarshal(body, &result)
			CheckError(err)
			if result.Code != 200 {
				fmt.Println(data)
				fmt.Println(name, version, result.Code, result.Message)
				return false
			}
			return true
		}
	} else {
		fmt.Println(name, version, detail, "获取详情异常")
	}
	return false
}

func SyncAppNodes(sourceHost string, targetHost string, targetToken string) {
	nodes := GetNodes(sourceHost)
	count := len(nodes)
	processer := NewProcesser(count, 50)
	// var waitGroup sync.WaitGroup
	for index, node := range nodes {
		// waitGroup.Add(1)
		// go SyncNode(node, &waitGroup)
		result := SyncNode(sourceHost, targetHost, targetToken, count-index, node)
		processer.show(result)
		// break
	}
	// waitGroup.Wait()
}
