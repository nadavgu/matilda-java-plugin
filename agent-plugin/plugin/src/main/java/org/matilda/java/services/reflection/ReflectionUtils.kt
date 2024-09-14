package org.matilda.java.services.reflection;


import org.matilda.java.services.reflection.protobuf.JavaType;
import org.matilda.java.services.reflection.protobuf.JavaValue;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectionUtils {
    @Inject
    ObjectRepository mObjectRepository;

    @Inject
    ReflectionUtils() {}

    long register(Object object) {
        return mObjectRepository.add(object);
    }

    Class<?> getClass(long classId) {
        return (Class<?>) mObjectRepository.get(classId);
    }

    Executable getExecutable(long id) {
        return (Executable) mObjectRepository.get(id);
    }

    Method getMethod(long methodId) {
        return (Method) mObjectRepository.get(methodId);
    }

    Constructor<?> getConstructor(long constructorId) {
        return (Constructor<?>) mObjectRepository.get(constructorId);
    }

    Field getField(long fieldId) {
        return (Field) mObjectRepository.get(fieldId);
    }

    Object getObject(long objectId) {
        return mObjectRepository.get(objectId);
    }

    Class<?> fromJavaType(JavaType type) throws ClassNotFoundException {
        if (type.hasPrimitiveClassName()) {
            return getPrimitiveClass(type.getPrimitiveClassName());
        } else {
            return getClass(type.getClassId());
        }
    }

    JavaType toJavaType(Class<?> type) {
        JavaType.Builder typeBuilder = JavaType.newBuilder();
        if (type.isPrimitive()) {
            typeBuilder.setPrimitiveClassName(type.getCanonicalName());
        } else {
            typeBuilder.setClassId(register(type));
        }
        return typeBuilder.build();
    }

    private static final Class<?>[] PRIMITIVE_CLASSES = new Class<?>[] {
            int.class,
            long.class,
            short.class,
            byte.class,
            boolean.class,
            double.class,
            float.class,
            char.class
    };

    private Class<?> getPrimitiveClass(String name) throws ClassNotFoundException {
        return Arrays.stream(PRIMITIVE_CLASSES)
                .filter(clazz -> clazz.getName().equals(name))
                .findFirst()
                .orElseThrow(ClassNotFoundException::new);
    }


    JavaValue toJavaValue(Class<?> type, Object object) {
        if (!type.isPrimitive()) {
            return JavaValue.newBuilder().setObjectId(register(object)).build();
        }

        if (object == null) {
            return JavaValue.newBuilder().build();
        } if (object instanceof Integer ) {
            return JavaValue.newBuilder().setInt((Integer) object).build();
        } else if (object instanceof Long) {
            return JavaValue.newBuilder().setInt((Long) object).build();
        } else if (object instanceof Boolean) {
            return JavaValue.newBuilder().setBool((Boolean) object).build();
        } else if (object instanceof Double) {
            return JavaValue.newBuilder().setFloat((Double) object).build();
        } else if (object instanceof Float) {
            return JavaValue.newBuilder().setFloat((Float) object).build();
        } else {
            throw new IllegalArgumentException(object.toString());
        }
    }

    Object fromJavaValue(Class<?> type, JavaValue javaValue) {
        switch (javaValue.getValueCase()) {
            case INT: {
                if (type.equals(byte.class) || type.equals(Byte.class)) {
                    return (byte) javaValue.getInt();
                } else if (type.equals(short.class) || type.equals(Short.class)) {
                    return (short) javaValue.getInt();
                } else if (type.equals(int.class) || type.equals(Integer.class)) {
                    return (int) javaValue.getInt();
                } else if (type.equals(long.class) || type.equals(Long.class)) {
                    return javaValue.getInt();
                } else {
                    throw new IllegalArgumentException(String.valueOf(javaValue.getInt()));
                }
            }
            case BOOL: return javaValue.getBool();
            case FLOAT: {
                if (type.equals(float.class) || type.equals(Float.class)) {
                    return (float) javaValue.getFloat();
                } else if (type.equals(double.class) || type.equals(Double.class)) {
                    return javaValue.getFloat();
                } else {
                    throw new IllegalArgumentException(String.valueOf(javaValue.getFloat()));
                }
            }
            case OBJECT_ID: return getObject(javaValue.getObjectId());
            default: return null;
        }
    }
}
