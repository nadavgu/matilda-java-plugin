package org.matilda.java.services.reflection;

import org.matilda.commands.MatildaCommand;
import org.matilda.commands.MatildaDynamicService;
import org.matilda.java.services.reflection.protobuf.JavaValue;

import java.util.List;

@MatildaDynamicService
public interface ProxyHandlerService {
    @MatildaCommand
    JavaValue invoke(long methodId, List<JavaValue> argumentIds);
}
