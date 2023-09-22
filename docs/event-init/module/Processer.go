package module

import (
	"fmt"
	"math"
	"strings"
)

type Processer struct {
	Total    int
	Current  int
	Success  int
	PerValue float64
	Length   int
	Filler   string
}

func NewProcesser(total int, length int) *Processer {
	return &Processer{
		Total:    total,
		Current:  0,
		Success:  0,
		PerValue: float64(length) / float64(total),
		Length:   length,
		Filler:   "█",
	}
}

func (processer Processer) Percent() float64 {
	return float64(processer.Current*100) / float64(processer.Total)
}

func (processer Processer) Print(word string) {
	current := int(math.Ceil(float64(processer.Current) * processer.PerValue))
	leftSpace := int(math.Max(0, float64(processer.Length-current)))
	h := strings.Repeat(processer.Filler, current) + strings.Repeat(" ", leftSpace)
	if "" != word {
		fmt.Printf("\r%.1f%%\t[%s](%s)", processer.Percent(), h, word)
	} else {
		fmt.Printf("\r%.1f%%\t[%s]", processer.Percent(), h)
	}
	if processer.Current == processer.Total {
		fmt.Println("")
	}
}

func (processer *Processer) showStep(step int, word string) {
	processer.Current += step
	processer.Print(word)
}

func (processer *Processer) show(success bool) {
	processer.Current++
	if success {
		processer.Success++
	}
	word := fmt.Sprintf("%d/%d/%d", processer.Current, processer.Success, processer.Total)
	processer.Print(word)
}
