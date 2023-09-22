package cn.spear.event.core.utils;

import cn.spear.event.core.Constants;
import cn.spear.event.core.lang.Func;
import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author shay
 * @date 2020/9/18
 */
public class ReflectUtils {

    private static final Map<String, Set<Class<?>>> CLAZZ_CACHE = new HashMap<>();

    /**
     * 是否抽象类
     *
     * @param clazz 类
     * @return boolean
     */
    public static boolean isAbstract(Class<?> clazz) {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    /**
     * 是否实现类
     *
     * @param baseClass 基础类
     * @param clazz     类
     * @return boolean
     */
    public static boolean isImplClass(Class<?> baseClass, Class<?> clazz) {
        return baseClass.isAssignableFrom(clazz) && !clazz.isInterface() && !isAbstract(clazz);
    }

    /**
     * 根据包名获取包下面所有的类名
     *
     * @param pack 包名
     * @return set
     */
    public static Set<Class<?>> findClasses(String pack) {
        if (null == pack) {
            pack = "";
        }
        if (CLAZZ_CACHE.containsKey(pack)) {
            return CLAZZ_CACHE.get(pack);
        }
        // 第一个class类的集合
        Set<Class<?>> classes = new LinkedHashSet<>();
        String packageDirName = pack.replace('.', '/');
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);

            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    findClassesInPackageByFile(pack, filePath, true, classes);
                } else if ("jar".equals(protocol)) {
                    JarFile jar;
                    try {
                        // 获取jar
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        // 从此jar包 得到一个枚举类
                        Enumeration<JarEntry> entries = jar.entries();
                        findClassesInPackageByJar(pack, entries, packageDirName, true, classes);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        CLAZZ_CACHE.putIfAbsent(pack, classes);
        return classes;
    }

    public static Set<Class<?>> findClasses(Func<Boolean, Class<?>> filter) {
        return findClasses(Constants.STR_EMPTY, filter);
    }

    public static Set<Class<?>> findClasses(String pack, Func<Boolean, Class<?>> filter) {
        Set<Class<?>> clazzList = findClasses(pack);
        if (CommonUtils.isEmpty(clazzList)) {
            return new LinkedHashSet<>();
        }
        if (null == filter) {
            return new LinkedHashSet<>(clazzList);
        }
        Set<Class<?>> result = new LinkedHashSet<>();
        for (Class<?> clazz : clazzList) {
            Boolean invoke = filter.invoke(clazz);
            if (null != invoke && invoke) {
                result.add(clazz);
            }
        }
        return result;
    }

    private static Class<?> createClass(String packageName, String name, boolean isJar) {
        if (CommonUtils.isEmpty(packageName) || CommonUtils.isEmpty(name)) {
            return null;
        }
        try {
            String className = packageName.concat(".").concat(FileUtil.mainName(name));
            if (isJar) {
                return Class.forName(className);
            } else {
                return Thread.currentThread().getContextClassLoader().loadClass(className);
            }
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * 以文件的形式来获取包下的所有Class
     *
     * @param packageName package
     * @param packagePath path
     * @param recursive   recursive
     * @param classes     classes
     */
    private static void findClassesInPackageByFile(
            String packageName, String packagePath, final boolean recursive, Set<Class<?>> classes
    ) {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        File[] dirFiles = dir.listFiles(file -> (recursive && file.isDirectory()) || (file.getName().endsWith(".class")));
        if (null == dirFiles) {
            return;
        }
        // 循环所有文件
        for (File file : dirFiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                String dirPackage = file.getName();
                if (CommonUtils.isNotEmpty(packageName)) {
                    dirPackage = packageName.concat(".").concat(dirPackage);
                }
                findClassesInPackageByFile(dirPackage, file.getAbsolutePath(), recursive, classes);
            } else {
                Class<?> clazz = createClass(packageName, file.getName(), false);
                if (null != clazz) {
                    classes.add(clazz);
                }
            }
        }
    }

    /**
     * 以jar的形式来获取包下的所有Class
     *
     * @param packageName    package
     * @param entries        entries
     * @param packageDirName dir
     * @param recursive      recursive
     * @param classes        classes
     */
    private static void findClassesInPackageByJar(
            String packageName, Enumeration<JarEntry> entries, String packageDirName, final boolean recursive,
            Set<Class<?>> classes) {

        // 同样的进行循环迭代
        while (entries.hasMoreElements()) {
            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            // 如果是以/开头的
            if (name.charAt(0) == '/') {
                // 获取后面的字符串
                name = name.substring(1);
            }
            // 如果前半部分和定义的包名相同
            if (name.startsWith(packageDirName)) {
                int idx = name.lastIndexOf('/');
                // 如果以"/"结尾 是一个包
                if (idx != -1) {
                    // 获取包名 把"/"替换成"."
                    packageName = name.substring(0, idx).replace('/', '.');
                }
                // 如果可以迭代下去 并且是一个包
                if ((idx != -1) || recursive) {
                    // 如果是一个.class文件 而且不是目录
                    if (name.endsWith(".class") && !entry.isDirectory()) {
                        Class<?> clazz = createClass(packageName, name, true);
                        if (null != clazz) {
                            classes.add(clazz);
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取方法注解 -> 逐级
     *
     * @param method          方法
     * @param annotationClass clazz
     * @param <T>             T
     * @return 注解
     */
    public static <T extends Annotation> T getAnnotation(Method method, Class<T> annotationClass) {
        //获取注解
        T annotation = method.getAnnotation(annotationClass);
        if (annotation == null) {
            Class<?> clazz = method.getDeclaringClass();
            annotation = clazz.getAnnotation(annotationClass);
            while (annotation == null && Object.class != (clazz = clazz.getSuperclass())) {
                annotation = clazz.getAnnotation(annotationClass);
            }
        }
        return annotation;
    }
}
