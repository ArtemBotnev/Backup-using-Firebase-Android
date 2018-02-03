package ru.artembotnev.basesaver.database

/**
 * Created by Artem Botmev on 14.12.2017.
 */

class DbSchema {
    object ItemTable {
        val NAME = "items"

        object Cols {
            val UUID = "uuid"
            val TITLE = "title"
            val TYPE = "type"
            val SIZE = "size"
            val COLOR = "color"
        }
    }
}