package cn.spear.event.core.domain.entity;

import java.util.Date;

/**
 * @author luoyong
 * @date 2022/11/7
 */
public interface BaseEntity {
    Date getCreatedTime();

    boolean isDeleted();
}
