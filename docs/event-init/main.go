package main

import (
	event "spear-event-init/module"
	"flag"
	"fmt"
)

func main() {
	initTables := flag.Bool("tables", false, "是否初始化表结构")
	syncEvents := flag.Bool("events", false, "是否初始化事件信息")
	cleanEvents := flag.Bool("cleanevents", false, "是否清理事件信息")
	syncClients := flag.Bool("clients", false, "是否初始化客户端信息")
	syncSchema := flag.Bool("sync-schema", false, "是否同步 Schema")
	syncNodes := flag.Bool("sync-nodes", false, "是否同步应用节点")
	var syncHost string
	var syncToken string
	var sourceHost string
	var sourceToken string
	var dbUrl string
	flag.StringVar(&syncHost, "host", "", "数据同步 Host，如：http://localhost:8080")
	flag.StringVar(&syncToken, "token", "", "数据同步 Token")
	flag.StringVar(&sourceHost, "source-host", "", "源 Host，如：http://localhost:8080")
	flag.StringVar(&sourceToken, "source-token", "", "源 Token")
	flag.StringVar(&dbUrl, "db", "", "数据库连接字符，用于表结构初始化,如：postgres://user:password@localhost:5432/spear-event?sslmode=disable")

	flag.Parse()
	fmt.Println("是否开启初始化数据库表结构：", *initTables)
	fmt.Println("是否开启初始化事件信息：", *syncEvents)
	fmt.Println("是否开启清理事件信息：", *cleanEvents)
	fmt.Println("是否开启初始化客户端信息：", *syncClients)
	fmt.Println("是否开启同步 Schema 信息：", *syncSchema)
	fmt.Println("是否开启同步是否同步应用节点：", *syncNodes)
	if *initTables {
		fmt.Println("初始化数据库表结构 ==> 开始")
		event.InitTables(dbUrl)
		fmt.Println("初始化数据库表结构 ==> 完成")
	}
	if *syncEvents {
		fmt.Println("初始化事件信息 ==> 开始")
		event.SyncEvents(syncHost, syncToken)
		fmt.Println("初始化事件信息 ==> 完成")
	}
	if *cleanEvents {
		fmt.Println("清理事件信息 ==> 开始")
		event.CleanEvents(syncHost, syncToken)
		fmt.Println("清理事件信息 ==> 完成")
	}
	if *syncClients {
		fmt.Println("初始化客户端信息 ==> 开始")
		event.SyncClients(syncHost, syncToken)
		fmt.Println("初始化客户端信息 ==> 完成")
	}
	if *syncSchema {
		fmt.Println("同步 Schema 信息 ==> 开始")
		event.SyncSchemaToOnline(sourceHost, sourceToken, syncHost, syncToken)
		fmt.Println("同步 Schema 信息 ==> 完成")
	}
	if *syncNodes {
		fmt.Println("同步应用节点信息 ==> 开始")
		event.SyncAppNodes(sourceHost, syncHost, syncToken)
		fmt.Println("同步应用节点信息 ==> 完成")
	}

}
