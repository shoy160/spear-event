package cn.spear.event.web.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.Min;

/**
 * 分页参数
 *
 * @author shay
 * @date 2020/7/28
 */
@Getter
@ToString
@ApiModel
public class PageVO {
    @ApiModelProperty(value = "页码(默认：1)")
    @Min(value = 1, message = "页码必须大于0")
    private Integer page;

    @ApiModelProperty(value = "分页大小(默认：10)")
    @Min(value = 1, message = "分页大小必须大于0")
    private Integer size;

    public PageVO() {
        this.page = 1;
        this.size = 10;
    }

    public PageVO(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public void setPage(Integer page) {
        this.page = page == null ? 1 : page;
    }

    public void setSize(Integer size) {
        this.size = size == null ? 10 : size;
    }
}
