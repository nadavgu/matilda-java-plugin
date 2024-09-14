package org.matilda.java.services.reflection;

import javax.inject.Inject;
import java.util.Random;

public class ObjectIdGenerator {
    @Inject
    Random mRandom;

    @Inject
    ObjectIdGenerator() {}


    public long generate(Object ignoredObject) {
        return mRandom.nextLong();
    }
}
