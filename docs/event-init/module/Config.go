package module

import (
	"io/ioutil"

	"gopkg.in/yaml.v2"
)

type eventConfig struct {
	Host  string `yaml:"host"`
	Token string `yaml:"token"`
}

type DataSourceConfig struct {
	Url       string `yaml:"url"`
	ExistsSql string `yaml:"esists-sql"`
}

type BaseConfig struct {
	Spear EventConfig `yaml:"spear"`
}

type EventConfig struct {
	event       eventConfig       `yaml:"event"`
	DataSource DataSourceConfig `yaml:"datasource"`
}

func GetConfig() BaseConfig {
	data, err := ioutil.ReadFile("./config.yaml")
	CheckError(err)
	var config BaseConfig
	err = yaml.Unmarshal(data, &config)
	CheckError(err)
	return config
}
