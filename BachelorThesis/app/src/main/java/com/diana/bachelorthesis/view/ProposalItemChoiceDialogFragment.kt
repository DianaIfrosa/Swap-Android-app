package com.diana.bachelorthesis.view

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.adapters.ItemChoiceAdapter
import com.diana.bachelorthesis.adapters.ItemsRecyclerViewAdapter
import com.diana.bachelorthesis.databinding.FragmentEditProfileDialogBinding
import com.diana.bachelorthesis.databinding.FragmentProposalItemChoiceDialogBinding
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.utils.ListParamCallback
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.utils.ProfileOptionsListener
import com.diana.bachelorthesis.viewmodel.ItemChoiceViewModel
import java.lang.Exception

class ProposalItemChoiceDialogFragment : DialogFragment() {
    private val TAG: String = ProposalItemChoiceDialogFragment::class.java.name

    private var _binding: FragmentProposalItemChoiceDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var fragmentParent: ItemFragment
    private lateinit var toolbar: Toolbar
    private lateinit var itemChoiceViewModel: ItemChoiceViewModel
    private var adapter: ItemChoiceAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "ProposalItemChoiceDialogFragment is onCreate")
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "ProposalItemChoiceDialogFragment is onCreateView")
        _binding = FragmentProposalItemChoiceDialogBinding.inflate(inflater, container, false)
        val root: View = binding.root

        toolbar = root.findViewById(R.id.toolbar_dialog_choose_proposal)
        customizeToolbar()

        fragmentParent = parentFragment as ItemFragment
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "ProposalItemChoiceDialogFragment is onViewCreated")
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        updateRecyclerView(arrayListOf(), true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "ProposalItemChoiceDialogFragment is onActivityCreated")

        itemChoiceViewModel = ViewModelProvider(this)[ItemChoiceViewModel::class.java]
        itemChoiceViewModel.currentUser = (requireActivity() as MainActivity).getCurrentUser()!!
        itemChoiceViewModel.getCurrentUserObjects(object: ListParamCallback<Item> {
            override fun onComplete(values: ArrayList<Item>) {
                updateRecyclerView(values)
            }

            override fun onError(e: Exception?) {
                Toast.makeText(context, getString(R.string.something_failed), Toast.LENGTH_LONG).show()
            }
        })

    }

    fun updateRecyclerView(items: List<Item>, progressBarAppears: Boolean = false) {
        if (progressBarAppears) {
            binding.recyclerView.visibility = View.GONE
            binding.noItemsText.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE

        } else if (items.isNotEmpty()){
            binding.progressBar.visibility = View.GONE
            binding.noItemsText.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
            adapter =  ItemChoiceAdapter(items, requireContext())
            binding.itemsAdapter = adapter
        } else {
            binding.recyclerView.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
            binding.noItemsText.visibility = View.VISIBLE
        }
    }

    private fun customizeToolbar() {
        toolbar.title = getString(R.string.choose_item)
        toolbar.setTitleTextColor(resources.getColor(R.color.purple_dark))
        toolbar.setNavigationOnClickListener {
            dialog!!.dismiss()
        }
        toolbar.inflateMenu(R.menu.dialog_fragment_menu)
        toolbar.setOnMenuItemClickListener {
            if (adapter != null) {
                if (adapter!!.selectedPosition >= 0) {
                    val itemToPropose = itemChoiceViewModel.items[adapter!!.selectedPosition]
                    Log.d(TAG, "Item to propose: ${itemToPropose.name}")
                    // todo chat proposal and open chat
//                ProposalItemChoiceDialogFragmentDirections.actionProposalItemChoiceDialogFragmentToNavChatPageFragment()
                } else  {
                    // no selected item even though there are options
                    Toast.makeText(context, getString(R.string.must_choose_item), Toast.LENGTH_LONG).show()
                }
            } else {
                // no selected item and there are no options
                dialog!!.dismiss()
            }
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "ProposalItemChoiceDialogFragment is onDestroyView")
        _binding = null
    }
}