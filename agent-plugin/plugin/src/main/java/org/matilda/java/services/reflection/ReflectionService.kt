package org.matilda.java.services.reflection

import org.matilda.commands.MatildaCommand
import org.matilda.commands.MatildaService
import org.matilda.java.services.reflection.protobuf.JavaType
import org.matilda.java.services.reflection.protobuf.JavaValue
import java.lang.reflect.Executable
import java.lang.reflect.Modifier
import java.lang.reflect.Proxy
import javax.inject.Inject

@MatildaService
class ReflectionService @Inject constructor() {
    @Inject
    lateinit var mReflectionUtils: ReflectionUtils

    @MatildaCommand
    fun findClass(className: String): Long {
        val clazz = Class.forName(className)
        return mReflectionUtils.register(clazz)
    }

    @MatildaCommand
    fun getObjectClass(objectId: Long): Long {
        return mReflectionUtils.register(mReflectionUtils.getObject(objectId)!!.javaClass)
    }

    @MatildaCommand
    fun getClassName(id: Long): String {
        val clazz = mReflectionUtils.getClass(id)
        return clazz.canonicalName
    }

    @MatildaCommand
    fun getClassMethods(id: Long): List<Long> {
        val clazz = mReflectionUtils.getClass(id)
        return clazz.declaredMethods.map { mReflectionUtils.register(it) }.toList()
    }

    @MatildaCommand
    fun getClassConstructors(id: Long): List<Long> {
        val clazz = mReflectionUtils.getClass(id)
        return clazz.declaredConstructors.map { mReflectionUtils.register(it) }.toList()
    }

    @MatildaCommand
    fun getSuperclass(classId: Long): Long {
        val superClass = mReflectionUtils.getClass(classId).superclass
        return mReflectionUtils.register(superClass)
    }

    @MatildaCommand
    fun getInterfaces(classId: Long): List<Long> {
        val interfaces = mReflectionUtils.getClass(classId).interfaces
        return interfaces.map { mReflectionUtils.register(it) }.toList()
    }

    @MatildaCommand
    fun getExecutableName(id: Long): String {
        return mReflectionUtils.getExecutable(id).name
    }

    @MatildaCommand
    fun getExecutableParameterTypes(id: Long): List<JavaType> {
        val executable = mReflectionUtils.getExecutable(id)
        return executable.parameters
            .map { it.type }
            .map { mReflectionUtils.toJavaType(it) }
            .toList()
    }

    @MatildaCommand
    fun getMethod(classId: Long, methodName: String, parameterTypes: List<JavaType>): Long {
        val clazz = mReflectionUtils.getClass(classId)
        val list = convertTypes(parameterTypes)
        return mReflectionUtils.register(clazz.getDeclaredMethod(methodName, *list.toTypedArray()))
    }

    @MatildaCommand
    fun getConstructor(classId: Long, parameterTypes: List<JavaType>): Long {
        val clazz = mReflectionUtils.getClass(classId)
        val list = convertTypes(parameterTypes)
        return mReflectionUtils.register(clazz.getDeclaredConstructor(*list.toTypedArray()))
    }

    private fun convertTypes(parameterTypes: List<JavaType>) =
        parameterTypes.map { mReflectionUtils.fromJavaType(it) }.toList()

    @MatildaCommand
    fun isMethodStatic(id: Long): Boolean {
        val method = mReflectionUtils.getMethod(id)
        return method.modifiers and Modifier.STATIC != 0
    }

    @MatildaCommand
    fun invokeMethod(methodId: Long, objectId: Long, arguments: List<JavaValue>): JavaValue {
        return invokeMethod(mReflectionUtils.getObject(objectId), methodId, arguments)
    }

    @MatildaCommand
    fun invokeStaticMethod(methodId: Long, arguments: List<JavaValue>): JavaValue {
        return invokeMethod(null, methodId, arguments)
    }

    private fun invokeMethod(receiver: Any?, methodId: Long, arguments: List<JavaValue>): JavaValue {
        val method = mReflectionUtils.getMethod(methodId)
        val objectArguments = convertMethodArguments(method, arguments)
        return mReflectionUtils.toJavaValue(method.returnType, method.invoke(receiver, *objectArguments))
    }

    @MatildaCommand
    fun createNewInstance(constructorId: Long, arguments: List<JavaValue>): Long {
        val constructor = mReflectionUtils.getConstructor(constructorId)
        val objectArguments = convertMethodArguments(constructor, arguments)
        return mReflectionUtils.register(constructor.newInstance(*objectArguments))
    }

    private fun convertMethodArguments(executable: Executable, arguments: List<JavaValue>): Array<Any?> {
        val parameters = executable.parameters
        require(parameters.size == arguments.size)

        return parameters.zip(arguments)
            .map { (param, arg) -> mReflectionUtils.fromJavaValue(param.type, arg)}
            .toTypedArray()
    }

    @MatildaCommand
    fun getClassFields(id: Long): List<Long> {
        val clazz = mReflectionUtils.getClass(id)
        return clazz.declaredFields.map { mReflectionUtils.register(it) }.toList()
    }

    @MatildaCommand
    fun getFieldName(id: Long): String {
        return mReflectionUtils.getField(id).name
    }

    @MatildaCommand
    fun getFieldType(id: Long): JavaType {
        return mReflectionUtils.toJavaType(mReflectionUtils.getField(id).type)
    }

    @MatildaCommand
    fun getField(classId: Long, fieldName: String): Long {
        return mReflectionUtils.register(mReflectionUtils.getClass(classId).getDeclaredField(fieldName))
    }

    @MatildaCommand
    fun isFieldStatic(id: Long): Boolean {
        return mReflectionUtils.getField(id).modifiers and Modifier.STATIC != 0
    }

    @MatildaCommand
    fun getFieldValue(fieldId: Long, objectId: Long): JavaValue {
        return getFieldValue(fieldId, mReflectionUtils.getObject(objectId))
    }

    @MatildaCommand
    fun getStaticFieldValue(fieldId: Long): JavaValue {
        return getFieldValue(fieldId, null)
    }

    private fun getFieldValue(fieldId: Long, obj: Any?): JavaValue {
        val field = mReflectionUtils.getField(fieldId)
        field.isAccessible = true
        return mReflectionUtils.toJavaValue(field.type, field.get(obj))
    }

    @MatildaCommand
    fun setFieldValue(fieldId: Long, objectId: Long, newValue: JavaValue) {
        setFieldValue(fieldId, mReflectionUtils.getObject(objectId), newValue)
    }

    @MatildaCommand
    fun setStaticFieldValue(fieldId: Long, newValue: JavaValue) {
        setFieldValue(fieldId, null, newValue)
    }

    private fun setFieldValue(fieldId: Long, obj: Any?, newValue: JavaValue) {
        val field = mReflectionUtils.getField(fieldId)
        field.isAccessible = true
        field.set(obj, mReflectionUtils.fromJavaValue(field.type, newValue))
    }

    @MatildaCommand
    fun newProxyInstance(interfaceIds: List<Long>, proxyHandler: ProxyHandlerService): Long {
        return mReflectionUtils.register(Proxy.newProxyInstance(javaClass.classLoader,
            interfaceIds.map { mReflectionUtils.getClass(it) }.toTypedArray(),
            ProxyServiceInvocationHandler(proxyHandler, mReflectionUtils)))
    }
}
