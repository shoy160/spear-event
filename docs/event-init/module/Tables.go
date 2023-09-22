package module

import (
	"database/sql"
	"fmt"
	"io/ioutil"

	_ "github.com/lib/pq"
)

func createConnection(url string) *sql.DB {
	conn, err := sql.Open("postgres", url)
	CheckError(err)
	return conn
}

func checkTables(conn *sql.DB, sql string) bool {
	row := conn.QueryRow(sql)
	var exists bool
	err := row.Scan(&exists)
	CheckError(err)
	return exists
}

func CheckError(err error) {
	if nil != err {
		panic(err)
	}
}

func InitTables(dbUrl string) {
	fmt.Println("开始初始化数据库表结构...")
	config := GetConfig()
	if "" == dbUrl {
		dbUrl = config.Spear.DataSource.Url
	}

	conn := createConnection(dbUrl)
	defer conn.Close()
	if checkTables(conn, config.Spear.DataSource.ExistsSql) {
		fmt.Println("event tables was exists")
		return
	}
	data, err := ioutil.ReadFile("./resource/event-postgresql.sql")
	CheckError(err)
	result, err := conn.Exec(string(data))
	CheckError(err)
	rows, err := result.RowsAffected()
	CheckError(err)
	fmt.Println("初始化表结构完成：Affected Rows: ", rows)
}
