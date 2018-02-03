package ru.artembotnev.basesaver.database

import android.database.Cursor
import android.database.CursorWrapper

import ru.artembotnev.basesaver.ClothesItem
import ru.artembotnev.basesaver.database.DbSchema.ItemTable

import java.util.*

/**
 * Created by Artem Botnev on 14.12.2017.
 */

class ItemCursorWrapper(cursor: Cursor) : CursorWrapper(cursor) {
    fun getItem(): ClothesItem {
        val uuid = getString(getColumnIndex(ItemTable.Cols.UUID))
        val title = getString(getColumnIndex(ItemTable.Cols.TITLE))
        val type = getInt(getColumnIndex(ItemTable.Cols.TYPE))
        val size = getInt(getColumnIndex(ItemTable.Cols.SIZE))
        val color = getInt(getColumnIndex(ItemTable.Cols.COLOR))

        //create new ClothesItem with definite uuid
        val item = ClothesItem(UUID.fromString(uuid))
        item.title = title
        item.type = type
        item.size = size
        item.colorId = color

        return item
    }
}