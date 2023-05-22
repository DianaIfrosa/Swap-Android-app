package com.diana.bachelorthesis.view

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.apachat.loadingbutton.core.customViews.CircularProgressButton
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.adapters.ItemsRecyclerViewAdapter
import com.diana.bachelorthesis.databinding.FragmentProposalPageBinding
import com.diana.bachelorthesis.model.ItemCategory
import com.diana.bachelorthesis.model.ItemExchange
import com.diana.bachelorthesis.utils.BasicFragment
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.utils.OneParamCallback
import com.diana.bachelorthesis.viewmodel.ProposalPageViewModel
import com.squareup.picasso.Picasso
import java.lang.Exception

class ProposalPageFragment : Fragment(), BasicFragment {
    private val TAG: String = ProposalPageFragment::class.java.name
    private var _binding: FragmentProposalPageBinding? = null

    private val binding get() = _binding!!
    private lateinit var proposalPageViewModel: ProposalPageViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "ProposalPageFragment is onCreateView")

        _binding = FragmentProposalPageBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.progressBar.visibility = View.VISIBLE
        binding.scrollView.visibility = View.GONE
        binding.itemGiven.visibility = View.GONE
        binding.layoutButtons.visibility = View.GONE
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "ProposalPageFragment is onViewCreated")
        setSubPageAppbar(requireActivity(), getString(R.string.proposal))
        initViewModel()
    }

    private fun initViewModel() {
        proposalPageViewModel = ViewModelProvider(this)[ProposalPageViewModel::class.java]
        proposalPageViewModel.item1 = ProposalPageFragmentArgs.fromBundle(requireArguments()).item1
        proposalPageViewModel.item2 = ProposalPageFragmentArgs.fromBundle(requireArguments()).item2
        proposalPageViewModel.proposal =
            ProposalPageFragmentArgs.fromBundle(requireArguments()).proposal
        proposalPageViewModel.currentUser = (requireActivity() as MainActivity).getCurrentUser()!!
        proposalPageViewModel.getUserMakingProposalData(object : NoParamCallback {
            override fun onComplete() {
                binding.progressBar.visibility = View.GONE
                binding.itemGiven.visibility = View.GONE
                binding.scrollView.visibility = View.VISIBLE
                binding.layoutButtons.visibility = View.VISIBLE
                updateUI()
                initListeners()
            }

            override fun onError(e: Exception?) {
                Toast.makeText(requireActivity(), getString(R.string.something_failed), Toast.LENGTH_LONG)
                    .show()
            }

        })
    }

    private fun updateUI() {
        // button text
        if (!proposalPageViewModel.proposal.confirmation1 || !proposalPageViewModel.proposal.confirmation2) {
            binding.btnAcceptProposal.text = getString(R.string.accept_proposal)
        } else {
            binding.btnAcceptProposal.text = getString(R.string.proposal_accepted)
        }

        if (proposalPageViewModel.userMakingProposal.email == proposalPageViewModel.currentUser.email) {
            // the current user was the one making the proposal
            binding.btnAcceptProposal.text = getString(R.string.proposal_accepted)
        }

        // user making proposal
        Picasso.get().load(proposalPageViewModel.userMakingProposal.profilePhoto)
            .into(binding.user2Photo)
        binding.user2Name.text = proposalPageViewModel.userMakingProposal.name

        if (proposalPageViewModel.proposal.itemId2 != null) {
            updateUIForExchange()
        } else {
            updateUIForDonation()
        }

    }

    private fun updateUIForDonation() {
        binding.root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.yellow_pale))
        binding.item2Layout.visibility = View.GONE

        binding.item1 = proposalPageViewModel.item1

        binding.item1Photos.setImageList(
            ItemsRecyclerViewAdapter.getPhotos(proposalPageViewModel.item1),
            ScaleTypes.CENTER_CROP
        )

        binding.textAction.text = getString(R.string.wants_to_receive_donation)

        // manufacture section
        if (proposalPageViewModel.item1.year != null) {
            binding.item1ManufactureSection.visibility = View.VISIBLE
            binding.item1ManufactureYear.text = proposalPageViewModel.item1.year.toString()
        } else {
            binding.item1ManufactureSection.visibility = View.GONE
        }

        binding.item1ExchangePreferencesSection.visibility = View.GONE
    }

    private fun updateUIForExchange() {
        binding.root.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.purple_pale))
        binding.item1Layout.visibility = View.VISIBLE

        binding.item1 = proposalPageViewModel.item1
        binding.item2 = proposalPageViewModel.item2

        binding.item1Photos.setImageList(
            ItemsRecyclerViewAdapter.getPhotos(proposalPageViewModel.item1),
            ScaleTypes.CENTER_CROP
        )
        binding.item2Photos.setImageList(
            ItemsRecyclerViewAdapter.getPhotos(proposalPageViewModel.item2!!),
            ScaleTypes.CENTER_CROP
        )

        binding.textAction.text = getString(R.string.wants_to_exchange)

        // manufacture section
        if (proposalPageViewModel.item1.year != null) {
            binding.item1ManufactureSection.visibility = View.VISIBLE
            binding.item1ManufactureYear.text = proposalPageViewModel.item1.year.toString()
        } else {
            binding.item1ManufactureSection.visibility = View.GONE
        }

        if (proposalPageViewModel.item2!!.year != null) {
            binding.item2ManufactureSection.visibility = View.VISIBLE
            binding.item2ManufactureYear.text = proposalPageViewModel.item2!!.year.toString()
        } else {
            binding.item2ManufactureSection.visibility = View.GONE
        }

        // exchange preferences
        var value = ""
        (proposalPageViewModel.item1 as ItemExchange).exchangePreferences.forEach {
            val name = ItemCategory.getTranslatedName(requireActivity(), it)
            value += "$name, "
        }
        if (value.isEmpty()) {
            binding.item1ExchangePreferencesSection.visibility = View.GONE
        } else {
            binding.item1ExchangePreferencesSection.visibility = View.VISIBLE
            value = value.substring(0, value.length - 2)
            binding.item1ExchangePreferences.text = value
        }

        value = ""
        (proposalPageViewModel.item2 as ItemExchange).exchangePreferences.forEach {
            val name = ItemCategory.getTranslatedName(requireActivity(), it)
            value += "$name, "
        }
        if (value.isEmpty()) {
            binding.item2ExchangePreferencesSection.visibility = View.GONE
        } else {
            binding.item2ExchangePreferencesSection.visibility = View.VISIBLE
            value = value.substring(0, value.length - 2)
            binding.item2ExchangePreferences.text = value
        }
    }

    override fun initListeners() {

        binding.item1Photos.setItemClickListener(object : ItemClickListener {
            override fun onItemSelected(position: Int) {
                Log.d(TAG, "Clicked on photos from item 1!")
                val action =
                    ProposalPageFragmentDirections.actionProposalPageFragmentToNavPhotos(
                        proposalPageViewModel.item1
                    )
                requireView().findNavController().navigate(action)
            }
        })


        binding.item2Photos.setItemClickListener(object : ItemClickListener {
            override fun onItemSelected(position: Int) {
                Log.d(TAG, "Clicked on photos from item 2!")
                val action =
                    ProposalPageFragmentDirections.actionProposalPageFragmentToNavPhotos(
                        proposalPageViewModel.item2!!
                    )
                requireView().findNavController().navigate(action)
            }
        })

        binding.btnAcceptProposal.setOnClickListener {
            if ((proposalPageViewModel.currentUser.email == proposalPageViewModel.proposal.userId1 && !proposalPageViewModel.proposal.confirmation1) || (proposalPageViewModel.currentUser.email == proposalPageViewModel.proposal.userId2 && !proposalPageViewModel.proposal.confirmation2)
            ) {

                val alertDialog = AlertDialog.Builder(requireActivity())
                    .setTitle(getString(R.string.accept_proposal))
                    .setMessage(getString(R.string.alert_accept_proposal))
                    .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                        dialog.cancel()
                        (it as CircularProgressButton).startAnimation()

                        proposalPageViewModel.confirmItemsAreStillAvailable(object: OneParamCallback<Boolean> {
                            override fun onComplete(value: Boolean?) {

                                if (value == true) {
                                    proposalPageViewModel.confirmProposal(object : NoParamCallback {
                                        override fun onComplete() {
                                            it.doneLoadingAnimation(
                                                R.color.green_light,
                                                ContextCompat.getDrawable(
                                                    requireActivity(),
                                                    R.drawable.ic_done
                                                )!!.toBitmap()
                                            )
                                        }

                                        override fun onError(e: Exception?) {
                                            it.revertAnimation()
                                            Toast.makeText(
                                                requireActivity(),
                                                getString(R.string.something_failed),
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    })
                                } else {
                                    binding.scrollView.visibility = View.GONE
                                    binding.layoutButtons.visibility = View.GONE
                                    binding.itemGiven.visibility = View.VISIBLE
                                }
                            }

                            override fun onError(e: Exception?) {
                                it.revertAnimation()
                                Toast.makeText(requireActivity(), getString(R.string.something_failed), Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                    .setNegativeButton(getString(R.string.no), null)
                    .setIcon(R.drawable.ic_done)

                alertDialog.show()
            } else {
                Log.d(TAG, "Proposal already accepted")
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "ProposalPageFragment is onDestroyView")
        _binding = null
    }

}