package org.matilda.java.services.reflection

import org.matilda.java.services.reflection.protobuf.JavaValue
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class ProxyServiceInvocationHandler(
    private val mProxyHandlerService: ProxyHandlerService,
    private val mReflectionUtils: ReflectionUtils
) : InvocationHandler {
    override fun invoke(proxy: Any, method: Method, args: Array<Any?>?): Any? {
        val methodId = mReflectionUtils.register(method)
        val argumentIds = convertArguments(method, args ?: emptyArray())
        val returnValue = mProxyHandlerService.invoke(methodId, argumentIds)
        return mReflectionUtils.fromJavaValue(method.returnType, returnValue)
    }

    private fun convertArguments(method: Method, args: Array<Any?>): List<JavaValue> {
        val parameters = method.parameters
        return parameters.zip(args).map { (param, arg) -> mReflectionUtils.toJavaValue(param.type, arg) }.toList()
    }
}
