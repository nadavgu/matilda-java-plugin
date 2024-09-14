import time
from typing import List

from java.java_method import JavaMethod
from java.java_plugin import JavaPlugin
from java.java_value import JavaValue
from matilda.matilda import Matilda

if __name__ == '__main__':
    with Matilda().run_in_java_process(java_path='/home/user/Downloads/jre1.8.0_411/bin/java') as matilda_process:
        java_plugin: JavaPlugin = matilda_process.plugins.java
        runnable_class = java_plugin.find_class("java.lang.Runnable")
        thread_class = java_plugin.find_class("java.lang.Thread")
        thread_getid_method = thread_class.get_method("getId")

        def handler(method: JavaMethod, args: List[JavaValue]):
            if method.name == 'run':
                print(f"Running in new thread!")
                time.sleep(5)
                print("Finished running in new thread!")
            elif method.name == 'toString':
                return "Hellllooooo"
            else:
                print(f"What is this: {method.name}({args})")

        proxy = java_plugin.new_proxy_instance([runnable_class], handler)
        print(proxy)
        print(proxy.get_class())
        print(proxy.get_class().superclass)
        print(proxy.get_class().interfaces)
        thread = thread_class.get_constructor(runnable_class).new_instance(proxy)
        print(f"Created thread! new thread id: {thread_getid_method.invoke(thread)}")
        thread_class.get_method("start").invoke(thread)
        print("started new thread")
        thread_class.get_method("join").invoke(thread)
        print("finished waiting for new thread")
        print(java_plugin.find_class("java.lang.Object").get_method("toString").invoke(proxy))
