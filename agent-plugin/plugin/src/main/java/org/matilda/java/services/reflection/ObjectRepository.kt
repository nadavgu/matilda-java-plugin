package org.matilda.java.services.reflection

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObjectRepository @Inject constructor() {
    @Inject
    lateinit var mObjectIdGenerator: ObjectIdGenerator

    private val mObjects: MutableMap<Long, Any> = mutableMapOf()


    fun add(obj: Any): Long {
        val id = mObjectIdGenerator.generate(obj)
        mObjects[id] = obj
        return id
    }

    operator fun get(id: Long) = mObjects.getOrElse(id) {
        throw NoSuchElementException(id.toString())
    }

    fun remove(id: Long) {
        mObjects.remove(id)
    }
}
