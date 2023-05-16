package com.diana.bachelorthesis.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.diana.bachelorthesis.adapters.ItemsRecyclerViewAdapter
import com.diana.bachelorthesis.databinding.FragmentPhotosBinding
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.utils.BasicFragment
import com.diana.bachelorthesis.viewmodel.ItemPageViewModel

class PhotosFragment : Fragment(), BasicFragment {
    private val TAG: String = PhotosFragment::class.java.name

    private var _binding: FragmentPhotosBinding? = null
    private val binding get() = _binding!!
    private lateinit var item: Item

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "PhotosFragment is onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "PhotosFragment is onCreateView")
        _binding = FragmentPhotosBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "PhotosFragment is onViewCreated")
        item = PhotosFragmentArgs.fromBundle(requireArguments()).item
        binding.photoCarousel.setImageList(ItemsRecyclerViewAdapter.getPhotos(item))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "PhotosFragment is on onActivityCreated")
        setSubPageAppbar(requireActivity(), item.name)

    }

    override fun initListeners() {}

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "PhotosFragment is on onDestroyView")
        _binding = null
    }
}