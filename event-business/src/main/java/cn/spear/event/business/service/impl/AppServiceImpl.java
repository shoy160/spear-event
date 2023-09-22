package cn.spear.event.business.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.spear.event.business.config.EventProperties;
import cn.spear.event.business.dao.AppMapper;
import cn.spear.event.business.domain.dto.AppCreateDTO;
import cn.spear.event.business.domain.dto.AppDTO;
import cn.spear.event.business.domain.dto.AppPageDTO;
import cn.spear.event.business.domain.dto.AppShowDTO;
import cn.spear.event.business.domain.enums.AppTypeEnum;
import cn.spear.event.business.domain.enums.OptionsEnum;
import cn.spear.event.business.domain.enums.SortWayEnum;
import cn.spear.event.business.domain.po.AppPO;
import cn.spear.event.business.service.AppService;
import cn.spear.event.business.utils.PagedUtils;
import cn.spear.event.core.Constants;
import cn.spear.event.core.domain.dto.PagedDTO;
import cn.spear.event.core.exception.BusinessException;
import cn.spear.event.core.session.Session;
import cn.spear.event.core.utils.CommonUtils;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;


/**
 * 事件服务
 *
 * @author luoyong
 * @date 2022/11/8
 */
@Service
@RequiredArgsConstructor
public class AppServiceImpl extends ServiceImpl<AppMapper, AppPO> implements AppService {
    private static final String RULE_APP_CODE = "^[a-z][0-9a-z_-\\.]*[0-9a-z]$";
    private final Snowflake snowflake;
    private final EventProperties eventProperties;
    private final Session session;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppDTO create(AppCreateDTO dto) {
//        if (!ReUtil.isMatch(RULE_APP_CODE, dto.getCode())) {
//            throw new BusinessException("应用唯一标识格式错误，请输入 2 位以上的数字、字母、_-.");
//        }
        boolean exists = lambdaQuery()
                .eq(AppPO::getCode, dto.getCode())
                .exists();
        if (exists) {
            throw new BusinessException("应用编码已存在");
        }
        if (AppTypeEnum.Custom.equals(dto.getType())) {
            if (StrUtil.isBlank(dto.getPrivateId())) {
                throw new BusinessException("自定义应用必须有私有 ID");
            }
            // 编码检测
            EventProperties.CustomAppConfig config = eventProperties.getCustomApp();
            final String prefix = config.getPrefix();
            if (!dto.getCode().startsWith(prefix)) {
                throw new BusinessException(String.format("自定义应用编码必须以「%s」开头", prefix));
            }
            if (StrUtil.isBlank(dto.getLogo())) {
                dto.setLogo(config.getLogo());
            }
        }
        AppPO model = CommonUtils.toBean(dto, AppPO.class);
        model.setId(snowflake.nextId());
        model.setOptions(OptionsEnum.None.getValue());
        boolean result = save(model);
        if (result) {
            return CommonUtils.toBean(model, AppDTO.class);
        }
        return null;
    }

    @Override
    public PagedDTO<AppPageDTO> findPaged(int page, int size, AppTypeEnum type, String privateId, String code, SortWayEnum sortWay) {
        Page<AppPO> paged = lambdaQuery()
                .eq(Objects.nonNull(type), AppPO::getType, type)
                .eq(StrUtil.isNotBlank(privateId), AppPO::getPrivateId, privateId)
                .eq(StrUtil.isNotBlank(code), AppPO::getCode, code)
                .orderBy(Objects.nonNull(sortWay), SortWayEnum.ascend.equals(sortWay), AppPO::getSort)
                .orderByDesc(AppPO::getCreatedAt)
                .page(new Page<>(page, size));
        return PagedUtils.convert(paged, i -> {
            AppPageDTO source = BeanUtil.toBean(i, AppPageDTO.class);
            if (Objects.equals(i.getType(), AppTypeEnum.System)
                    || StrUtil.startWith(i.getCode(), eventProperties.getCustomApp().getPrefix())) {
                source.setAllowDelete(false);
            }
            return source;
        });
    }

