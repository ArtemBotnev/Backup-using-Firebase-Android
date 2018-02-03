package ru.artembotnev.basesaver

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context

import ru.artembotnev.basesaver.database.DbSchema.ItemTable
import ru.artembotnev.basesaver.database.ItemBaseHelper
import ru.artembotnev.basesaver.database.ItemCursorWrapper

import java.util.*

import kotlin.collections.ArrayList

/**
 * Created by Artem Botnev on 07.12.2017.
 */

class Wardrobe private constructor(context: Context) {
    companion object {
        private var instance: Wardrobe? = null

        fun getInstance(c: Context): Wardrobe {
            return instance ?: Wardrobe(c)
        }
    }

    private val db = ItemBaseHelper(context).writableDatabase

    //get list of clothes
    fun getClothes(): List<ClothesItem> {
        val cursor = queryItem(null, null)
        val clothes = ArrayList<ClothesItem>()
        try {
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                clothes.add(cursor.getItem())
                cursor.moveToNext()
            }
        } finally {
            cursor.close()
        }

        return clothes
    }

    //get item clothes
    fun getItem(itemId: UUID): ClothesItem? {
        val cursor = queryItem(
                "${ItemTable.Cols.UUID}=? ",
                Array(1) { itemId.toString() }
        )

        try {
            return if (cursor.count == 0) {
                null
            } else {
                cursor.moveToFirst()
                cursor.getItem()
            }
        } finally {
            cursor.close()
        }
    }

    //add new item
    fun add(item: ClothesItem) {
        val values = getContentValues(item)
        db.insert(ItemTable.NAME, null, values)
    }

    //update item
    fun update(item: ClothesItem) {
        val values = getContentValues(item)
        db.update(ItemTable.NAME,
                values,
                "${ItemTable.Cols.UUID}=? ",
                Array(1) { item.id.toString() }
        )
    }

    //delete item
    fun delete(item: ClothesItem) {
        db.delete(ItemTable.NAME,
                "${ItemTable.Cols.UUID}=? ",
                Array(1) { item.id.toString() })
    }

    fun addList(clothes: List<ClothesItem>) {
        val innerClothes = getClothes()
        val newClothes: MutableList<ClothesItem> = ArrayList()
        // filling newClothes except duplicates
        clothes.filterNotTo(newClothes) { innerClothes.contains(it) }
        // add received items
        newClothes.forEach { add(it) }
    }

    //pack the values
    private fun getContentValues(item: ClothesItem): ContentValues {
        val contentValues = ContentValues()

        with(contentValues) {
            put(ItemTable.Cols.UUID, item.id.toString())
            put(ItemTable.Cols.TITLE, item.title)
            put(ItemTable.Cols.TYPE, item.type)
            put(ItemTable.Cols.SIZE, item.size)
            put(ItemTable.Cols.COLOR, item.colorId)
        }

        return contentValues
    }

    // get cursor wrapper
    @SuppressLint("Recycle")
    private fun queryItem(where: String?, whereArgs: Array<String>?): ItemCursorWrapper {
        val cursor = db.query(ItemTable.NAME,
                null,
                where,
                whereArgs,
                null,
                null,
                null
        )

        return ItemCursorWrapper(cursor)
    }
}