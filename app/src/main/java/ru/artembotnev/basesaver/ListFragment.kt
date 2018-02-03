package ru.artembotnev.basesaver

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView

import kotlinx.android.synthetic.main.list_fragment.*

/**
 * Created by Artem Botnev on 08.12.2017.
 */


class ListFragment : Fragment() {
    companion object {
        fun newInstance() = ListFragment()
    }

    private lateinit var recyclerView: RecyclerView
    private var rvAdapter: ClothesAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.list_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //floating action button, create new item of clothes and add one to list
        fab!!.setOnClickListener({
            val item = ClothesItem()
            Wardrobe.getInstance(context!!).add(item)

            startItemActivity(item)
        })

        //recycler
        recyclerView = recycler
        recyclerView.layoutManager = LinearLayoutManager(activity) //add layout manager
        updateUI()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    fun updateUI() {
        val clothes = Wardrobe.getInstance(context!!).getClothes()
        if (rvAdapter == null) {
            rvAdapter = ClothesAdapter(clothes)
            recyclerView.adapter = rvAdapter
        } else {
            rvAdapter!!.clothesList = clothes
            rvAdapter!!.notifyDataSetChanged()
        }
    }

    private fun startItemActivity(item: ClothesItem) {
        val intent = ItemActivity.newIntent(context!!, item)
        startActivity(intent)
    }

    //View holder
    private inner class ClothesHolder(inflater: LayoutInflater, parent: ViewGroup) :
            RecyclerView.ViewHolder(inflater.inflate(R.layout.item_layout, parent, false)),
            View.OnClickListener {
        lateinit var clothesItem: ClothesItem

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            startItemActivity(clothesItem)
        }

        fun bing(item: ClothesItem) {
            val itemTitle = itemView.findViewById<TextView>(R.id.title)
            val itemColor = itemView.findViewById<FrameLayout>(R.id.color)
            val itemType = itemView.findViewById<TextView>(R.id.type)
            val itemSize = itemView.findViewById<TextView>(R.id.size)

            val colorArray = resources.obtainTypedArray(R.array.clothes_colors)

            clothesItem = item
            with(clothesItem) {
                itemTitle.text = title
                itemColor.setBackgroundColor(colorArray.getColor(colorId, 0))
                itemType.text = resources.getStringArray(R.array.clothes_type)[type]
                itemSize.text = resources.getStringArray(R.array.clothes_size)[size]
            }
        }
    }

    //Adapter
    private inner class ClothesAdapter(var clothesList: List<ClothesItem>) :
            RecyclerView.Adapter<ClothesHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ClothesHolder {
            val inflater = LayoutInflater.from(activity)

            return ClothesHolder(inflater, parent!!)
        }

        override fun onBindViewHolder(holder: ClothesHolder?, position: Int) {
            holder!!.bing(clothesList[position])
        }

        override fun getItemCount(): Int = clothesList.size
    }
}
