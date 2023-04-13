package com.diana.bachelorthesis.view

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.FragmentRecommendationsBinding
import com.diana.bachelorthesis.utils.BasicFragment
import com.diana.bachelorthesis.viewmodel.RecommendationsViewModel

class RecommendationsFragment : Fragment(), BasicFragment {

    private val TAG: String = RecommendationsFragment::class.java.name
    private var _binding: FragmentRecommendationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val recommendationsViewModel =
            ViewModelProvider(this).get(RecommendationsViewModel::class.java)

        _binding = FragmentRecommendationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textRecommendations
        recommendationsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "RecommendationsFragment is onActivityCreated")
        setAppbar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun initListeners() {
        TODO("Not yet implemented")
    }

    override fun setAppbar() {
        requireActivity().findViewById<TextView>(R.id.titleAppBar)?.apply {
            visibility = View.VISIBLE
            text = requireView().findNavController().currentDestination!!.label
        }
        requireActivity().findViewById<ImageView>(R.id.logoApp)?.apply {
            visibility = View.GONE
        }
        requireActivity().findViewById<ImageButton>(R.id.profilePhotoAppBar)?.apply {
            visibility = View.VISIBLE
        }
    }

}