package com.diana.bachelorthesis.view

import android.app.AlertDialog
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
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.adapters.ItemsRecyclerViewAdapter
import com.diana.bachelorthesis.databinding.FragmentItemBinding
import com.diana.bachelorthesis.model.Chat
import com.diana.bachelorthesis.model.ItemExchange
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.utils.BasicFragment
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.utils.OneParamCallback
import com.diana.bachelorthesis.viewmodel.ChatViewModel
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
    lateinit var itemPageViewModel: ItemPageViewModel

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

        itemPageViewModel.currentItem = ItemFragmentArgs.fromBundle(requireArguments()).itemClicked
        updateUIElements()

        userViewModel.getUserData(
            itemPageViewModel.currentItem.owner,
            object : OneParamCallback<User> {
                override fun onComplete(value: User?) {
                    if (value != null)
                        showItemOwnerDetails(value)
                    itemPageViewModel.owner = value
                }

                override fun onError(e: Exception?) {
                    Log.w(
                        TAG,
                        "Item's owner with email ${itemPageViewModel.currentItem.owner} could not been retrieved!"
                    )
                }
            })
        showItemDetails()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "ItemFragment is on onActivityCreated")
        initListeners()
        setSubPageAppbar(requireActivity(), itemPageViewModel.currentItem.name)

        if ((requireActivity() as MainActivity).getCurrentUser()!!.email == itemPageViewModel.currentItem.owner) {
            // its own item
            binding.layoutButtons.visibility = View.GONE
            binding.btnFavorite.visibility = View.GONE
        } else {
            binding.layoutButtons.visibility = View.VISIBLE
        }
    }

    private fun getViewModels() {
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        itemPageViewModel = ViewModelProvider(this)[ItemPageViewModel::class.java]
    }

    private fun chatExists(otherUserEmail: String): Boolean {
        val currentUser = (requireActivity() as MainActivity).getCurrentUser()!!

        currentUser.chatIds.forEach {
            if (it["chatId"] == (currentUser.email + " " + otherUserEmail) ||
                it["chatId"] == (otherUserEmail + " " + currentUser.email)
            )
                return true
        }
        return false
    }

    override fun initListeners() {
        binding.btnFavorite.setOnClickListener {
            val drawable = binding.btnFavorite.drawable
            if (drawable.constantState!! == resources.getDrawable(R.drawable.ic_heart_filled).constantState) {
                // remove from favorites
                binding.btnFavorite.setImageResource(R.drawable.ic_heart_unfilled)
                (requireActivity() as MainActivity).removeFavoriteItem(itemPageViewModel.currentItem)
            } else {
                // add to favorites
                binding.btnFavorite.setImageResource(R.drawable.ic_heart_filled)
                (requireActivity() as MainActivity).addFavoriteItem(itemPageViewModel.currentItem)
            }
        }

        binding.deleteItem.setOnClickListener {
            val alertDialog = AlertDialog.Builder(requireActivity())
                .setTitle(getString(R.string.delete_item_title_dialog))
                .setMessage(getString(R.string.delete_item_message_dialog))
                .setPositiveButton(getString(R.string.yes)) { dialog, p1 ->
                    dialog.cancel()
                    binding.itemPageLayout.visibility = View.GONE
                    binding.progressBarDelete.visibility = View.VISIBLE
                    itemPageViewModel.deleteItem(object : NoParamCallback {
                        override fun onComplete() {
                            binding.progressBarDelete.visibility = View.GONE
                            binding.itemDeletedText.text =
                                getString(R.string.item_deleted_successfully)
                            binding.itemDeletedText.visibility = View.VISIBLE
                        }

                        override fun onError(e: Exception?) {
                            binding.progressBarDelete.visibility = View.GONE
                            binding.itemDeletedText.text = getString(R.string.item_deleted_failed)
                            binding.itemDeletedText.visibility = View.VISIBLE
                        }
                    })
                }
                .setNegativeButton(getString(R.string.no), null)
                .setIcon(R.drawable.ic_delete)

            alertDialog.show()
        }

        binding.ownerPicture.setOnClickListener {
            if (itemPageViewModel.owner != null) {
                val action =
                    ItemFragmentDirections.actionNavItemToNavOwnerProfile(itemPageViewModel.owner!!)
                requireView().findNavController().navigate(action)
            }
        }

        binding.photoCarousel.setItemClickListener(object : ItemClickListener {
            override fun onItemSelected(position: Int) {
                Log.d(TAG, "Clicked on photos!")
                val action =
                    ItemFragmentDirections.actionNavItemToNavPhotos(itemPageViewModel.currentItem)
                requireView().findNavController().navigate(action)
            }
        })

        binding.btnMessage.setOnClickListener {
            val chat: Chat?
            val currentUser = (requireActivity() as MainActivity).getCurrentUser()!!
            if (chatExists(itemPageViewModel.currentItem.owner)) {
                chat =
                    findChatInExistingList(itemPageViewModel.currentItem.owner, currentUser.email)
                if (chat == null) {
                    Toast.makeText(
                        context,
                        getString(R.string.something_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val action = ItemFragmentDirections.actionNavItemToNavChatPageFragment(chat)
                    requireView().findNavController().navigate(action)

                }
            } else {
                chat = Chat(
                    id = currentUser.email + " " + itemPageViewModel.owner!!.email,
                    otherUser = itemPageViewModel.owner
                )
                val action = ItemFragmentDirections.actionNavItemToNavChatPageFragment(chat)
                requireView().findNavController().navigate(action)
            }
        }

        binding.btnAction.setOnClickListener {
            if (itemPageViewModel.currentItem is ItemExchange) {
                val proposalItemChoiceDialogFragment = ProposalItemChoiceDialogFragment()
                proposalItemChoiceDialogFragment.isCancelable = true
                proposalItemChoiceDialogFragment.show(childFragmentManager, "ProposalItemChoiceDialogFragment")
            } else {
                //todo
            }
        }
    }

    private fun findChatInExistingList(email1: String, email2: String): Chat? {
        val chatViewModel: ChatViewModel =
            ViewModelProvider(requireActivity())[ChatViewModel::class.java]
        if (chatViewModel.chats.value == null)
            return null
        chatViewModel.chats.value!!.forEach {
            if (it.id == "$email1 $email2" || it.id == "$email2 $email1") {
                return it
            }
        }
        return null
    }

    private fun updateUIElements() {
        binding.progressBarDelete.visibility = View.GONE
        binding.itemDeletedText.visibility = View.GONE
        binding.itemPageLayout.visibility = View.VISIBLE

        if (userViewModel.verifyUserLoggedIn()) {
            binding.btnFavorite.visibility = View.VISIBLE
            if ((requireActivity() as MainActivity).itemIsFavorite(itemPageViewModel.currentItem)) {
                binding.btnFavorite.setImageDrawable(resources.getDrawable(R.drawable.ic_heart_filled))
            } else {
                binding.btnFavorite.setImageDrawable(resources.getDrawable(R.drawable.ic_heart_unfilled))
            }
        } else {
            binding.btnFavorite.visibility = View.INVISIBLE
        }

        if (itemPageViewModel.currentItem.owner == userViewModel.getCurrentUserEmail()) {
            binding.deleteItem.visibility = View.VISIBLE
        } else {
            binding.deleteItem.visibility = View.GONE
        }
    }

    private fun showItemOwnerDetails(owner: User) {
        if (binding != null) {
            binding.itemOwnerName.text = owner.name
            Picasso.get().load(owner.profilePhoto).into(binding.ownerPicture)
        }
    }

    private fun showItemDetails() {
        binding.item = itemPageViewModel.currentItem

        // photos
        binding.photoCarousel.setImageList(
            ItemsRecyclerViewAdapter.getPhotos(itemPageViewModel.currentItem),
            ScaleTypes.CENTER_CROP
        )

        // location
        binding.itemAddress.text = itemPageViewModel.currentItem.address

        // post date
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.itemPostDate.text = dateFormatter.format(
            itemPageViewModel.currentItem.postDate!!.toDate()
        ).toString()

        // manufacture year
        if (itemPageViewModel.currentItem.year != null) {
            binding.itemManufactureSection.visibility = View.VISIBLE
            binding.itemManufactureYear.text = itemPageViewModel.currentItem.year.toString()
        } else {
            binding.itemManufactureSection.visibility = View.GONE
        }

        // exchange preferences & purpose (item + background color)
        if (binding.item is ItemExchange) {
            binding.itemExchangePreferencesSection.visibility = View.VISIBLE
            var value = ""
            (itemPageViewModel.currentItem as ItemExchange).exchangePreferences.forEach {
                val name = it.displayName
                value += "$name, "
            }
            if (value.isEmpty()) {
                binding.itemExchangePreferencesSection.visibility = View.GONE
            } else {
                binding.itemExchangePreferencesSection.visibility = View.VISIBLE
                value = value.substring(0, value.length - 2)
                binding.itemExchangePreferences.text = value
            }
            binding.root.setBackgroundColor(resources.getColor(R.color.purple_pale))
            binding.itemPurpose.setCompoundDrawablesWithIntrinsicBounds(
                resources.getDrawable(R.drawable.ic_exchange),
                null,
                null,
                null
            )
            binding.btnAction.text = resources.getString(R.string.make_exchange)
        } else {
            binding.itemExchangePreferencesSection.visibility = View.GONE
            binding.root.setBackgroundColor(resources.getColor(R.color.yellow_pale))
            binding.itemPurpose.setCompoundDrawablesWithIntrinsicBounds(
                resources.getDrawable(R.drawable.ic_donate),
                null,
                null,
                null
            )
            binding.btnAction.text = resources.getString(R.string.get_donation)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "ItemFragment is on onDestroyView")
        _binding = null
    }
}