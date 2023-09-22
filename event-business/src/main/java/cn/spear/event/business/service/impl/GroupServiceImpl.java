package cn.spear.event.business.service.impl;

import cn.spear.event.business.dao.GroupMapper;
import cn.spear.event.business.domain.dto.GroupCreateDTO;
import cn.spear.event.business.domain.dto.GroupDTO;
import cn.spear.event.business.domain.dto.GroupDetailDTO;
import cn.spear.event.business.domain.dto.GroupQueryDTO;
import cn.spear.event.business.domain.enums.GroupTypeEnum;
import cn.spear.event.business.domain.enums.SortWayEnum;
import cn.spear.event.business.domain.po.AppPO;
import cn.spear.event.business.domain.po.GroupPO;
import cn.spear.event.business.service.AppService;
import cn.spear.event.business.service.GroupService;
import cn.spear.event.business.utils.PagedUtils;
import cn.spear.event.core.domain.dto.PagedDTO;
import cn.spear.event.core.exception.BusinessException;
import cn.spear.event.core.utils.CommonUtils;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 事件分组服务
 *
 * @author luoyong
 * @date 2023/2/16
 */
@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupPO> implements GroupService {
    private final Snowflake snowflake;
    private final AppService appService;

    @Override
    public boolean create(GroupCreateDTO groupDTO) {
        AppPO appModel = appService.lambdaQuery()
                .eq(AppPO::getCode, groupDTO.getAppCode())
                .one();
        if (Objects.isNull(appModel)) {
            throw new BusinessException("应用 ID 不存在");
        }
        boolean exists = lambdaQuery()
                .eq(GroupPO::getAppCode, appModel.getCode())
                .eq(GroupPO::getType, groupDTO.getType())
                .eq(GroupPO::getName, groupDTO.getName())
                .exists();
        if (exists) {
            throw new BusinessException(String.format("当前应用已存在相同的%s", groupDTO.getType().getName()));
        }
        if (Objects.nonNull(groupDTO.getParentId())) {
            GroupPO parentModel = getById(groupDTO.getParentId());
            if (Objects.isNull(parentModel)) {
                throw new BusinessException("父级 ID 不存在");
            }
            if (!Objects.equals(parentModel.getAppCode(), appModel.getCode())) {
                throw new BusinessException("父级 ID 不在同一个应用下");
            }
            if (!Objects.equals(parentModel.getType(), groupDTO.getType())) {
                throw new BusinessException("父级 ID 类型不一致");
            }
        }
        GroupPO model = CommonUtils.toBean(groupDTO, GroupPO.class);
        model.setId(snowflake.nextId());
        return save(model);
    }

    @Override
    public boolean edit(Long id, String name, String logo, Integer sort) {
        GroupPO model = getById(id);
        if (Objects.isNull(model)) {
            throw new BusinessException("事件分组/标签不存在");
        }
        boolean exists = lambdaQuery()
                .eq(GroupPO::getAppCode, model.getAppCode())
                .eq(GroupPO::getType, model.getType())
                .eq(GroupPO::getName, name)
                .ne(GroupPO::getId, id)
                .exists();
        if (exists) {
            throw new BusinessException(String.format("当前应用已存在相同的%s", model.getType().getName()));
        }
        return lambdaUpdate()
                .eq(GroupPO::getId, id)
                .set(StrUtil.isNotBlank(name), GroupPO::getName, name)
                .set(StrUtil.isNotBlank(logo), GroupPO::getLogo, logo)
                .set(Objects.nonNull(sort), GroupPO::getSort, sort)
                .update();
    }

    @Override
    public PagedDTO<GroupDetailDTO> findPaged(int page, int size, GroupQueryDTO queryDTO) {
        if (Objects.nonNull(queryDTO.getAppId())) {
            AppPO appModel = appService.getById(queryDTO.getAppId());
            if (Objects.isNull(appModel)) {
                return PagedDTO.paged(new ArrayList<>(0));
            }
            queryDTO.setAppCode(appModel.getCode());
        }
        Page<GroupPO> paged = lambdaQuery()
                .eq(Objects.nonNull(queryDTO.getType()), GroupPO::getType, queryDTO.getType())
                .eq(StrUtil.isNotBlank(queryDTO.getAppCode()), GroupPO::getAppCode, queryDTO.getAppCode())
                .eq(Objects.nonNull(queryDTO.getParentId()), GroupPO::getParentId, queryDTO.getParentId())
                .like(StrUtil.isNotBlank(queryDTO.getName()), GroupPO::getName, queryDTO.getName())
                .orderBy(Objects.nonNull(queryDTO.getSortWay()), SortWayEnum.ascend.equals(queryDTO.getSortWay()), GroupPO::getSort)
                .orderByDesc(GroupPO::getCreatedAt)
                .page(new Page<>(page, size));
        List<String> appCodes = paged.getRecords()
                .stream()
                .map(GroupPO::getAppCode)
                .distinct()
                .collect(Collectors.toList());
        Map<String, AppPO> appMap = new HashMap<>(0);
        if (CollUtil.isNotEmpty(appCodes)) {
            appMap = appService.lambdaQuery()
                    .select(AppPO::getName, AppPO::getCode, AppPO::getId, AppPO::getLogo)
                    .in(AppPO::getCode, appCodes)
                    .list().stream()
                    .collect(Collectors.toMap(AppPO::getCode, v -> v));
        }
        final Map<String, AppPO> finalAppMap = appMap;
        return PagedUtils.convert(paged, i -> {
            GroupDetailDTO source = BeanUtil.toBean(i, GroupDetailDTO.class);
            AppPO app = finalAppMap.get(i.getAppCode());
            if (Objects.nonNull(app)) {
                source.setAppId(app.getId());
                source.setAppName(app.getName());
                source.setAppLogo(app.getLogo());
            }
            return source;
        });
    }

    @Override
    public List<GroupDTO> findByAppCode(String appCode, GroupTypeEnum type) {
        LambdaQueryChainWrapper<GroupPO> wrapper = lambdaQuery()
                .eq(GroupPO::getAppCode, appCode);
        if (Objects.nonNull(type)) {
            wrapper.eq(GroupPO::getType, type);
        }
        return wrapper.list()
                .stream().map(t -> BeanUtil.toBeanIgnoreError(t, GroupDTO.class))
                .collect(Collectors.toList());
    }
}
