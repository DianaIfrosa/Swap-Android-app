package com.diana.bachelorthesis.view

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.ActivityIntroAuthBinding


class IntroAuthActivity : AppCompatActivity() {
    private val TAG: String = IntroAuthActivity::class.java.name

    private var _binding: ActivityIntroAuthBinding? = null
    private val binding get() = _binding!!

    internal lateinit var layouts: ArrayList<Int>

    private var viewPagerPageChangeListener = object: ViewPager.OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            displayDots(position)
        }

        override fun onPageSelected(position: Int) {}

        override fun onPageScrollStateChanged(state: Int) {}

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "IntroAuthActivity is onCreate")

        _binding = ActivityIntroAuthBinding.inflate(layoutInflater)

        // make notification bar transparent
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        setContentView(binding.root)
        changeStatusBarColor()

        layouts = arrayListOf(R.layout.intro_slide_1, R.layout.intro_slide_2, R.layout.intro_slide_3)
        displayDots(0)

        val introScreensAdapter = IntroScreensAdapter()
        binding.viewPager.adapter = introScreensAdapter
        binding.viewPager.addOnPageChangeListener(viewPagerPageChangeListener)

        initListeners()
    }

    private fun initListeners() {
        binding.btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.btnSignup.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun displayDots(currentPage: Int) {
        val dots = arrayListOf<TextView>()

        binding.layoutDots.removeAllViews()
        for (index in 0 until layouts.size) {
            val dot = TextView(this)
            dot.setTextColor(resources.getColor(R.color.grey))
            dot.apply {
                text = Html.fromHtml("?")
                setTextColor(resources.getColor(R.color.grey)) // mark as not current
                textSize = 35F
            }
            dots.add(dot)
        }

        dots[currentPage].setTextColor(resources.getColor(R.color.grey_light)) // mark as current
    }
    private fun changeStatusBarColor() {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT

    }

    // make adapter as inner class because I need activity context
    inner class IntroScreensAdapter: PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater.inflate(layouts[position], container, false)
            container.addView(view)
            return view
        }

        override fun getCount(): Int {
            return layouts.size
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view == obj
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            val view = obj as View
            container.removeView(view)
        }
    }
}