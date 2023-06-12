package com.diana.bachelorthesis.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.adapters.MessagesRecyclerViewAdapter
import com.diana.bachelorthesis.databinding.FragmentChatPageBinding
import com.diana.bachelorthesis.model.*
import com.diana.bachelorthesis.utils.BasicFragment
import com.diana.bachelorthesis.viewmodel.ChatPageViewModel

class ChatPageFragment : Fragment(), BasicFragment {
    private val TAG: String = ChatPageFragment::class.java.name

    private var _binding: FragmentChatPageBinding? = null
    private lateinit var chatPageViewModel: ChatPageViewModel
    private lateinit var adapter: MessagesRecyclerViewAdapter
    private val binding get() = _binding!!
    private var isAtBottom = true

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private val PICK_IMAGE_CODE = 10

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "ChatPageFragment is onCreateView")
        _binding = FragmentChatPageBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    private fun initAdapterAndLayoutManager() {
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.stackFromEnd = true

        adapter = MessagesRecyclerViewAdapter(chatPageViewModel.currentUser, chatPageViewModel.currentChat.value!!.messages, requireContext(), ::onMessageClicked)
        binding.recyclerView.adapter = adapter

        val observer: RecyclerView.AdapterDataObserver = object :
            RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (isAtBottom) {
                    binding.recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
                }
            }
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                isAtBottom = !recyclerView.canScrollVertically(1)
            }
        })

        adapter.registerAdapterDataObserver(observer)
        binding.recyclerView.layoutManager = linearLayoutManager
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "ChatPageFragment is onViewCreated")
        chatPageViewModel = ViewModelProvider(requireActivity())[ChatPageViewModel::class.java]

        chatPageViewModel.currentUser = (requireActivity() as MainActivity).getCurrentUser()!!
        chatPageViewModel.setCurrentChat(ChatPageFragmentArgs.fromBundle(requireArguments()).chat)
        setSubPageAppbar(requireActivity(), chatPageViewModel.currentChat.value!!.otherUser!!.name)

        chatPageViewModel.proposal = ChatPageFragmentArgs.fromBundle(requireArguments()).proposal
        initAdapterAndLayoutManager()

        chatPageViewModel.listenToChatChanges()
        initListeners()

        chatPageViewModel.sendProposalIfExisting()
    }

    private fun updateRecyclerView(chat: Chat) {
        adapter = MessagesRecyclerViewAdapter(chatPageViewModel.currentUser, chat.messages, requireContext(), ::onMessageClicked)
        binding.messagesAdapter = adapter
    }

    private fun onMessageClicked(item1: Item, item2: Item?, proposal: Proposal) {
        val newProposal = proposal.copy()

        val action = ChatPageFragmentDirections.actionNavChatPageFragmentToProposalPageFragment(item1, item2, newProposal)
        requireView().findNavController().navigate(action)
    }

    override fun initListeners() {
        chatPageViewModel.currentChat.observe(viewLifecycleOwner) {
            updateRecyclerView(it)
        }

        binding.btnSendText.setOnClickListener {
            if (chatPageViewModel.currentChat.value != null) {
                if (binding.textBox.text.toString().isNotEmpty()) {
                    chatPageViewModel.addMessageToChat(textMessage = binding.textBox.text.toString())
                    binding.textBox.setText("")
                } else {
                    displayErrorToast()
                }
            }
        }

        binding.btnSendPhoto.setOnClickListener {
            if (chatPageViewModel.currentChat.value != null) {
                choosePhoto()
            } else {
                displayErrorToast()
            }
        }
    }

    private fun displayErrorToast() {
        Toast.makeText(requireActivity(), getString(R.string.something_failed), Toast.LENGTH_SHORT).show()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            activityResultLauncher.launch(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        Log.d(TAG, "onActivityResult from ChatPageFragment")
        when (requestCode) {
            PICK_IMAGE_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val imageUri: Uri? = intent?.data
                    imageUri?.let {
                        chatPageViewModel.addPhotoToChat(it)
                    }
                }
            }
        }
    }

    private fun choosePhoto() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // ask for permission if it is not given already
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            ) // 1 is standard

        } else {
            val getIntent = Intent(Intent.ACTION_GET_CONTENT)
            getIntent.type = "image/*"
            val pickIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            val chooserIntent =
                Intent.createChooser(getIntent, resources.getString(R.string.select_photo))
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))

            startActivityForResult(chooserIntent, PICK_IMAGE_CODE)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "ChatPageFragment is onDestroyView")
        chatPageViewModel.detachCurrentChatListeners()
        _binding = null
    }

}