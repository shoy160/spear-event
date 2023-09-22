package module

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
)

type ClientGrantDto struct {
	Type        string `json:"type"`
	PatternType string `json:"patternType"`
	Topic       string `json:"topic"`
	Source      string `json:"source"`
	Group       string `json:"group"`
	Host        string `json:"host"`
}

type ClientDto struct {
	Id     string           `json:"id"`
	Secret string           `json:"secret"`
	Name   string           `json:"name"`
	Grants []ClientGrantDto `json:"grants"`
}

type ClientResultDto struct {
	Code    int32     `json:"code"`
	Success bool      `json:"success"`
	Message string    `json:"message"`
	Data    ClientDto `json:"data"`
}

func ReadClients() []ClientDto {
	data, err := ioutil.ReadFile("./resource/clients.json")
	CheckError(err)
	var clients []ClientDto

	err = json.Unmarshal(data, &clients)
	CheckError(err)
	return clients
}

func SyncClients(host string, token string) {
	fmt.Println("开始初始化客户端信息...")
	clients := ReadClients()
	processer := NewProcesser(len(clients), 50)
	for _, client := range clients {
		body := HttpPost("manage/client", client, host, token)
		var result ClientResultDto
		err := json.Unmarshal(body, &result)
		CheckError(err)
		if result.Success {
			clientId := result.Data.Id
			api := fmt.Sprintf("manage/client/grant/%s", clientId)
			// 授权
			grants := client.Grants
			for _, grant := range grants {
				HttpPost(api, grant, host, token)
			}
		}
		if result.Success || 400 == result.Code {
			processer.show(result.Success)
		} else {
			fmt.Printf("\n客户端信息初始化异常：%s, %d,%s\n", client.Name, result.Code, result.Message)
			break
		}
		processer.show(result.Success)
	}
	fmt.Println("客户端信息初始化完成，总计：", processer.Total)
}
