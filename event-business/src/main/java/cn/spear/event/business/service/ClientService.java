package cn.spear.event.business.service;

import cn.spear.event.business.domain.dto.ClientDTO;
import cn.spear.event.business.domain.enums.StatusEnum;
import cn.spear.event.business.domain.po.ClientPO;
import cn.spear.event.core.connector.enums.LanguageType;
import cn.spear.event.core.domain.dto.PagedDTO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @author luoyong
 * @date 2022/11/10
 */
public interface ClientService extends IService<ClientPO> {
    /**
     * 创建 Client
     *
     * @param clientId     ID
     * @param name         名称
     * @param clientSecret 密钥
     * @return Client
     */
    ClientDTO create(String name, Long clientId, String clientSecret);

    /**
     * 创建 Client
     *
     * @param name 名称
     * @return Client
     */
    default ClientDTO create(String name) {
        return create(name, null, null);
    }

    /**
     * 客户端分页
     *
     * @param page page
     * @param size size
     * @param name 客户端名称
     * @return page of ClientDTO
     */
    PagedDTO<ClientDTO> paged(int page, int size, String name);


    /**
     * 查询 Client
     *
     * @param clientId 客户端 ID
     * @return client
     */
    ClientDTO findById(Long clientId);

    /**
     * 验证客户端
     *
     * @param clientId 客户端 ID
     * @param secret   客户端秘钥
     * @return 客户端信息
     */
    ClientDTO verify(Long clientId, String secret);

    /**
     * 更新状态
     *
     * @param clientId 客户端 ID
     * @param status   状态
     * @return boolean
     */
    boolean updateStatus(Long clientId, StatusEnum status);


    /**
     * 获取订阅配置
     *
     * @param clientId     客户端 ID
     * @param event        事件编码
     * @param languageType 语言类型
     * @param privateId    私有 ID
     * @return Map 配置项
     */
    Map<String, String> consumerConfig(Long clientId, String event, LanguageType languageType, String privateId);

    /**
     * 客户端编辑
     *
     * @param clientId 客户端 ID
     * @param name     客户端名称
     * @return boolean
     */
    boolean edit(Long clientId, String name);

    /**
     * 客户端密钥更新
     *
     * @param clientId 客户端 ID
     * @return boolean
     */
    boolean updateSecret(Long clientId);

    /**
     * 客户端删除
     *
     * @param clientId clientId
     * @return boolean
     */
    boolean remove(Long clientId);

    /**
     *  客户端详情
     * @param id id
     * @return client
     */
    ClientDTO getDetail(Long id);
}
