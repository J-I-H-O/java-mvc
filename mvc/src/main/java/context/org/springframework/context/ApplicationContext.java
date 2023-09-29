package context.org.springframework.context;

import context.org.springframework.stereotype.Controller;
import core.org.springframework.util.ReflectionUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.reflections.Reflections;
import webmvc.org.springframework.web.servlet.mvc.handler.adapter.HandlerAdapter;
import webmvc.org.springframework.web.servlet.mvc.handler.mapping.HandlerMapping;

public class ApplicationContext {

    private static final List<Class<?>> beanClasses = List.of(
        HandlerMapping.class,
        HandlerAdapter.class,
        ApplicationContextAware.class
    );
    private static final List<Class<? extends Annotation>> beanAnnotations = List.of(
        Controller.class
    );

    private final Map<Class<?>, Object> beans;

    public ApplicationContext(final String... basePackages) {
        this.beans = new HashMap<>();
        Arrays.stream(basePackages)
            .forEach(this::registerBeans);
        initialize();
    }

    public void registerBeans(final String basePackage) {
        final Reflections reflections = new Reflections(basePackage);
        beanClasses.stream()
            .map(reflections::getSubTypesOf)
            .forEach(this::registerBeans);
        beanAnnotations.stream()
            .map(reflections::getTypesAnnotatedWith)
            .forEach(this::registerBeans);
    }

    private <T> void registerBeans(final Set<Class<? extends T>> objectClasses) {
        objectClasses.stream()
            .filter(clazz -> !clazz.isInterface())
            .forEach(this::registerBean);
    }

    private void registerBean(final Class<?> clazz) {
        try {
            final Constructor<?> constructor = ReflectionUtils.accessibleConstructor(clazz);
            final Object bean = constructor.newInstance();
            beans.put(clazz, bean);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(clazz.getName() + " 기본 생성자가 존재하지 않습니다.");
        } catch (InvocationTargetException e) {
            throw new RuntimeException("기본 생성자를 호출할 수 없습니다.");
        } catch (InstantiationException e) {
            throw new RuntimeException("인스턴스를 생성할 수 없습니다.");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("기본 생성자에 접근할 수 없습니다.");
        }
    }

    public void initialize() {
        setFields();
        setApplicationContext();
    }

    private void setFields() {
        for (final Class<?> clazz : beans.keySet()) {
            final Field[] fields = clazz.getDeclaredFields();
            for (final Field field : fields) {
                setField(clazz, field);
            }
        }
    }

    private void setField(final Class<?> clazz, final Field field) {
        final Class<?> type = field.getType();
        final Object bean = getBean(type);
        if (bean != null) {
            try {
                field.setAccessible(true);
                field.set(beans.get(clazz), bean);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("필드에 접근할 수 없습니다.");
            }
        }
    }

    public void setApplicationContext() {
        beans.values().stream()
            .filter(bean -> bean instanceof ApplicationContextAware)
            .forEach(bean -> ((ApplicationContextAware) bean).setApplicationContext(this));
    }

    public Object getBean(final Class<?> classType) {
        return beans.get(classType);
    }

    public List<Object> getBeansOfAnnotationType(final Class<? extends Annotation> annotationType) {
        return beans.keySet().stream()
            .filter(clazz -> clazz.isAnnotationPresent(annotationType))
            .map(beans::get)
            .collect(Collectors.toList());
    }

    public <T> List<? extends T> getBeansOfType(final Class<T> classType) {
        return beans.keySet().stream()
            .filter(classType::isAssignableFrom)
            .map(beans::get)
            .map(classType::cast)
            .collect(Collectors.toList());
    }
}
