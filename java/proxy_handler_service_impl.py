from typing import List

from java.generated.commands.proxy_handler_service import ProxyHandlerService
from java.generated.commands.reflection_service import ReflectionService
from java.java_method import JavaMethod
from java.java_value import JavaValue, get_value_from_protobuf, convert_value_to_protobuf
from java.proxy_handler import ProxyHandler


class ProxyHandlerServiceImpl(ProxyHandlerService):
    def __init__(self, reflection_service: ReflectionService, proxy_handler: ProxyHandler):
        self.__reflection_service = reflection_service
        self.__proxy_handler = proxy_handler

    def invoke(self, method_id: int, arguments: List[JavaValue]) -> JavaValue:
        method = JavaMethod(method_id, self.__reflection_service)
        argument_wrappers = [get_value_from_protobuf(self.__reflection_service, argument) for argument in arguments]
        result_wrapper = self.__proxy_handler(method, argument_wrappers)
        return convert_value_to_protobuf(result_wrapper)
