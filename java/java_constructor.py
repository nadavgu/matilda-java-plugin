from java.generated.commands.reflection_service import ReflectionService

from java.java_executable import JavaExecutable
from java.java_object import JavaObject
from java.java_value import JavaValue, convert_value_to_protobuf


class JavaConstructor(JavaExecutable):
    def __init__(self, constructor_id: int, reflection_service: ReflectionService):
        super().__init__(constructor_id, reflection_service)
        self.__reflection_service = reflection_service

    def __str__(self):
        from java.java_type import get_type_name
        return f"{self.name}({', '.join(map(get_type_name, self.parameter_types))})"

    def __repr__(self):
        return f"JavaConstructor({str(self)})"

    def new_instance(self, *args: JavaValue) -> JavaObject:
        result = self.__reflection_service.create_new_instance(self.object_id,
                                                               [convert_value_to_protobuf(arg) for arg in args])
        return JavaObject(self.__reflection_service, result)
