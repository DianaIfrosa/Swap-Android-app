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
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.adapters.ItemsRecyclerViewAdapter
import com.diana.bachelorthesis.databinding.FragmentHistoryDonationBinding
import com.diana.bachelorthesis.utils.BasicFragment
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.viewmodel.HistoryEventViewModel
import com.squareup.picasso.Picasso
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class HistoryDonationFragment : Fragment(), BasicFragment {
    private val TAG: String = HistoryDonationFragment::class.java.name
    private var _binding: FragmentHistoryDonationBinding? = null

    private val binding get() = _binding!!
    private lateinit var historyEventViewModel: HistoryEventViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "HistoryDonationFragment is onCreateView")
        _binding = FragmentHistoryDonationBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "HistoryDonationFragment is onActivityCreated")

        setSubPageAppbar(requireActivity(), getString(R.string.donation_made))

        historyEventViewModel = ViewModelProvider(this)[HistoryEventViewModel::class.java]
        getNavigationArguments()
        historyEventViewModel.currentUser = (requireActivity() as MainActivity).getCurrentUser()!!

        historyEventViewModel.getDonationReceiverData(object: NoParamCallback {
            override fun onComplete() {
                binding.progressBar.visibility = View.GONE
                binding.mainLayout.visibility = View.VISIBLE

                updateUIElements()

                binding.donationReceiverName.text = historyEventViewModel.otherOwner.name
                Picasso.get().load(historyEventViewModel.otherOwner.profilePhoto).into(binding.donationReceiverPicture)

                initListeners()
            }

            override fun onError(e: Exception?) {
                Toast.makeText(requireContext(), getString( R.string.something_failed), Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "HistoryDonationFragment is onViewCreated")

        binding.progressBar.visibility = View.VISIBLE
        binding.mainLayout.visibility = View.GONE
    }

    private fun updateUIElements() {

        binding.itemTitle.text = historyEventViewModel.item1.name
        binding.photoCarousel.setImageList(ItemsRecyclerViewAdapter.getPhotos(historyEventViewModel.item1))

        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.donationDate.text = dateFormatter.format(
            historyEventViewModel.history.date.toDate()
        ).toString()
    }

    private fun getNavigationArguments() {
        historyEventViewModel.item1 = HistoryDonationFragmentArgs.fromBundle(requireArguments()).item
        historyEventViewModel.history = HistoryDonationFragmentArgs.fromBundle(requireArguments()).history
    }

    override fun initListeners() {
        binding.donationReceiverPicture.setOnClickListener {
            val action = HistoryDonationFragmentDirections.actionNavHistoryDonationFragmentToNavOwnerProfile(historyEventViewModel.donationReceiver)
            requireView().findNavController().navigate(action)
        }

        binding.donationReceiverName.setOnClickListener {
            val action = HistoryDonationFragmentDirections.actionNavHistoryDonationFragmentToNavOwnerProfile(historyEventViewModel.donationReceiver)
            requireView().findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "HistoryDonationFragment is onDestroyView")
        _binding = null
    }
}