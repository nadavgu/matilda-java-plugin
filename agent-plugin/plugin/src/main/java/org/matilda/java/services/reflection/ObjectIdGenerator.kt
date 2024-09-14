package org.matilda.java.services.reflection

import java.util.*
import javax.inject.Inject

class ObjectIdGenerator @Inject internal constructor() {
    @Inject
    lateinit var mRandom: Random
    fun generate(ignored: Any?): Long {
        return mRandom.nextLong()
    }
}
