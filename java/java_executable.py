from functools import cached_property
from typing import List

from java.generated.commands.reflection_service import ReflectionService
from java.java_object import JavaObject
from java.java_type import JavaType


class JavaExecutable(JavaObject):
    def __init__(self, executable_id: int, reflection_service: ReflectionService):
        super().__init__(reflection_service, executable_id)
        self.__reflection_service = reflection_service

    @cached_property
    def name(self) -> str:
        return self.__reflection_service.get_executable_name(self.object_id)

    @cached_property
    def parameter_types(self) -> List[JavaType]:
        from java.java_type import get_type_from_protobuf
        return [get_type_from_protobuf(self.__reflection_service, protobuf) for protobuf
                in self.__reflection_service.get_executable_parameter_types(self.object_id)]
