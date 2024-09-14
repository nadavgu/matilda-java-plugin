package org.matilda.java.services.reflection

import org.matilda.commands.MatildaCommand
import org.matilda.commands.MatildaDynamicService
import org.matilda.java.services.reflection.protobuf.JavaValue

@MatildaDynamicService
fun interface ProxyHandlerService {
    @MatildaCommand
    operator fun invoke(methodId: Long, argumentIds: List<JavaValue>): JavaValue
}
