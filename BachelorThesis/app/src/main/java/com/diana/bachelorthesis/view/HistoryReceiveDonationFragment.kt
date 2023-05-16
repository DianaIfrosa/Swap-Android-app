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
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.adapters.ItemsRecyclerViewAdapter
import com.diana.bachelorthesis.databinding.FragmentHistoryReceiveDonationBinding
import com.diana.bachelorthesis.utils.BasicFragment
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.viewmodel.HistoryEventViewModel
import com.squareup.picasso.Picasso
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class HistoryReceiveDonationFragment : Fragment(), BasicFragment {
    private val TAG: String = HistoryReceiveDonationFragment::class.java.name
    private var _binding: FragmentHistoryReceiveDonationBinding? = null

    private val binding get() = _binding!!
    private lateinit var historyEventViewModel: HistoryEventViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "HistoryReceiveDonationFragment is onCreateView")
        _binding = FragmentHistoryReceiveDonationBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "HistoryReceiveDonationFragment is onActivityCreated")

        setSubPageAppbar(requireActivity(), getString(R.string.donation_received))

        historyEventViewModel = ViewModelProvider(this)[HistoryEventViewModel::class.java]
        getNavigationArguments()
        historyEventViewModel.currentUser = (requireActivity() as MainActivity).getCurrentUser()!!

        historyEventViewModel.getDonationMakerData(object: NoParamCallback {
            override fun onComplete() {
                binding.progressBar.visibility = View.GONE
                binding.mainLayout.visibility = View.VISIBLE

                updateUIElements()

                binding.donationMakerName.text = historyEventViewModel.otherOwner.name
                Picasso.get().load(historyEventViewModel.otherOwner.profilePhoto).into(binding.donationMakerPicture)

                initListeners()
            }

            override fun onError(e: Exception?) {
                Toast.makeText(requireContext(), getString( R.string.something_failed), Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "HistoryReceiveDonationFragment is onViewCreated")

        binding.progressBar.visibility = View.VISIBLE
        binding.mainLayout.visibility = View.GONE
    }

    private fun updateUIElements() {

        binding.itemTitle.text = historyEventViewModel.item1.name
        binding.photoCarousel.setImageList(ItemsRecyclerViewAdapter.getPhotos(historyEventViewModel.item1), ScaleTypes.CENTER_CROP)

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
        binding.donationMakerPicture.setOnClickListener {
            val action = HistoryReceiveDonationFragmentDirections.actionNavHistoryReceiveDonationFragmentToNavOwnerProfile(historyEventViewModel.otherOwner)
            requireView().findNavController().navigate(action)
        }

        binding.donationMakerName.setOnClickListener {
            val action = HistoryReceiveDonationFragmentDirections.actionNavHistoryReceiveDonationFragmentToNavOwnerProfile(historyEventViewModel.otherOwner)
            requireView().findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "HistoryReceiveDonationFragment is onDestroyView")
        _binding = null
    }
}