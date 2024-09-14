from functools import cached_property

from java.generated.commands.reflection_service import ReflectionService
from java.java_executable import JavaExecutable
from java.java_object import JavaObject
from java.java_value import JavaValue, convert_value_to_protobuf, get_value_from_protobuf


class JavaMethod(JavaExecutable):
    def __init__(self, method_id: int, reflection_service: ReflectionService):
        super().__init__(method_id, reflection_service)
        self.__reflection_service = reflection_service

    def __str__(self):
        from java.java_type import get_type_name
        return f"{'static ' if self.is_static else ''}{self.name}({', '.join(map(get_type_name, self.parameter_types))})"

    def __repr__(self):
        return f"JavaMethod({str(self)})"

    @cached_property
    def is_static(self) -> bool:
        return self.__reflection_service.is_method_static(self.object_id)

    def invoke(self, receiver: JavaObject, *args: JavaValue) -> JavaValue:
        result = self.__reflection_service.invoke_method(self.object_id, receiver.object_id,
                                                         [convert_value_to_protobuf(arg) for arg in args])
        return get_value_from_protobuf(self.__reflection_service, result)

    def invoke_static(self, *args: JavaValue) -> JavaValue:
        result = self.__reflection_service.invoke_static_method(self.object_id,
                                                                [convert_value_to_protobuf(arg) for arg in args])
        return get_value_from_protobuf(self.__reflection_service, result)
