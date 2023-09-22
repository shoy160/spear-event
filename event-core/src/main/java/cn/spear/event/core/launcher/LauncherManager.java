package cn.spear.event.core.launcher;

import cn.spear.event.core.Constants;
import cn.spear.event.core.utils.ReflectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * @author shay
 * @date 2020/8/14
 */
@Slf4j
public class LauncherManager {

    private final Set<Launcher> launcherList;
    private static volatile LauncherManager instance;

    public static synchronized LauncherManager getInstance() {
        if (null == instance) {
            instance = new LauncherManager();
        }
        return instance;
    }

    private LauncherManager() {
        launcherList = new HashSet<>();
        Set<Class<?>> classSet =
                ReflectUtils.findClasses(Constants.BASE_PACKAGE, c -> ReflectUtils.isImplClass(Launcher.class, c));
        for (Class<?> clazz : classSet) {
            try {
                Object newInstance = clazz.getDeclaredConstructor().newInstance();
                launcherList.add((Launcher) newInstance);
            } catch (Exception ex) {
                log.warn("启动项加载失败", ex);
            }
        }
    }

    public void addLauncher(Launcher launcher) {
        this.launcherList.add(launcher);
    }

    public void preLoad() {
        for (Launcher launcher : this.launcherList) {
            launcher.preLoad();
        }
    }

    public void config(ConfigurableEnvironment environment, Properties properties) {
        for (Launcher launcher : this.launcherList) {
            launcher.config(environment, properties);
        }
    }

    public void onLoad() {
        for (Launcher launcher : this.launcherList) {
            launcher.onLoad();
        }
    }

    public void onDestroy() {
        for (Launcher launcher : this.launcherList) {
            launcher.onDestroy();
        }
    }
}
