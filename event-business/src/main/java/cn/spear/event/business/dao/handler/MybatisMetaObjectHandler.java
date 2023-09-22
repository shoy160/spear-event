package cn.spear.event.business.dao.handler;

import cn.spear.event.core.Constants;
import cn.spear.event.core.session.Session;
import cn.spear.event.core.utils.CommonUtils;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * todo
 *
 * @author shay
 * @date 2022/11/12
 **/
@Component
@RequiredArgsConstructor
public class MybatisMetaObjectHandler implements MetaObjectHandler {
    private static final String FIELD_CREATED_AT = "createdAt";
    private static final String FIELD_UPDATED_AT = "updatedAt";
    private static final String FIELD_CREATOR_ID = "creatorId";
    private static final String FIELD_CREATOR_NAME = "creatorName";
    private static final String FIELD_MODIFIER_ID = "modifierId";
    private static final String FIELD_MODIFIER_NAME = "modifierName";

    private final Session session;

    @Override
    public void insertFill(MetaObject metaObject) {
        this.fillInsertField(metaObject, FIELD_CREATED_AT, new Date(), Date.class);
        String userId = Optional.ofNullable(session.userIdAsString())
                .orElse(Constants.STR_EMPTY);
        this.fillInsertField(metaObject, FIELD_CREATOR_ID, userId, String.class);
        this.fillInsertField(metaObject, FIELD_CREATOR_NAME, session.getUserName(), String.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.fillUpdateField(metaObject, FIELD_UPDATED_AT, new Date(), Date.class);
        String userId = Optional.ofNullable(session.userIdAsString())
                .orElse(Constants.STR_EMPTY);
        this.fillUpdateField(metaObject, FIELD_MODIFIER_ID, userId, String.class);
        this.fillUpdateField(metaObject, FIELD_MODIFIER_NAME, session.getUserName(), String.class);
    }

    private <T> void fillInsertField(MetaObject metaObject, String field, T value, Class<T> clazz) {
        if (!metaObject.hasSetter(field)
                || Objects.nonNull(metaObject.getValue(field))) {
            return;
        }
        if (CommonUtils.isNotEmpty(value)) {
            this.strictInsertFill(metaObject, field, clazz, value);
        }
    }

    private <T> void fillUpdateField(MetaObject metaObject, String field, T value, Class<T> clazz) {
        if (!metaObject.hasSetter(field)) {
            return;
        }
        if (CommonUtils.isNotEmpty(value)) {
            this.strictUpdateFill(metaObject, field, clazz, value);
        }
    }
}
