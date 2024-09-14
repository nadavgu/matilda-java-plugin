package org.matilda.java.services.reflection

import javax.inject.Inject
import kotlin.random.Random

class ObjectIdGenerator @Inject internal constructor() {
    @Inject
    lateinit var mRandom: Random
    fun generate(ignored: Any?): Long {
        return mRandom.nextLong()
    }
}
