package com.diana.bachelorthesis.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.FragmentIntroAuthBinding
import com.diana.bachelorthesis.utils.BasicFragment

class IntroAuthFragment : Fragment(), BasicFragment {
    private val TAG: String = IntroAuthFragment::class.java.name

    private var _binding: FragmentIntroAuthBinding? = null
    private val binding get() = _binding!!

    internal lateinit var layouts: ArrayList<Int>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "IntroAuthFragment is onCreateView")
        _binding = FragmentIntroAuthBinding.inflate(inflater, container, false)
        val root: View = binding.root

        layouts = arrayListOf(R.layout.intro_slide_1, R.layout.intro_slide_2, R.layout.intro_slide_3)

        val introScreensAdapter = IntroScreensAdapter()
        binding.viewPager.adapter = introScreensAdapter
        binding.springDotsIndicator.attachTo(binding.viewPager)

        initListeners()

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "IntroAuthFragment is onActivityCreated")
        setAppbar()
    }

    override fun initListeners() {
        binding.btnLogin.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.nav_login)
        }

        binding.btnSignup.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.nav_register)
        }
    }

    override fun setAppbar() {
        requireActivity().findViewById<TextView>(R.id.titleAppBar)?.apply {
            visibility = View.GONE
        }
        requireActivity().findViewById<ImageView>(R.id.logoApp)?.apply {
            visibility = View.VISIBLE
        }
        requireActivity().findViewById<ImageButton>(R.id.iconAppBar)?.apply {
            visibility = View.INVISIBLE
        }
    }


    // make adapter as inner class because I need activity context
    inner class IntroScreensAdapter: PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val layoutInflater = requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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