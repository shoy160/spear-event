package cn.spear.event.business.service;

import cn.spear.event.business.domain.dto.AppCreateDTO;
import cn.spear.event.business.domain.dto.AppDTO;
import cn.spear.event.business.domain.dto.AppPageDTO;
import cn.spear.event.business.domain.dto.AppShowDTO;
import cn.spear.event.business.domain.enums.AppTypeEnum;
import cn.spear.event.business.domain.enums.SortWayEnum;
import cn.spear.event.business.domain.po.AppPO;
import cn.spear.event.core.domain.dto.PagedDTO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @author luoyong
 * @date 2022/11/8
 */
public interface AppService extends IService<AppPO> {
    /**
     * 创建事件
     *
     * @param dto 创建实体
     * @return boolean
     */
    AppDTO create(AppCreateDTO dto);

    /**
     * 分页查询
     *
     * @param page      page
     * @param size      size
     * @param type      应用类型
     * @param code      应用编码
     * @param privateId 私有 ID
     * @param sortWay   排序方式
     * @return page of AppDto
     */
    PagedDTO<AppPageDTO> findPaged(int page, int size, AppTypeEnum type, String privateId, String code, SortWayEnum sortWay);

    /**
     * 获取自定义应用列表
     *
     * @param privateId 私有 ID
     * @param type      应用类型
     * @param options   应用配置
     * @return List<AppPO>
     */
    List<AppDTO> findByPrivateId(String privateId, AppTypeEnum type, Integer options);

    /**
     * 获取或创建一个自定义应用
     *
     * @param privateId 私有 ID
     * @return 应用信息
     */
    AppDTO findOrCreateCustomApp(String privateId);

    /**
     * 获取应用详情
     *
     * @param appCode 应用编码
     * @return 应用信息
     */
    AppDTO findByCode(String appCode);

    /**
     * 应用编辑
     *
     * @param id  id
     * @param app app
     * @return boolean
     */
    boolean edit(Long id, AppDTO app);

    /**
     * 应用列表
     *
     * @param type      应用类型
     * @param privateId 私有 ID
     * @param code      应用编码
     * @return list
     */
    List<AppDTO> getList(AppTypeEnum type, String privateId, String code);

    /**
     * 应用删除
     *
     * @param id id
     * @return boolean
     */
    Boolean deleteById(Long id);

    /**
     * 获取展示应用展示信息
     *
     * @param appCodes appCodes
     * @return map
     */
    Map<String, AppShowDTO> findShowInfoByCode(List<String> appCodes);

    /**
     * 应用详情
     *
     * @param id id
     * @return detail
     */
    AppDTO getDetail(Long id);

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
