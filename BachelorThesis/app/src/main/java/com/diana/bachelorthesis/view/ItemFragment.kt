package com.diana.bachelorthesis.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.adapters.ItemsRecyclerViewAdapter
import com.diana.bachelorthesis.databinding.FragmentItemBinding
import com.diana.bachelorthesis.model.ItemExchange
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.utils.BasicFragment
import com.diana.bachelorthesis.utils.LocationHelper
import com.diana.bachelorthesis.utils.OneParamCallback
import com.diana.bachelorthesis.viewmodel.ItemPageViewModel
import com.diana.bachelorthesis.viewmodel.UserViewModel
import com.squareup.picasso.Picasso
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class ItemFragment : Fragment(), BasicFragment {

    private val TAG: String = ItemFragment::class.java.name
    private var _binding: FragmentItemBinding? = null
    private val binding get() = _binding!!
    lateinit var userViewModel: UserViewModel
    lateinit var itemViewModel: ItemPageViewModel

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

        itemViewModel.currentItem = ItemFragmentArgs.fromBundle(requireArguments()).ItemClicked
        updateUIElements()

        userViewModel.getUserData(itemViewModel.currentItem.owner, object : OneParamCallback<User> {
            override fun onComplete(value: User?) {
                if (value != null)
                    showItemOwnerDetails(value)
            }

            override fun onError(e: Exception?) {
                Log.w(
                    TAG,
                    "Item's owner with email ${itemViewModel.currentItem.owner} could not been retrieved!"
                )
            }
        })
        showItemDetails()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "ItemFragment is on onActivityCreated")
        initListeners()
        setSubPageAppbar(requireActivity(), itemViewModel.currentItem.name)
    }

    private fun getViewModels() {
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        itemViewModel = ViewModelProvider(this)[ItemPageViewModel::class.java]
    }

    override fun initListeners() {
        binding.btnFavorite.setOnClickListener { view ->
            val drawable = binding.btnFavorite.drawable
            if (drawable.constantState!! == resources.getDrawable(R.drawable.ic_heart_filled).constantState) {
                // remove from favorites
                binding.btnFavorite.setImageResource(R.drawable.ic_heart_unfilled)
                (requireActivity() as MainActivity).removeFavoriteItem(itemViewModel.currentItem)
            } else {
                // add to favorites
                binding.btnFavorite.setImageResource(R.drawable.ic_heart_filled)
                (requireActivity() as MainActivity).addFavoriteItem(itemViewModel.currentItem)
            }
        }
    }

    private fun updateUIElements() {
        if (userViewModel.verifyUserLoggedIn()) {
            binding.btnFavorite.visibility = View.VISIBLE
            if ((requireActivity() as MainActivity).itemIsFavorite(itemViewModel.currentItem)) {
                binding.btnFavorite.setImageDrawable(resources.getDrawable(R.drawable.ic_heart_filled))
            } else {
                binding.btnFavorite.setImageDrawable(resources.getDrawable(R.drawable.ic_heart_unfilled))
            }
        } else {
            binding.btnFavorite.visibility = View.INVISIBLE
        }
    }

    override fun onStart() {
        super.onStart()
    }

    private fun showItemOwnerDetails(owner: User) {
        binding.itemOwnerName.text = owner.name
        Picasso.get().load(owner.profilePhoto).into(binding.ownerPicture)
    }

    private fun showItemDetails() {
        binding.item = itemViewModel.currentItem

        // photos
        binding.photoCarousel.setImageList(ItemsRecyclerViewAdapter.getPhotos(itemViewModel.currentItem))

        // location
        binding.itemAddress.text = itemViewModel.currentItem.address

        // post date
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.itemPostDate.text = dateFormatter.format(
            itemViewModel.currentItem.postDate!!.toDate()
        ).toString()

        // manufacture year
        if (itemViewModel.currentItem.year != null) {
            binding.itemManufactureSection.visibility = View.VISIBLE
            binding.itemManufactureYear.text = itemViewModel.currentItem.year.toString()
        } else {
            binding.itemManufactureSection.visibility = View.GONE
        }

        // exchange preferences & purpose (item + background color)
        if (binding.item is ItemExchange) {
            binding.itemExchangePreferencesSection.visibility = View.VISIBLE
            var value = ""
            (itemViewModel.currentItem as ItemExchange).exchangePreferences.forEach {
                val name = it.displayName
                value += "$name, "
            }
            value = value.substring(0, value.length - 2)
            binding.itemExchangePreferences.text = value
            binding.root.setBackgroundColor(resources.getColor(R.color.purple_pale))
            binding.itemPurpose.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(R.drawable.ic_exchange), null, null, null)
            binding.btnAction.text = resources.getString(R.string.make_exchange)
        } else {
            binding.itemExchangePreferencesSection.visibility = View.GONE
            binding.root.setBackgroundColor(resources.getColor(R.color.yellow_pale))
            binding.itemPurpose.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(R.drawable.ic_donate), null, null, null)
            binding.btnAction.text = resources.getString(R.string.get_donation)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "ItemFragment is on onDestroyView")
        _binding = null
    }

}