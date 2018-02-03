package ru.artembotnev.basesaver

import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment

import java.util.*

/**
 * Created by Artem Botnev on 17.12.2017.
 */

class ItemActivity : ParentActivity() {
    companion object {
        private const val ITEM_ID = "item_id"

        fun newIntent(context: Context, item: ClothesItem): Intent {
            val intent = Intent(context, ItemActivity::class.java)
            intent.putExtra(ITEM_ID, item.id)

            return intent
        }
    }

    override fun createFragment(): Fragment {
        val id = intent.getSerializableExtra(ITEM_ID) as UUID

        return ItemFragment.newInstance(id)
    }
}