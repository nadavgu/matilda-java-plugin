from typing import List

import pytest
from java.java_value import JavaValue

from java.java_method import JavaMethod

from java.java_primitive_type import JavaPrimitiveType
from matilda.exceptions.command_failed_exception import CommandFailedException

from java.java_plugin import JavaPlugin


class TestSanity:
    def test_find_class(self, plugin: JavaPlugin):
        integer_class = plugin.find_class("java.lang.Integer")
        assert integer_class.name == 'java.lang.Integer'

    def test_find_invalid_class_throws(self, plugin: JavaPlugin):
        with pytest.raises(CommandFailedException):
            plugin.find_class("java.lang.Hellooooo")

    def test_superclass(self, plugin: JavaPlugin):
        assert repr(plugin.find_class("java.lang.Integer").superclass) == repr(plugin.find_class("java.lang.Number"))

    def test_interfaces(self, plugin: JavaPlugin):
        assert repr(plugin.find_class("java.lang.Thread").interfaces) == repr([plugin.find_class("java.lang.Runnable")])

    def test_get_static_field(self, plugin: JavaPlugin):
        integer_class = plugin.find_class("java.lang.Integer")
        field = integer_class.get_field("MAX_VALUE")
        assert field.is_static
        assert field.name == "MAX_VALUE"
        assert field.type == JavaPrimitiveType.INT

    def test_get_static_field_value(self, plugin: JavaPlugin):
        integer_class = plugin.find_class("java.lang.Integer")
        field = integer_class.get_field("MAX_VALUE")
        assert field.get_static() == 2 ** 31 - 1

    def test_get_static_method(self, plugin: JavaPlugin):
        integer_class = plugin.find_class("java.lang.Integer")
        method = integer_class.get_method("valueOf", JavaPrimitiveType.INT)
        assert method.is_static
        assert method.name == "valueOf"
        assert repr(method.return_type) == repr(integer_class)
        assert method.parameter_types == [JavaPrimitiveType.INT]

    def test_invoke_static_method(self, plugin: JavaPlugin):
        integer_class = plugin.find_class("java.lang.Integer")
        string_class = plugin.find_class("java.lang.String")
        method = integer_class.get_method("parseInt", string_class)
        assert method.invoke_static("123") == 123

    def test_get_instance_method(self, plugin: JavaPlugin):
        integer_class = plugin.find_class("java.lang.Integer")
        method = integer_class.get_method("intValue")
        assert not method.is_static
        assert method.name == "intValue"
        assert method.return_type == JavaPrimitiveType.INT
        assert method.parameter_types == []

    def test_invoke_instance_method(self, plugin: JavaPlugin):
        integer_class = plugin.find_class("java.lang.Integer")
        integer_instance = integer_class.get_method("valueOf", JavaPrimitiveType.INT).invoke_static(3)
        method = integer_class.get_method("intValue")
        assert method.invoke(integer_instance) == 3

    def test_get_constructor(self, plugin: JavaPlugin):
        integer_class = plugin.find_class("java.lang.Integer")
        constructor = integer_class.get_constructor(JavaPrimitiveType.INT)
        assert constructor.name == "java.lang.Integer"
        assert constructor.parameter_types == [JavaPrimitiveType.INT]

    def test_create_new_instance(self, plugin: JavaPlugin):
        integer_class = plugin.find_class("java.lang.Integer")
        constructor = integer_class.get_constructor(JavaPrimitiveType.INT)
        integer_instance = constructor.new_instance(3)
        assert integer_class.get_method("intValue").invoke(integer_instance) == 3

    def test_get_fields(self, plugin: JavaPlugin):
        integer_class = plugin.find_class("java.lang.Integer")
        assert any([field.name == "value" for field in integer_class.get_fields()])

    def test_get_methods(self, plugin: JavaPlugin):
        integer_class = plugin.find_class("java.lang.Integer")
        assert any([method.name == "highestOneBit" for method in integer_class.get_methods()])

    def test_get_constructors(self, plugin: JavaPlugin):
        integer_class = plugin.find_class("java.lang.Integer")
        assert any([constructor.parameter_types == [JavaPrimitiveType.INT]
                    for constructor in integer_class.get_constructors()])

    def test_new_proxy_instance(self, plugin: JavaPlugin):
        runnable_class = plugin.find_class("java.lang.Runnable")
        thread_class = plugin.find_class("java.lang.Thread")

        called_methods = []

        def handler(method: JavaMethod, args: List[JavaValue]):
            called_methods.append((method.name, args))

        proxy = plugin.new_proxy_instance([runnable_class], handler)
        assert repr(proxy.get_class().interfaces) == repr([plugin.find_class("java.lang.Runnable")])
        thread = thread_class.get_constructor(runnable_class).new_instance(proxy)
        thread_class.get_method("start").invoke(thread)
        thread_class.get_method("join").invoke(thread)
        assert called_methods == [("run", [])]
