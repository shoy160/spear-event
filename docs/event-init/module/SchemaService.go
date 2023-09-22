package module

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
)

type SchemaDto struct {
	Id     string `json:"id"`
	Code   string `json:"code"`
	Schema string `json:"schema"`
}

type SchemaResultDto struct {
	Code    int32     `json:"code"`
	Success bool      `json:"success"`
	Message string    `json:"message"`
	Data    SchemaDto `json:"data"`
}

func getSchema(code string, host string, token string) SchemaResultDto {
	api := fmt.Sprintf("manage/event/%s/schema", code)
	body := HttpGet(api, host, token)
	var result SchemaResultDto
	err := json.Unmarshal(body, &result)
	CheckError(err)
	return result
}

func syncSchema(code string, schema string, host string, token string) ResultDto {
	api := fmt.Sprintf("manage/event/%s/schema", code)
	body := HttpPost(api, schema, host, token)
	var result ResultDto
	err := json.Unmarshal(body, &result)
	CheckError(err)
	return result
}

func readEventCodes() []string {
	// data, err := ioutil.ReadFile("./resource/schema_events.json")
	data, err := ioutil.ReadFile("./resource/schema_230422.json")
	CheckError(err)
	var events []string

	err = json.Unmarshal(data, &events)
	CheckError(err)
	return events
}

func SyncSchemaToOnline(sourceHost string, sourceToken string, syncHost string, syncToken string) {
	fmt.Println("开始同步事件 Schema 信息...")
	events := readEventCodes()
	processer := NewProcesser(len(events), 50)
	for _, event := range events {
		schemaResult := getSchema(event, sourceHost, sourceToken)
		// fmt.Println(result)
		if !schemaResult.Success {
			fmt.Printf("\n获取事件 Schema 信息异常：%s, %d,%s\n", event, schemaResult.Code, schemaResult.Message)
			continue
		}
		result := syncSchema(event, schemaResult.Data.Schema, syncHost, syncToken)
		processer.show(result.Success)
		if !result.Success {
			fmt.Printf("\n同步事件 Schema 信息异常：%s, %d,%s\n", event, result.Code, result.Message)
			continue
		}
		// break
	}
}
