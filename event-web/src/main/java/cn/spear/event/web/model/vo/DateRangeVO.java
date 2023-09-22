package cn.spear.event.web.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * 时间区间参数
 *
 * @author shay
 * @date 2020/7/29
 */
@Getter
@Setter
@ToString
@ApiModel
public class DateRangeVO {

    @ApiModelProperty(value = "开始时间")
    private Date begin;

    @ApiModelProperty(value = "结束时间")
    private Date end;

    public DateRangeVO() {
    }

    public DateRangeVO(Date begin, Date end) {
        this.begin = begin;
        this.end = end;
    }
}
