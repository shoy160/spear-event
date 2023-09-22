package cn.spear.event.business.service;

import cn.spear.event.business.domain.dto.GroupCreateDTO;
import cn.spear.event.business.domain.dto.GroupDTO;
import cn.spear.event.business.domain.dto.GroupDetailDTO;
import cn.spear.event.business.domain.dto.GroupQueryDTO;
import cn.spear.event.business.domain.enums.GroupTypeEnum;
import cn.spear.event.business.domain.po.GroupPO;
import cn.spear.event.core.domain.dto.PagedDTO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author luoyong
 * @date 2023/2/16
 */
public interface GroupService extends IService<GroupPO> {

    /**
     * 创建
     *
     * @param groupDTO group
     * @return boolean
     */
    boolean create(GroupCreateDTO groupDTO);

    /**
     * 编辑
     *
     * @param id   id
     * @param name name
     * @param logo Logo
     * @param sort 排序
     * @return boolean
     */
    boolean edit(Long id, String name, String logo, Integer sort);

    /**
     * 分页查询
     *
     * @param page     page
     * @param size     size
     * @param queryDTO 查询实体
     * @return 分页列表
     */
    PagedDTO<GroupDetailDTO> findPaged(int page, int size, GroupQueryDTO queryDTO);

    /**
     * 查询应用下的分组
     *
     * @param appCode 应用标识
     * @param type    分组类型
     * @return 事件分组列表
     */
    List<GroupDTO> findByAppCode(String appCode, GroupTypeEnum type);
}
