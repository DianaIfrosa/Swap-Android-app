package com.diana.bachelorthesis.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.FragmentIntroAuthBinding
import com.diana.bachelorthesis.utils.BasicFragment


class IntroAuthFragment : Fragment(), BasicFragment {
    private val TAG: String = IntroAuthFragment::class.java.name

    private var _binding: FragmentIntroAuthBinding? = null
    private val binding get() = _binding!!
    lateinit var layouts: ArrayList<Int>
    private var myViewPagerAdapter: MyViewPagerAdapter? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "IntroAuthFragment is onCreateView")
        _binding = FragmentIntroAuthBinding.inflate(inflater, container, false)
        val root: View = binding.root

        layouts =
            arrayListOf(R.layout.intro_slide_1, R.layout.intro_slide_2, R.layout.intro_slide_3)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "IntroAuthFragment is onViewCreated")
        setAuthOrProfileAppbar(
            requireActivity(),
            requireView().findNavController().currentDestination!!.label.toString()
        )
        // adding bottom dots
        addBottomDots(0)

        myViewPagerAdapter = MyViewPagerAdapter()
        binding.viewPager.adapter = myViewPagerAdapter
        binding.viewPager.addOnPageChangeListener(viewPagerPageChangeListener)
        initListeners()
    }

    override fun initListeners() {
        binding.btnSkip.setOnClickListener{ launchAuthScreen() }
        binding.btnNext.setOnClickListener {
            // checking for last page if true launch MainActivity
            val current = getItem()
            if (current < layouts.size) {
                // move to next screen
                binding.viewPager.currentItem = current
            } else {
                launchAuthScreen()
            }
        }
    }

    private fun addBottomDots(currentPage: Int) {
        val dots = arrayListOf<ImageView>()
        val colorActive = resources.getColor(R.color.purple_medium)
        val colorInactive = resources.getColor(R.color.purple_dark)
        (binding.layoutDots as LinearLayout).removeAllViews()

        for (i in 0 until layouts.size) {
            val newDot = ImageView(requireActivity())
            newDot.apply{
                setImageDrawable(resources.getDrawable(R.drawable.ic_circle_unfilled))
            }

            (binding.layoutDots as LinearLayout).addView(newDot)
            dots.add(newDot)
        }
        if (dots.size > 0) dots[currentPage].setImageDrawable(resources.getDrawable(R.drawable.ic_circle_filled))
    }

    private fun getItem(): Int {
        return binding.viewPager.currentItem + 1
    }

    private fun launchAuthScreen() {
        val action = IntroAuthFragmentDirections.actionNavIntroAuthToNavAuth()
        requireView().findNavController().navigate(action)
    }

    //  viewpager change listener
    private var viewPagerPageChangeListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            addBottomDots(position)

            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.size - 1) {
                // last page. make button text to GOT IT
                binding.btnNext.text = getString(R.string.ok)
                binding.btnSkip.visibility = View.INVISIBLE
            } else {
                // still pages are left
                binding.btnNext.text = getString(R.string.next)
                binding.btnSkip.visibility = View.VISIBLE
            }
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
        override fun onPageScrollStateChanged(arg0: Int) {}
    }

    inner class MyViewPagerAdapter : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view: View = layoutInflater.inflate(layouts[position], container, false)
            container.addView(view)
            return view
        }

        override fun getCount(): Int {
            return layouts.size
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val view = `object` as View
            container.removeView(view)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "IntroAuthFragment is onDestroyView")
        _binding = null
    }

}