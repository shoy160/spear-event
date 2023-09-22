package cn.spear.event.business.service;

import cn.spear.event.business.domain.dto.*;
import cn.spear.event.business.domain.po.EventPO;
import cn.spear.event.core.domain.dto.PagedDTO;
import cn.spear.event.core.exception.BusinessException;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

/**
 * @author luoyong
 * @date 2022/11/8
 */
public interface EventService extends IService<EventPO> {

    /**
     * 分页查询
     *
     * @param queryDTO 查询实体
     * @param page     page
     * @param size     size
     * @return paged of event
     */
    PagedDTO<EventPageDTO> findByPaged(int page, int size, EventQueryDTO queryDTO);

    /**
     * 列表查询
     *
     * @param queryDTO 查询实体
     * @return 事件列表
     */
    List<EventDTO> findList(EventQueryDTO queryDTO);

    /**
     * 分组查询
     *
     * @param queryDTO 查询实体
     * @return 事件列表
     */
    List<EventGroupDTO> findGroups(EventQueryDTO queryDTO);

    /**
     * 查询所有事件主题
     *
     * @return 所有事件主题列表
     */
    List<String> findAllTopics();

    /**
     * 事件详情
     *
     * @param eventId 事件 ID
     * @return event
     */
    EventDTO detail(Long eventId);

    /**
     * 创建事件
     *
     * @param dto 创建实体
     * @return boolean
     */
    EventDTO create(EventCreateDTO dto);


    /**
     * 编辑事件
     *
     * @param eventId 事件 ID
     * @param name    事件名称
     * @param desc    事件描述
     * @param tags    事件标签
     * @return boolean
     */
    boolean edit(Long eventId, String name, String desc, List<String> tags, Integer sort);

    /**
     * 编辑事件
     *
     * @param eventId   事件 ID
     * @param name      事件名称
     * @param desc      事件描述
     * @param tags      事件标签
     * @param sort      排序
     * @param privateId 私有 ID
     * @return boolean
     */
    boolean editInApp(
            Long eventId, String name, String desc, List<String> tags, Integer sort, String privateId
    );

    /**
     * 扩分区
     *
     * @param eventId    事件 ID
     * @param partitions 分区数
     * @return boolean
     */
    boolean extend(Long eventId, Integer partitions);

    /**
     * 删除事件
     *
     * @param eventId 事件 ID
     * @return boolean
     */
    boolean remove(Long eventId);

    /**
     * 删除事件
     *
     * @param eventId 事件 ID
     * @return boolean
     */
    boolean removeInApp(Long eventId, String privateId);

    /**
     * 查询事件
     *
     * @param eventCode 事件编码
     * @return 事件
     */
    EventDTO findByCode(String eventCode);

    /**
     * 同步消息数量
     */
    void syncMessageCount();

    /**
     * 同步消息数量
     *
     * @param eventCode 事件编码
     */
    void syncMessageCount(String eventCode);

    /**
     * 查询事件 （不存在则抛业务异常）
     *
     * @param eventCode 事件编码
     * @return 事件
     */
    default EventDTO findByRequired(String eventCode) {
        EventDTO event = findByCode(eventCode);
        if (Objects.isNull(event)) {
            throw new BusinessException(String.format("事件[%s]不存在", eventCode));
        }
        return event;
    }

    /**
     * 事件导入 （Excel）
     *
     * @param originalFilename 文件名
     * @param inputStream      stream
     */
    void importEvent(String originalFilename, InputStream inputStream) throws IOException;

    /**
     * 事件 schema 查询
     *
     * @param id 事件 ID
     */
    EventSchemaDTO getSchema(Long id);

    /**
     * 事件 schema 查询
     *
     * @param code 事件编码
     */
    EventSchemaDTO getSchema(String code);

    /**
     * 保存事件 schema
     *
     * @param id     事件 ID
     * @param schema schema
     * @return boolean
     */
    boolean saveSchema(Long id, String schema);

    /**
     * 保存事件 schema
     *
     * @param id        事件 ID
     * @param schema    schema
     * @param privateId 私有 ID
     * @return boolean
     */
    boolean saveSchema(Long id, String schema, String privateId);


    /**
     * 保存事件 schema
     *
     * @param code   事件编码
     * @param schema schema
     * @return boolean
     */
    boolean saveSchema(String code, String schema);

    /**
     * 保存事件 schema
     *
     * @param code      事件编码
     * @param schema    schema
     * @param privateId 私有 ID
     * @return boolean
     */
    boolean saveSchema(String code, String schema, String privateId);

    /**
     * 设置配置项
     *
     * @param id      ID
     * @param options 配置值
     * @param isAnd   是否新增
     * @return boolean
     */
    boolean setOptions(Long id, int options, boolean isAnd);
}
