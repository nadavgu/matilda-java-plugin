package org.matilda.java.services.reflection

import org.matilda.java.services.reflection.protobuf.JavaType
import org.matilda.java.services.reflection.protobuf.JavaValue
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Field
import java.lang.reflect.Method
import javax.inject.Inject

class ReflectionUtils @Inject internal constructor() {
    @Inject
    lateinit var mObjectRepository: ObjectRepository
    fun register(obj: Any): Long {
        return mObjectRepository.add(obj)
    }

    fun getClass(classId: Long): Class<*> {
        return mObjectRepository[classId] as Class<*>
    }

    fun getExecutable(id: Long): Executable {
        return mObjectRepository[id] as Executable
    }

    fun getMethod(methodId: Long): Method {
        return mObjectRepository[methodId] as Method
    }

    fun getConstructor(constructorId: Long): Constructor<*> {
        return mObjectRepository[constructorId] as Constructor<*>
    }

    fun getField(fieldId: Long): Field {
        return mObjectRepository[fieldId] as Field
    }

    fun getObject(objectId: Long): Any {
        return mObjectRepository[objectId]
    }

    fun fromJavaType(type: JavaType): Class<*> {
        return if (type.hasPrimitiveClassName()) {
            getPrimitiveClass(type.primitiveClassName)
        } else {
            getClass(type.classId)
        }
    }

    fun toJavaType(type: Class<*>): JavaType {
        val typeBuilder = JavaType.newBuilder()
        if (type.isPrimitive) {
            typeBuilder.setPrimitiveClassName(type.canonicalName)
        } else {
            typeBuilder.setClassId(register(type))
        }
        return typeBuilder.build()
    }

    private fun getPrimitiveClass(name: String): Class<*> {
        return PRIMITIVE_CLASSES.firstOrNull { it.name == name } ?: throw ClassNotFoundException()
    }

    fun toJavaValue(type: Class<*>, obj: Any?): JavaValue {
        if (obj == null) {
            return JavaValue.newBuilder().build()
        }

        if (!type.isPrimitive) {
            return JavaValue.newBuilder().setObjectId(register(obj)).build()
        }

        return when (obj) {
            is Int -> JavaValue.newBuilder().setInt(obj.toLong()).build()
            is Long -> JavaValue.newBuilder().setInt(obj).build()
            is Boolean -> JavaValue.newBuilder().setBool(obj).build()
            is Double -> JavaValue.newBuilder().setFloat(obj).build()
            is Float -> JavaValue.newBuilder().setFloat(obj.toDouble()).build()
            else -> throw IllegalArgumentException(obj.toString())
        }
    }

    fun fromJavaValue(type: Class<*>, javaValue: JavaValue): Any? {
        return when (javaValue.valueCase) {
            JavaValue.ValueCase.INT -> {
                when (type) {
                    Byte::class.javaPrimitiveType, Byte::class.java -> javaValue.int.toByte()
                    Short::class.javaPrimitiveType, Short::class.java -> javaValue.int.toShort()
                    Int::class.javaPrimitiveType, Int::class.java -> javaValue.int.toInt()
                    Long::class.javaPrimitiveType, Long::class.java -> javaValue.int
                    else -> throw IllegalArgumentException(javaValue.int.toString())
                }
            }
            JavaValue.ValueCase.BOOL -> javaValue.bool
            JavaValue.ValueCase.FLOAT -> {
                when (type) {
                    Float::class.javaPrimitiveType, Float::class.java -> javaValue.float.toFloat()
                    Double::class.javaPrimitiveType, Double::class.java -> javaValue.float
                    else -> throw IllegalArgumentException(javaValue.float.toString())
                }
            }

            JavaValue.ValueCase.OBJECT_ID -> getObject(javaValue.objectId)
            else -> null
        }
    }

    companion object {
        private val PRIMITIVE_CLASSES = arrayOf<Class<*>>(
            Int::class.javaPrimitiveType!!,
            Long::class.javaPrimitiveType!!,
            Short::class.javaPrimitiveType!!,
            Byte::class.javaPrimitiveType!!,
            Boolean::class.javaPrimitiveType!!,
            Double::class.javaPrimitiveType!!,
            Float::class.javaPrimitiveType!!,
            Char::class.javaPrimitiveType!!
        )
    }
}
