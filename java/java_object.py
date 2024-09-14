from typing import TYPE_CHECKING

from java.generated.commands.reflection_service import ReflectionService

if TYPE_CHECKING:
    from java.java_class import JavaClass


class JavaObject:
    def __init__(self, reflection_service: ReflectionService, object_id: int):
        self.__object_id = object_id
        self.__reflection_service = reflection_service

    @property
    def object_id(self) -> int:
        return self.__object_id

    def get_class(self) -> 'JavaClass':
        from java.java_class import JavaClass
        return JavaClass(self.__reflection_service, self.__reflection_service.get_object_class(self.__object_id))
