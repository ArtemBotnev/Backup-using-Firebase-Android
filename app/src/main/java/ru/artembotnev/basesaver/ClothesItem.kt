package ru.artembotnev.basesaver

import java.util.*

/**
 * Created by Artem Botnev on 10.12.2017.
 */

data class ClothesItem(val id: UUID) {
    constructor() : this(UUID.randomUUID())

    var title: String = "" // name this clothes item
    var type: Int = 0 // type of clothes
    var size: Int = 0 // size of one
    var colorId: Int = 0 // color of one

    override fun toString() = title
}