from functools import cached_property

from java.generated.commands.reflection_service import ReflectionService
from java.java_object import JavaObject
from java.java_type import JavaType
from java.java_value import JavaValue, convert_value_to_protobuf, get_value_from_protobuf


class JavaField(JavaObject):
    def __init__(self, field_id: int, reflection_service: ReflectionService):
        super().__init__(reflection_service, field_id)
        self.__reflection_service = reflection_service

    def __str__(self):
        from java.java_type import get_type_name
        return f"{'static ' if self.is_static else ''}{get_type_name(self.type)} {self.name}"

    def __repr__(self):
        return f"JavaField({str(self)})"

    @cached_property
    def name(self) -> str:
        return self.__reflection_service.get_field_name(self.object_id)

    @cached_property
    def type(self) -> JavaType:
        from java.java_type import get_type_from_protobuf
        return get_type_from_protobuf(self.__reflection_service,
                                      self.__reflection_service.get_field_type(self.object_id))

    @cached_property
    def is_static(self) -> bool:
        return self.__reflection_service.is_field_static(self.object_id)

    def get(self, receiver: JavaObject) -> JavaValue:
        result = self.__reflection_service.get_field_value(self.object_id, receiver.object_id)
        return get_value_from_protobuf(self.__reflection_service, result)

    def get_static(self) -> JavaValue:
        result = self.__reflection_service.get_static_field_value(self.object_id)
        return get_value_from_protobuf(self.__reflection_service, result)

    def set(self, receiver: JavaObject, value: JavaValue) -> None:
        self.__reflection_service.set_field_value(self.object_id, receiver.object_id, convert_value_to_protobuf(value))

    def set_static(self, value: JavaValue) -> None:
        self.__reflection_service.set_static_field_value(self.object_id, convert_value_to_protobuf(value))
