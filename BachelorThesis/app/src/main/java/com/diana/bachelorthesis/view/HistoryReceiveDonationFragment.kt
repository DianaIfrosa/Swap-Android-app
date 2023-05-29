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
import com.bumptech.glide.Glide
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.adapters.ItemsRecyclerViewAdapter
import com.diana.bachelorthesis.databinding.FragmentHistoryReceiveDonationBinding
import com.diana.bachelorthesis.utils.BasicFragment
import com.diana.bachelorthesis.viewmodel.ReceiveDonationEventViewModel
import java.text.SimpleDateFormat
import java.util.*

class HistoryReceiveDonationFragment : Fragment(), BasicFragment {
    private val TAG: String = HistoryReceiveDonationFragment::class.java.name
    private var _binding: FragmentHistoryReceiveDonationBinding? = null

    private val binding get() = _binding!!
    private lateinit var historyEventViewModel: ReceiveDonationEventViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "HistoryReceiveDonationFragment is onCreateView")
        _binding = FragmentHistoryReceiveDonationBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.progressBar.visibility = View.VISIBLE
        binding.mainLayout.visibility = View.GONE
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "HistoryReceiveDonationFragment is onViewCreated")
        setSubPageAppbar(requireActivity(), getString(R.string.donation_received))
        historyEventViewModel = ViewModelProvider(this)[ReceiveDonationEventViewModel::class.java]
        getNavigationArguments()
        historyEventViewModel.currentUser = (requireActivity() as MainActivity).getCurrentUser()!!
        historyEventViewModel.getDonationMakerData()

        historyEventViewModel.donationMaker.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.progressBar.visibility = View.GONE
                binding.mainLayout.visibility = View.VISIBLE

                updateUIElements()

                binding.donationMakerName.text = historyEventViewModel.donationMaker.value!!.name
                Glide.with(context).load(historyEventViewModel.donationMaker.value!!.profilePhoto)
                    .centerCrop()
                    .into(binding.donationMakerPicture)

                initListeners()
            } else {
                Toast.makeText(
                    requireActivity(),
                    getString(R.string.something_failed),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    }

    private fun updateUIElements() {

        binding.itemTitle.text = historyEventViewModel.item1.name
        binding.photoCarousel.setImageList(
            ItemsRecyclerViewAdapter.getPhotos(historyEventViewModel.item1),
            ScaleTypes.CENTER_CROP
        )

        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.donationDate.text = dateFormatter.format(
            historyEventViewModel.history.date.toDate()
        ).toString()
    }

    private fun getNavigationArguments() {
        historyEventViewModel.item1 =
            HistoryDonationFragmentArgs.fromBundle(requireArguments()).item
        historyEventViewModel.history =
            HistoryDonationFragmentArgs.fromBundle(requireArguments()).history
    }

    override fun initListeners() {
        binding.donationMakerPicture.setOnClickListener {
            if (historyEventViewModel.donationMaker.value != null) {
                val newUser = (historyEventViewModel.donationMaker.value)!!.clone()
                val action =
                    HistoryReceiveDonationFragmentDirections.actionNavHistoryReceiveDonationFragmentToNavOwnerProfile(
                        newUser
                    )
                requireView().findNavController().navigate(action)
            }
        }

        binding.donationMakerName.setOnClickListener {
            if (historyEventViewModel.donationMaker.value != null) {
                val newUser = (historyEventViewModel.donationMaker.value)!!.clone()
                val action =
                    HistoryReceiveDonationFragmentDirections.actionNavHistoryReceiveDonationFragmentToNavOwnerProfile(
                       newUser
                    )
                requireView().findNavController().navigate(action)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "HistoryReceiveDonationFragment is onDestroyView")
        _binding = null
    }
}