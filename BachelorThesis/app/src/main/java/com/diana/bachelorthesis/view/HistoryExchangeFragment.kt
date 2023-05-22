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
import com.diana.bachelorthesis.databinding.FragmentHistoryExchangeBinding
import com.diana.bachelorthesis.utils.BasicFragment
import com.diana.bachelorthesis.viewmodel.ExchangeEventViewModel
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*


class HistoryExchangeFragment : Fragment(), BasicFragment {
    private val TAG: String = HistoryExchangeFragment::class.java.name
    private var _binding: FragmentHistoryExchangeBinding? = null

    private val binding get() = _binding!!
    private lateinit var historyEventViewModel: ExchangeEventViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "HistoryExchangeFragment is onCreateView")
        _binding = FragmentHistoryExchangeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.progressBar.visibility = View.VISIBLE
        binding.mainLayout.visibility = View.GONE
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "HistoryExchangeFragment is onViewCreated")
        setSubPageAppbar(requireActivity(), getString(R.string.exchange))
        historyEventViewModel = ViewModelProvider(this)[ExchangeEventViewModel::class.java]
        getNavigationArguments()
        historyEventViewModel.currentUser = (requireActivity() as MainActivity).getCurrentUser()!!

        historyEventViewModel.getOtherOwnerData()
        historyEventViewModel.otherOwner.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.progressBar.visibility = View.GONE
                binding.mainLayout.visibility = View.VISIBLE

                updateUIElements()

                binding.ownerName.text = historyEventViewModel.otherOwner.value!!.name
                Picasso.get().load(historyEventViewModel.otherOwner.value!!.profilePhoto).into(binding.ownerPicture)

                initListeners()
            } else {
                Toast.makeText(requireActivity(), getString( R.string.something_failed), Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun updateUIElements() {
        if (historyEventViewModel.item1.owner != historyEventViewModel.currentUser.email) {
            historyEventViewModel.swapItems()
        }
        binding.item1Title.text = historyEventViewModel.item1.name
        binding.photoCarousel1.setImageList(ItemsRecyclerViewAdapter.getPhotos(historyEventViewModel.item1), ScaleTypes.CENTER_CROP)
        binding.item2Title.text = historyEventViewModel.item2!!.name
        binding.photoCarousel2.setImageList(ItemsRecyclerViewAdapter.getPhotos(historyEventViewModel.item2!!), ScaleTypes.CENTER_CROP)

        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.exchangeDate.text = dateFormatter.format(
            historyEventViewModel.history.date.toDate()
        ).toString()
    }

    private fun getNavigationArguments() {
        historyEventViewModel.item1 = HistoryExchangeFragmentArgs.fromBundle(requireArguments()).item1
        historyEventViewModel.item2 = HistoryExchangeFragmentArgs.fromBundle(requireArguments()).item2
        historyEventViewModel.history = HistoryExchangeFragmentArgs.fromBundle(requireArguments()).history
    }

    override fun initListeners() {
       binding.ownerName.setOnClickListener {
           if (historyEventViewModel.otherOwner.value != null) {
               val newUser = historyEventViewModel.otherOwner.value!!.clone()
               val action =
                   HistoryExchangeFragmentDirections.actionNavHistoryExchangeFragmentToNavOwnerProfile(
                       newUser
                   )
               requireView().findNavController().navigate(action)
           }
       }

        binding.ownerPicture.setOnClickListener {
            if (historyEventViewModel.otherOwner.value != null) {
                val newUser = (historyEventViewModel.otherOwner.value)!!.clone()
                val action =
                    HistoryExchangeFragmentDirections.actionNavHistoryExchangeFragmentToNavOwnerProfile(
                       newUser
                    )
                requireView().findNavController().navigate(action)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "HistoryExchangeFragment is onDestroyView")
        _binding = null
    }
}