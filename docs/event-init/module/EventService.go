package module

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"strings"
)

type EventDto struct {
	Type      string   `json:"type"`
	PrivateId string   `json:"privateId"`
	Code      string   `json:"code"`
	Name      string   `json:"name"`
	Desc      string   `json:"desc"`
	Tags      []string `json:"tags"`
	Partiton  int32    `json:"partition"`
	Retention int32    `json:"retention"`
}

func ReadEvents() []EventDto {
	data, err := ioutil.ReadFile("./resource/events.json")
	CheckError(err)
	var events []EventDto

	err = json.Unmarshal(data, &events)
	CheckError(err)
	return events
}

func SyncEvents(host string, token string) {
	fmt.Println("开始初始化事件信息...")
	events := ReadEvents()
	processer := NewProcesser(len(events), 50)
	for _, event := range events {
		event.Type = "Public"
		body := HttpPost("manage/event", event, host, token)
		var result ResultDto
		err := json.Unmarshal(body, &result)
		CheckError(err)
		if result.Success {
			processer.show(result.Success)
		} else if result.Code == 400 {
			if result.Message != "事件编码已存在" {
				fmt.Printf("\n事件信息初始化异常：%s, %d,%s\n", event.Code, result.Code, result.Message)
			}
			processer.show(result.Success)
		} else {
			fmt.Printf("\n事件信息初始化异常：%s, %d,%s\n", event.Code, result.Code, result.Message)
			break
		}
		// break
	}
	fmt.Println("事件信息初始化完成，总计：", processer.Total, ",成功：", processer.Success)
}

func CleanEvents(host string, token string) {
	data, err := ioutil.ReadFile("./resource/invalid_events.txt")
	CheckError(err)
	ids := strings.Split(string(data), "\n")
	processer := NewProcesser(len(ids), 50)
	for _, id := range ids {
		if id == "" {
			continue
		}
		// fmt.Println("delete event:", id)
		api := "manage/event/" + id
		body := HttpDelete(api, host, token)
		// fmt.Println(string(body))
		var result ResultDto
		err := json.Unmarshal(body, &result)
		CheckError(err)
		processer.show(result.Success)
	}
}
