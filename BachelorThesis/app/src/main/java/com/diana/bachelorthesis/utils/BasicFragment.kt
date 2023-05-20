package com.diana.bachelorthesis.utils

import android.app.Activity
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.diana.bachelorthesis.R

interface BasicFragment {
    fun initListeners()
    fun setMainPageAppbar(activity: Activity, title: String) {
        activity.findViewById<TextView>(R.id.titleAppBarMainPage)?.apply {
            visibility = View.VISIBLE
            text = title
        }
        activity.findViewById<TextView>(R.id.titleAppBarSubpage)?.apply {
            visibility = View.GONE
        }
        activity.findViewById<ImageView>(R.id.logoApp)?.apply {
            visibility = View.GONE
        }
        activity.findViewById<ImageButton>(R.id.iconAppBar)?.apply {
            visibility = View.VISIBLE
        }
    }

    fun setSubPageAppbar(activity: Activity, title: String) {
        activity.findViewById<TextView>(R.id.titleAppBarMainPage)?.apply {
            visibility = View.GONE
        }
        activity.findViewById<TextView>(R.id.titleAppBarSubpage)?.apply {
            visibility = View.VISIBLE
            text = title
        }
        activity.findViewById<ImageView>(R.id.logoApp)?.apply {
            visibility = View.GONE
        }
        activity.findViewById<ImageButton>(R.id.iconAppBar)?.apply {
            visibility = View.VISIBLE
        }
    }

    fun setHomeAppbar(activity: Activity) {
        activity.findViewById<TextView>(R.id.titleAppBarMainPage)?.apply {
            visibility = View.GONE
        }
        activity.findViewById<TextView>(R.id.titleAppBarSubpage)?.apply {
            visibility = View.GONE
        }
        activity.findViewById<ImageView>(R.id.logoApp)?.apply {
            visibility = View.VISIBLE
        }
        activity.findViewById<ImageButton>(R.id.iconAppBar)?.apply {
            visibility = View.VISIBLE
        }
    }

    fun setAuthOrProfileAppbar(activity: Activity, title: String) {
        activity.findViewById<TextView>(R.id.titleAppBarMainPage)?.apply {
            visibility = View.VISIBLE
            text = title
        }
        activity.findViewById<ImageView>(R.id.logoApp)?.apply {
            visibility = View.GONE
        }

        activity.findViewById<TextView>(R.id.titleAppBarSubpage)?.apply {
            visibility = View.GONE
        }
        activity.findViewById<ImageButton>(R.id.iconAppBar)?.apply {
            visibility = View.INVISIBLE
        }
    }
}