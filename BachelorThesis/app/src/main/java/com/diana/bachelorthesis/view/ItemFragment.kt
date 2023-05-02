package com.diana.bachelorthesis.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.adapters.ItemsRecyclerViewAdapter
import com.diana.bachelorthesis.databinding.FragmentItemBinding
import com.diana.bachelorthesis.model.ItemExchange
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.utils.BasicFragment
import com.diana.bachelorthesis.utils.LocationHelper
import com.diana.bachelorthesis.utils.OneParamCallback
import com.diana.bachelorthesis.viewmodel.ItemProfileViewModel
import com.diana.bachelorthesis.viewmodel.UserViewModel
import com.squareup.picasso.Picasso
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class ItemFragment : Fragment(), BasicFragment {

    private val TAG: String = ItemFragment::class.java.name
    private var _binding: FragmentItemBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<ItemFragmentArgs>()
    lateinit var userViewModel: UserViewModel
    lateinit var itemViewModel: ItemProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "ItemFragment is on onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "ItemFragment is on onCreateView")
        _binding = FragmentItemBinding.inflate(inflater, container, false)
        val root: View = binding.root
        getViewModels()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "ItemFragment is on onViewCreated")

        itemViewModel.item = ItemFragmentArgs.fromBundle(requireArguments()).ItemClicked

        userViewModel.getUserData(itemViewModel.item.owner, object: OneParamCallback<User> {
            override fun onComplete(value: User?) {
                if (value != null)
                    showItemOwnerDetails(value)
            }

            override fun onError(e: Exception?) {
               Log.w(TAG, "Item's owner with email ${itemViewModel.item.owner} could not been retrieved!")
            }
        })
        showItemDetails()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "ItemFragment is on onActivityCreated")
        initListeners()
        setSubPageAppbar(requireActivity(), itemViewModel.item.name)
    }

    private fun getViewModels() {
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        itemViewModel = ViewModelProvider(this)[ItemProfileViewModel::class.java]
    }

    override fun initListeners() {
        binding.btnFavorite.setOnClickListener {  view ->
            if (userViewModel.verifyUserLoggedIn()) {
                val drawable = binding.btnFavorite.drawable
                if (drawable.constantState!! == resources.getDrawable(R.drawable.ic_heart_filled).constantState) {
                    binding.btnFavorite.setImageResource(R.drawable.ic_heart_unfilled)
                    //TODO add logic
                } else {
                    binding.btnFavorite.setImageResource(R.drawable.ic_heart_filled)
                    //TODO add logic
                }
            } else {
                view.findNavController().navigate(R.id.nav_intro_auth)
            }
        }
    }

    private fun showItemOwnerDetails(owner: User) {
        binding.itemOwnerName.text = owner.name
        Picasso.get().load(owner.profilePhoto).into(binding.ownerPicture)
    }

    private fun showItemDetails() {
        binding.item = itemViewModel.item

        // photos
        binding.photoCarousel.setImageList(ItemsRecyclerViewAdapter.getPhotos(itemViewModel.item))
        val city = LocationHelper(requireContext()).getItemCity(itemViewModel.item.location)
        binding.itemAddress.text = city

        // post date
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.itemPostDate.text = dateFormatter.format(
            itemViewModel.item.postDate!!.toDate()
        ).toString()

        // manufacture year
        if (itemViewModel.item.year != null) {
            binding.itemManufactureSection.visibility = View.VISIBLE
            binding.itemManufactureYear.text = itemViewModel.item.year.toString()
        } else {
            binding.itemManufactureSection.visibility = View.GONE
        }

        // exchange preferences & purpose
        if (binding.item is ItemExchange) {
            binding.itemExchangePreferencesSection.visibility = View.VISIBLE
            var value = ""
            (itemViewModel.item as ItemExchange).exchangePreferences.forEach {
                val name = it.displayName
                value += "$name, "
            }
            value = value.substring(0, value.length - 2)
            binding.itemExchangePreferences.text = value
            binding.itemPurpose.text = " ${resources.getString(R.string.for_exchange)}"
            binding.btnAction.text = resources.getString(R.string.make_exchange)
        } else {
            binding.itemExchangePreferencesSection.visibility = View.GONE
            binding.itemPurpose.text = " ${resources.getString(R.string.for_donate)}"
            binding.btnAction.text = resources.getString(R.string.get_donation)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "ItemFragment is on onDestroyView")
        _binding = null
    }

}