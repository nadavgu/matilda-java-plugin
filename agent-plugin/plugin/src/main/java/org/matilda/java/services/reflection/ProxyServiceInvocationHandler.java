package org.matilda.java.services.reflection;


import org.matilda.java.services.reflection.protobuf.JavaValue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class ProxyServiceInvocationHandler implements InvocationHandler {
    private final ProxyHandlerService mProxyHandlerService;
    private final ReflectionUtils mReflectionUtils;
    public ProxyServiceInvocationHandler(ProxyHandlerService proxyHandlerService, ReflectionUtils reflectionUtils) {
        mProxyHandlerService = proxyHandlerService;
        mReflectionUtils = reflectionUtils;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        long methodId = mReflectionUtils.register(method);
        List<JavaValue> argumentIds = convertArguments(method, args);
        JavaValue returnValue = mProxyHandlerService.invoke(methodId, argumentIds);
        return mReflectionUtils.fromJavaValue(method.getReturnType(), returnValue);
    }

    private List<JavaValue> convertArguments(Method method, Object[] args) {
        List<JavaValue> argumentIds = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            argumentIds.add(mReflectionUtils.toJavaValue(parameters[i].getType(), args[i]));
        }

        return argumentIds;
    }
}
