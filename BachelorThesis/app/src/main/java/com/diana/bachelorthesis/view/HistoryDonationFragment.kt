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
import com.diana.bachelorthesis.databinding.FragmentHistoryDonationBinding
import com.diana.bachelorthesis.utils.BasicFragment
import com.diana.bachelorthesis.viewmodel.DonationEventViewModel
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class HistoryDonationFragment : Fragment(), BasicFragment {
    private val TAG: String = HistoryDonationFragment::class.java.name
    private var _binding: FragmentHistoryDonationBinding? = null

    private val binding get() = _binding!!
    private lateinit var donationEventViewModel: DonationEventViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "HistoryDonationFragment is onCreateView")
        _binding = FragmentHistoryDonationBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.progressBar.visibility = View.VISIBLE
        binding.mainLayout.visibility = View.GONE
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "HistoryDonationFragment is onViewCreated")
        setSubPageAppbar(requireActivity(), getString(R.string.donation_made))
        donationEventViewModel = ViewModelProvider(this)[DonationEventViewModel::class.java]
        getNavigationArguments()
        donationEventViewModel.currentUser = (requireActivity() as MainActivity).getCurrentUser()!!

        donationEventViewModel.getDonationReceiverData()
        donationEventViewModel.donationReceiver.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.progressBar.visibility = View.GONE
                binding.mainLayout.visibility = View.VISIBLE

                updateUIElements()

                binding.donationReceiverName.text = donationEventViewModel.donationReceiver.value!!.name
                Picasso.get().load(donationEventViewModel.donationReceiver.value!!.profilePhoto).into(binding.donationReceiverPicture)

                initListeners()
            } else {
                Toast.makeText(requireActivity(), getString( R.string.something_failed), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateUIElements() {

        binding.itemTitle.text = donationEventViewModel.item1.name
        binding.photoCarousel.setImageList(ItemsRecyclerViewAdapter.getPhotos(donationEventViewModel.item1), ScaleTypes.CENTER_CROP)

        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.donationDate.text = dateFormatter.format(
            donationEventViewModel.history.date.toDate()
        ).toString()
    }

    private fun getNavigationArguments() {
        donationEventViewModel.item1 = HistoryDonationFragmentArgs.fromBundle(requireArguments()).item
        donationEventViewModel.history = HistoryDonationFragmentArgs.fromBundle(requireArguments()).history
    }

    override fun initListeners() {
        binding.donationReceiverPicture.setOnClickListener {
            if (donationEventViewModel.donationReceiver.value != null) {
                val newParam = donationEventViewModel.donationReceiver.value!!.clone()
                val action = HistoryDonationFragmentDirections.actionNavHistoryDonationFragmentToNavOwnerProfile(newParam)
                requireView().findNavController().navigate(action)
            }
        }

        binding.donationReceiverName.setOnClickListener {
            if (donationEventViewModel.donationReceiver.value != null) {
                val newParam = donationEventViewModel.donationReceiver.value!!.clone()
                val action =
                    HistoryDonationFragmentDirections.actionNavHistoryDonationFragmentToNavOwnerProfile(newParam)
                requireView().findNavController().navigate(action)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "HistoryDonationFragment is onDestroyView")
        _binding = null
    }
}