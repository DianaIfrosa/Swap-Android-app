package com.diana.bachelorthesis.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.utils.HelperListAdapter


class ListAdapterPreferences(private val context: Context, private val listItems: ArrayList<String>) :BaseAdapter() {
    override fun getCount(): Int {
        return listItems.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun getItems(): ArrayList<String> {
        return listItems
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val finalView = LayoutInflater.from(context).inflate(R.layout.list_row, parent, false)
        val textview = finalView.findViewById<TextView>(R.id.list_item_textview)
        val btn = finalView.findViewById<ImageButton>(R.id.list_item_delete)
        btn.tag = position
        btn.setOnClickListener {
            val index: Int = it.tag as Int
            listItems.removeAt(index)
            HelperListAdapter.adjustListViewSize(parent as ListView)
            notifyDataSetChanged()
        }
        textview.text = listItems[position]
        return finalView
    }


}