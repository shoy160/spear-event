package cn.spear.event.core;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author shay
 * @date 2021/4/12
 */
@Slf4j
@RequiredArgsConstructor
public class AppContext {
    private final String appName;
    private String appMode;
    private static AppContext instance;

    public static void init(String appName) {
        instance = new AppContext(appName);
    }

    public static String getAppName() {
        return null == instance ? Constants.STR_EMPTY : instance.appName;
    }

    public static String getAppMode() {
        return null == instance ? Constants.MODE_DEV : instance.appMode;
    }

    private static String getActiveProfile(String[] activeProfiles) {
        if (null == activeProfiles || 0 == activeProfiles.length) {
            return Constants.MODE_DEV;
        }
        // 判断环境:dev、test、ready、prod
        List<String> profiles = Arrays.asList(activeProfiles);
        return profiles
                .stream()
                .filter(Constants.MODE_LIST::contains)
                .findFirst()
                .orElse(Constants.MODE_DEV);
    }

    public static void setAppMode(String[] activeProfiles) {
        if (Objects.nonNull(instance)) {
            instance.appMode = getActiveProfile(activeProfiles);
            log.info("Start With Mode -> {}", instance.appMode);
        }
    }

    /**
     * 是否开发环境
     *
     * @return boolean
     */
    public static boolean isDev() {
        if (instance == null || StrUtil.isEmpty(instance.appMode)) {
            return true;
        }
        if (!Constants.validMode(instance.appMode)) {
            return true;
        }
        return isMode(Constants.MODE_DEV);
    }

    /**
     * 是否测试环境
     *
     * @return boolean
     */
    public static boolean isTest() {
        return isMode(Constants.MODE_TEST);
    }

    /**
     * 是否正式环境
     *
     * @return boolean
     */
    public static boolean isProd() {
        return isMode(Constants.MODE_PROD);
    }


    /**
     * 是否运行与某个环境
     *
     * @param mode 环境，Constants.MODE_xx
     * @return boolean
     */
    public static boolean isMode(String mode) {
        if (instance == null || StrUtil.isEmpty(mode)) {
            return false;
        }
        return mode.equals(instance.appMode);
    }

    /**
     * 是否运行与某几个环境钟的一个
     *
     * @param modes 环境，Constants.MODE_xx
     * @return boolean
     */
    public static boolean isModes(String... modes) {
        if (instance == null) {
            return false;
        }
        return Arrays.asList(modes).contains(instance.appMode);
    }
}
