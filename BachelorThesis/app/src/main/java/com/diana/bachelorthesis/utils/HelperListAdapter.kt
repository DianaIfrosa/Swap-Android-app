package com.diana.bachelorthesis.utils

import android.view.ViewGroup
import android.widget.ListView

class HelperListAdapter {
    companion object {
        fun adjustListViewSize(listView: ListView) {
            if (listView.adapter == null)
                return
            var totalHeight = 0
            // calculate total height by summing each view's height
            for (size in 0 until listView.adapter.count) {
                val listItem = listView.adapter.getView(size, null, listView)
                listItem.measure(0, 0)
                totalHeight += listItem.measuredHeight
            }
            // set listview item in adapter
            val layoutParams: ViewGroup.LayoutParams = listView.layoutParams
            layoutParams.height = totalHeight + listView.dividerHeight * (listView.adapter.count - 1)
            listView.layoutParams = layoutParams
        }
    }
}