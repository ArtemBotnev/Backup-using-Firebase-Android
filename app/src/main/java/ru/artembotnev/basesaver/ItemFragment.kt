package ru.artembotnev.basesaver

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView

import kotlinx.android.synthetic.main.item_fragment.*

import java.util.*


/**
 * Created by Artem Botnev on 11.12.2017.
 */

class ItemFragment : Fragment() {
    companion object {
        private const val ID = "item_id"

        fun newInstance(id: UUID): Fragment {
            val args = Bundle()
            args.putSerializable(ID, id)
            val fragment = ItemFragment()
            fragment.arguments = args

            return fragment
        }
    }

    private lateinit var clothesItem: ClothesItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = arguments!!.getSerializable(ID) as UUID
        clothesItem = Wardrobe.getInstance(context!!).getItem(id) ?:
                throw NullPointerException("clothes item is null!")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.item_fragment, container, false)

    @SuppressLint("Recycle")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //Title text
        title.setText(clothesItem.title)
        title.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?,
                                           start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clothesItem.title = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        //Select item spinner
        itemType.setSelection(clothesItem.type)
        itemType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?, position: Int, id: Long) {
                clothesItem.type = position
            }
        }

        //Select size spinner
        itemSize.setSelection(clothesItem.size)
        itemSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?, position: Int, id: Long) {
                clothesItem.size = position
            }
        }

        //Select color
        with(clothesItem) {
            val colorArray = resources.obtainTypedArray(R.array.clothes_colors)
            val color = colorArray.getColor(colorId, 0)

            itemColor.setBackgroundColor(color)

            itemColor.setOnClickListener {
                colorId++
                colorId %= 7 //change rainbow's color
                it.setBackgroundColor(colorArray.getColor(colorId, 0))
            }
        }

        //Delete button
        delete.setOnClickListener {
            Wardrobe.getInstance(context!!).delete(clothesItem)
            activity!!.finish()
        }
    }

    override fun onPause() {
        super.onPause()
        Wardrobe.getInstance(context!!).update(clothesItem)
    }
}