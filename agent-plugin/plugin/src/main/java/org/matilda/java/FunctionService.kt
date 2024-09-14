package org.matilda.java

import org.matilda.commands.MatildaCommand
import org.matilda.commands.MatildaDynamicService

@MatildaDynamicService
interface FunctionService {
    @MatildaCommand
    fun apply(value: Int): Int
}