    @Override
    public List<AppDTO> findByPrivateId(String privateId, AppTypeEnum type, Integer options) {
        LambdaQueryChainWrapper<AppPO> wrapper = lambdaQuery();
        if (Objects.nonNull(type)) {
            wrapper.eq(AppPO::getType, type);
            if (AppTypeEnum.Custom.equals(type)) {
                wrapper.eq(StrUtil.isNotBlank(privateId), AppPO::getPrivateId, privateId);
            }
        } else {
            if (StrUtil.isNotBlank(privateId)) {
                wrapper.and(t -> t.eq(AppPO::getType, AppTypeEnum.System)
                        .or()
                        .eq(AppPO::getPrivateId, privateId)
                );
            } else {
                wrapper.eq(AppPO::getType, AppTypeEnum.System);
            }
        }
        wrapper.apply(Objects.nonNull(options), "(options & {0})={0}", options);
        wrapper.orderByAsc(AppPO::getType).orderByDesc(AppPO::getSort);
        return wrapper
                .list()
                .stream()
                .map(t -> CommonUtils.toBean(t, AppDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public AppDTO findOrCreateCustomApp(String privateId) {
        List<AppDTO> privateApps = findByPrivateId(privateId, AppTypeEnum.Custom, null);
        if (CollUtil.isEmpty(privateApps)) {
            //自动创建
            AppCreateDTO createDTO = new AppCreateDTO();
            createDTO.setType(AppTypeEnum.Custom);
            createDTO.setPrivateId(privateId);
            String code = generateCode();
            createDTO.setName("默认自定义应用");
            createDTO.setCode(code);
            AppDTO app = create(createDTO);
            if (Objects.nonNull(app)) {
                return app;
            }
            throw new BusinessException("创建自定义应用失败");
        }
        return privateApps.get(0);
    }

    @Override
    public AppDTO findByCode(String appCode) {
        AppPO model = lambdaQuery().eq(AppPO::getCode, appCode).one();
        if (Objects.isNull(model)) {
            return null;
        }
        return CommonUtils.toBean(model, AppDTO.class);
    }

    @Override
    public boolean edit(Long id, AppDTO app) {
        AppPO model = getById(id);
        if (Objects.isNull(model)) {
            throw new BusinessException("应用不存在");
        }
        session.setClaim(Constants.AUDIT_SOURCE_DATA, model);
        boolean result = lambdaUpdate().eq(AppPO::getId, id)
                .set(StrUtil.isNotBlank(app.getName()), AppPO::getName, app.getName())
                .set(AppPO::getLogo, app.getLogo())
                .set(AppPO::getSort, app.getSort())
                .update();
        if (result) {
            session.setClaim(Constants.AUDIT_TARGET_DATA, getById(id));
        }
        return result;

    }

    @Override
    public List<AppDTO> getList(AppTypeEnum type, String privateId, String code) {
        // 允许添加自定义事件
        LambdaQueryChainWrapper<AppPO> wrapper = lambdaQuery()
                .eq(StrUtil.isNotBlank(code), AppPO::getCode, code);
        if (Objects.nonNull(type) || StrUtil.isNotBlank(privateId)) {
            wrapper.eq(AppPO::getCode, "spear")
                    .or(t -> t.eq(Objects.nonNull(type), AppPO::getType, type)
                            .eq(StrUtil.isNotBlank(privateId), AppPO::getPrivateId, privateId));
        }
        return wrapper
                .orderByAsc(AppPO::getType)
                .orderByDesc(AppPO::getSort)
                .list().stream()
                .map(i -> BeanUtil.toBean(i, AppDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Boolean deleteById(Long id) {
        AppPO model = getById(id);
        if (Objects.isNull(model)) {
            throw new BusinessException("应用不存在");
        }
        if (StrUtil.startWith(model.getCode(), eventProperties.getCustomApp().getPrefix())) {
            throw new BusinessException("该应用无法删除");
        }
        return removeById(id);
    }

    @Override
    public Map<String, AppShowDTO> findShowInfoByCode(List<String> appCodes) {
        if (CollUtil.isEmpty(appCodes)) {
            return new HashMap<>(0);
        }
        return lambdaQuery().in(AppPO::getCode, appCodes)
                .select(AppPO::getCode, AppPO::getName, AppPO::getId, AppPO::getLogo)
                .list().stream()
                .collect(Collectors.toMap(AppPO::getCode, v -> BeanUtil.toBean(v, AppShowDTO.class)));
    }

    @Override
    public AppDTO getDetail(Long id) {
        AppPO model = getById(id);
        if (Objects.isNull(model)) {
            throw new BusinessException("应用 ID 不存在");
        }
        return BeanUtil.toBean(model, AppDTO.class, CopyOptions.create().ignoreNullValue().ignoreError());
    }

    @Override
    public boolean setOptions(Long id, int options, boolean isAnd) {
        AppPO model = lambdaQuery().eq(AppPO::getId, id)
                .select(AppPO::getId, AppPO::getOptions)
                .one();
        if (Objects.isNull(model)) {
            throw new BusinessException(String.format("应用[%d]不存在", id));
        }
        options = options | model.getOptions();
        options = isAnd ? options : (options ^ model.getOptions());
        options = Math.max(0, options);
        return lambdaUpdate()
                .eq(AppPO::getId, id)
                .set(AppPO::getOptions, options)
                .update();
    }

    private String generateCode() {
        EventProperties.CustomAppConfig config = eventProperties.getCustomApp();
        final String prefix = config.getPrefix();
        final int length = config.getLength();
        final long timeoutMs = config.getTimeout();
        List<String> existsCodeList = lambdaQuery().likeRight(AppPO::getCode, prefix)
                .select(AppPO::getCode)
                .list()
                .stream()
                .map(AppPO::getCode)
                .collect(Collectors.toList());
        Future<String> codeFuture = ThreadUtil.execAsync(() -> {
            while (true) {
                String code = prefix.concat(RandomUtil.randomString(RandomUtil.BASE_CHAR, length));
                if (!existsCodeList.contains(code)) {
                    return code;
                }
            }
        });
        try {
            return codeFuture.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new BusinessException("生成应用编码失败");
        }
    }
}
