package com.diana.bachelorthesis.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.adapters.ItemsRecyclerViewAdapter
import com.diana.bachelorthesis.databinding.FragmentHistoryAvailableBinding
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.viewmodel.HistoryAvailableViewModel
import com.diana.bachelorthesis.viewmodel.HistoryViewModel
import java.lang.Exception

class HistoryAvailableFragment : Fragment() {
    private val TAG: String = HistoryAvailableFragment::class.java.name
    private var _binding: FragmentHistoryAvailableBinding? = null
    private lateinit var historyViewModel: HistoryAvailableViewModel

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "HistoryAvailableFragment is onCreateView")
        _binding = FragmentHistoryAvailableBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.itemsAdapter = ItemsRecyclerViewAdapter(listOf(), requireContext()) {}
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "HistoryAvailableFragment is onViewCreated")

        historyViewModel =
            ViewModelProvider(this)[HistoryAvailableViewModel::class.java]
        historyViewModel.currentUser = (requireActivity() as MainActivity).getCurrentUser()!!
//        historyViewModel.availableItems.observe(viewLifecycleOwner) {
//            updateRecyclerView(it)
////            (parentFragment as HistoryFragment).scrollRecyclerView(binding.recyclerView)
//        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "HistoryAvailableFragment is onStart")

    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "HistoryAvailableFragment is onResume")

        if (historyViewModel.availableItems.size != 0) {
            // already loaded the items in previous visits to tab
            updateRecyclerView(historyViewModel.availableItems)
        } else {
            updateRecyclerView(arrayListOf(), true)
        }

        historyViewModel.populateItemsAvailable(object: NoParamCallback {
            override fun onComplete() {
                updateRecyclerView(historyViewModel.availableItems)
            }

            override fun onError(e: Exception?) {
                Toast.makeText(context, getString(R.string.something_failed), Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "HistoryAvailableFragment is onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "HistoryAvailableFragment is onStop")
//        historyViewModel.lastScrollPosition =
//            (binding.recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
    }

    private fun updateRecyclerView(items: List<Item>, progressBarAppears: Boolean = false) {
        if (progressBarAppears) {
            binding.progressBar.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.INVISIBLE
            binding.textNoItems.visibility = View.INVISIBLE
            binding.textNumberItems.text = resources.getString(R.string.loading)
        } else if (items.isNotEmpty()) {
            binding.recyclerView.visibility = View.VISIBLE
            binding.progressBar.visibility = View.INVISIBLE
            binding.textNoItems.visibility = View.INVISIBLE
            binding.itemsAdapter =
                ItemsRecyclerViewAdapter(items, requireContext()) { item ->
                    val action = HistoryFragmentDirections.actionNavHistoryToNavItem(item)
                    requireView().findNavController().navigate(action)
                }
            binding.textNumberItems.text = items.size.toString()
        } else {
            binding.progressBar.visibility = View.INVISIBLE
            binding.recyclerView.visibility = View.INVISIBLE
            binding.textNoItems.visibility = View.VISIBLE
            binding.textNumberItems.text = items.size.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "HistoryAvailableFragment is onDestroyView")
        _binding = null
    }

}