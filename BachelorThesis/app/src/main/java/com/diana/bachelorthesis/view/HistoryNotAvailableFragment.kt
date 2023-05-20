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
import com.diana.bachelorthesis.adapters.CardsHistoryAdapter
import com.diana.bachelorthesis.databinding.FragmentHistoryNotAvailableBinding
import com.diana.bachelorthesis.model.History
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.viewmodel.HistoryNotAvailableViewModel
import com.diana.bachelorthesis.viewmodel.HistoryViewModel
import java.lang.Exception

class HistoryNotAvailableFragment : Fragment() {
    private val TAG: String = HistoryNotAvailableFragment::class.java.name
    private var _binding: FragmentHistoryNotAvailableBinding? = null
    private lateinit var historyViewModel: HistoryNotAvailableViewModel
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "HistoryNotAvailableFragment is onCreateView")

        _binding = FragmentHistoryNotAvailableBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.itemsAdapter = CardsHistoryAdapter(null, listOf(), listOf(),  requireContext()) { _, _, _ ->}
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "HistoryNotAvailableFragment is onViewCreated")

        historyViewModel =
            ViewModelProvider(this)[HistoryNotAvailableViewModel::class.java]
        historyViewModel.currentUser = (requireActivity() as MainActivity).getCurrentUser()!!

//        historyViewModel.notAvailableItemsHistory.observe(viewLifecycleOwner) {
//            updateRecyclerView(it)
////            (parentFragment as HistoryFragment).scrollRecyclerView(binding.recyclerView)
//        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "HistoryNotAvailableFragment is onStart")
//        updateRecyclerView(listOf(), true)

    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "HistoryNotAvailableFragment is onResume")

        if (historyViewModel.notAvailableItemsHistory.size != 0 && historyViewModel.notAvailableItemsHistory.size == historyViewModel.itemsPairs.size) {
            // already loaded the items in previous visits to tab
            Log.d(TAG, "Load items already existing")
            updateRecyclerView(historyViewModel.notAvailableItemsHistory, historyViewModel.itemsPairs)
        } else {
            updateRecyclerView(arrayListOf(), arrayListOf(),true)
        }

        historyViewModel.populateItemsNotAvailable(object: NoParamCallback {
            override fun onComplete() {
                updateRecyclerView(historyViewModel.notAvailableItemsHistory, historyViewModel.itemsPairs)
            }

            override fun onError(e: Exception?) {
                Toast.makeText(context, getString(R.string.something_failed), Toast.LENGTH_LONG).show()
            }
        })

    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "HistoryNotAvailableFragment is onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "HistoryNotAvailableFragment is onStop")
//        historyViewModel.lastScrollPosition =
//            (binding.recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
    }

    private fun updateRecyclerView(historyList: List<History>, itemsList: List<Pair<Item, Item?>>, progressBarAppears: Boolean = false) {
        if (progressBarAppears) {
            binding.progressBar.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.INVISIBLE
            binding.textNoItems.visibility = View.INVISIBLE
            binding.textNumberCards.text = resources.getString(R.string.loading)
        } else if (historyList.isNotEmpty()) {
            binding.recyclerView.visibility = View.VISIBLE
            binding.progressBar.visibility = View.INVISIBLE
            binding.textNoItems.visibility = View.INVISIBLE
            binding.itemsAdapter =
                CardsHistoryAdapter(historyViewModel.currentUser, historyList, itemsList, requireContext()) { item1, item2, history ->
                    if (item2 != null) {
                        // exchange event
                        val action =
                            HistoryFragmentDirections.actionNavHistoryToNavHistoryExchangeFragment(
                                item1,
                                item2,
                                history
                            )
                        requireView().findNavController().navigate(action)
                    } else { // donation event
                        if (history.donationReceiverEmail == historyViewModel.currentUser.email) {
                            // current user was the donation receiver
                            val action = HistoryFragmentDirections.actionNavHistoryToNavHistoryReceiveDonationFragment(item1, history)
                            requireView().findNavController().navigate(action)
                        } else {
                            // current user did the donation
                            val action = HistoryFragmentDirections.actionNavHistoryToNavHistoryDonationFragment(item1, history)
                            requireView().findNavController().navigate(action)
                        }
                    }
                }
            binding.textNumberCards.text = historyList.size.toString()
        } else {
            binding.progressBar.visibility = View.INVISIBLE
            binding.recyclerView.visibility = View.INVISIBLE
            binding.textNoItems.visibility = View.VISIBLE
            binding.textNumberCards.text = historyList.size.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "HistoryNotAvailableFragment is onDestroyView")
        _binding = null
    }
}