package ru.artembotnev.basesaver.fbHelper

import android.util.Log

import com.google.firebase.database.*
import com.google.gson.*

import ru.artembotnev.basesaver.ClothesItem

import java.lang.reflect.Type
import java.util.*

/**
 * Created by Artem Botnev on 09.01.2018.
 */

class FiredataClothes(data: String, private val userId: String) {
    companion object {
        private const val TAG = "FiredataClothes"
        private const val USERS = "clients"
    }

    private var database: DatabaseReference // instance of Firebase database
    private var gson: Gson // instance of Gson with custom converter

    init {
        database = FirebaseDatabase.getInstance().getReference(data)
        val builder = GsonBuilder()
        gson = builder.registerTypeAdapter(ClothesItem::class.java, Converter())
                .create()
    }

    // create json for each ClothesItem and send list of it to database
    fun send(clothes: List<ClothesItem>) {
        val clothesJson = clothes.map { gson.toJson(it) }
        database.child(USERS).child(userId).setValue(clothesJson)
    }

    // receive data from database, parse json and create list of ClothesItem
    fun receive(recipient: OnReceiveDataListener) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                // receive data from database
                val type = object : GenericTypeIndicator<ArrayList<String>>() {}
                val value = dataSnapshot?.child(USERS)?.child(userId)?.getValue(type)
                // parse json and create new list
                val clothes = value?.map { gson.fromJson(it, ClothesItem::class.java) }
                // send list to recipient
                recipient.receiveFiredata(clothes)

                Log.i(TAG, "receive")
                Log.i(TAG, clothes.toString())
            }

            override fun onCancelled(error: DatabaseError?) {
                Log.w(TAG, "loadPost:onCancelled", error?.toException())
            }
        })
    }

    //custom json converter for ClothesItem (GSON library)
    private inner class Converter :
            JsonSerializer<ClothesItem>, JsonDeserializer<ClothesItem> {
        private val ID = "id"
        private val TITLE = "title"
        private val TYPE = "type"
        private val SIZE = "size"
        private val COLOR_ID = "color_id"

        override fun serialize(src: ClothesItem?, typeOfSrc: Type?,
                               context: JsonSerializationContext?): JsonElement {
            val obj = JsonObject()
            with(obj) {
                with(src!!) {
                    addProperty(ID, id.toString())
                    addProperty(TITLE, title)
                    addProperty(TYPE, type)
                    addProperty(SIZE, size)
                    addProperty(COLOR_ID, colorId)
                }
            }

            return obj
        }

        override fun deserialize(json: JsonElement?, typeOfT: Type?,
                                 context: JsonDeserializationContext?): ClothesItem {
            val obj = json?.asJsonObject ?: throw NullPointerException("JsonElement is null")

            val id = obj.get(ID).asString
            val title = obj.get(TITLE).asString
            val type = obj.get(TYPE).asInt
            val size = obj.get(SIZE).asInt
            val color = obj.get(COLOR_ID).asInt

            //create new ClothesItem with definite uuid
            val item = ClothesItem(UUID.fromString(id))
            item.title = title
            item.type = type
            item.size = size
            item.colorId = color

            return item
        }
    }

    // must be used by recipient class
    interface OnReceiveDataListener {
        fun receiveFiredata(items: List<ClothesItem>?)
    }
}