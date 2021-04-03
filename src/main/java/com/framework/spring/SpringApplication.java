package com.framework.spring;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpringApplication {
    private Class configClass;
    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private Map<String, Object> singletonPoolBeans = new ConcurrentHashMap<>();
    private List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();
    public SpringApplication(Class configClass)  {
        this.configClass = configClass;
        //扫描class
        List<Class> classes = scan(configClass);
        //过滤 BeanDefinition  解析类--》得到BeanDef
        for (Class clazz : classes) {
            if (clazz.isAnnotationPresent(Component.class)) {

                if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                    try {
                        BeanPostProcessor beanPostProcessor = (BeanPostProcessor)clazz.getDeclaredConstructor().newInstance();
                        beanPostProcessors.add(beanPostProcessor);
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }
                BeanDefinition beanDefinition = new BeanDefinition();
                beanDefinition.setBeanClass(clazz);

                Component component = (Component) clazz.getAnnotation(Component.class);
                String beanName = component.value();
                if (clazz.isAnnotationPresent(Scope.class)) {
                    Scope scopeAnnotation = (Scope) clazz.getAnnotation(Scope.class);
                    String scope = scopeAnnotation.value();
                    beanDefinition.setScope(scope);
                }else{
                    //默认单例
                    beanDefinition.setScope("singleton");
                }
                beanDefinitionMap.put(beanName,beanDefinition);
            }
        }
        //基于class创建单例bean
        instanceSingletonBean();
    }

    private List<Class> scan(Class configClass) {
        List<Class> classes = new ArrayList<Class>();
        //判断ComponentScan注解
        ComponentScan componentScan = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
        String path = componentScan.value();
        path = path.replace(".", "/");
        ClassLoader classLoader = SpringApplication.class.getClassLoader();
        URL url = classLoader.getResource(path);
        File file = new File(url.getFile());
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File fil : files) {
                String absolutePath = fil.getAbsolutePath();
                absolutePath = absolutePath.substring(absolutePath.indexOf("com"), absolutePath.indexOf(".class"));
                absolutePath = absolutePath.replace("\\", ".");
                Class<?> clazz = null;
                try {
                    clazz = classLoader.loadClass(absolutePath);
                    //判断Component注解
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                classes.add(clazz);
            }
        }
        return classes;
    }

    private void instanceSingletonBean() {
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("singleton")){
                //创建bean
                Object bean = doCreateBean(beanName, beanDefinition);
                singletonPoolBeans.putIfAbsent(beanName, bean);
            }
        }
    }

    private Object doCreateBean(String beanName, BeanDefinition beanDefinition) {
        //1.实例化
        Class beanClass = beanDefinition.getBeanClass();
        try {
            Object bean = beanClass.getDeclaredConstructor().newInstance();
            //2 依赖注入 属性填充
            Field[] fields = beanClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    //属性赋值
                    Object value = getBean(field.getName());
                    field.setAccessible(true);
                    field.set(bean,value);
                }
            }
            //3.Aware
            if (bean instanceof BeanNameAware) {
                ((BeanNameAware) bean).setBeanName(beanName);
            }

            //初始化之前
            for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
                bean=beanPostProcessor.postProcessBeforeInitialization(bean,beanName);
            }
            //4. 初始化
            if (bean instanceof InitializingBean) {
                ((InitializingBean) bean).afterPropertiesSet();
            }
            //初始化之后
            for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
                bean = beanPostProcessor.postProcessAfterInitialization(bean,beanName);
            }
            return bean;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Object getBean(String beanName) {
        //beanName 单例  原型
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition.getScope().equals("prototype")) {
            //创建bean
            Object bean = doCreateBean(beanName, beanDefinition);
            return bean;
        } else if (beanDefinition.getScope().equals("singleton")) {
            //从单例池拿
            Object bean = singletonPoolBeans.get(beanName);
            if (bean == null) {
                bean= doCreateBean(beanName, beanDefinition);
                singletonPoolBeans.put(beanName, bean);
            }
            return bean;
        }
        return null;
    }
}
