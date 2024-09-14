from typing import Callable, List

from java.java_method import JavaMethod
from java.java_value import JavaValue, OptionalJavaValue

ProxyHandler = Callable[[JavaMethod, List[JavaValue]], OptionalJavaValue]
