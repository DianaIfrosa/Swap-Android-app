package com.diana.bachelorthesis.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.diana.bachelorthesis.adapters.MessagesRecyclerViewAdapter
import com.diana.bachelorthesis.databinding.FragmentChatPageBinding
import com.diana.bachelorthesis.model.Chat
import com.diana.bachelorthesis.model.Message
import com.diana.bachelorthesis.utils.BasicFragment
import com.diana.bachelorthesis.viewmodel.ChatPageViewModel

class ChatPageFragment : Fragment(), BasicFragment {
    private val TAG: String = ChatPageFragment::class.java.name

    private var _binding: FragmentChatPageBinding? = null
    lateinit var chatPageViewModel: ChatPageViewModel
    private lateinit var adapter: MessagesRecyclerViewAdapter
    private val binding get() = _binding!!
    private var isAtBottom = true

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

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "ChatPageFragment is onActivityCreated")

        chatPageViewModel = ViewModelProvider(requireActivity())[ChatPageViewModel::class.java]

        chatPageViewModel.currentUser = (requireActivity() as MainActivity).getCurrentUser()!!
        chatPageViewModel.setCurrentChat(ChatPageFragmentArgs.fromBundle(requireArguments()).chat)
        initAdapterAndLayoutManager()

        setSubPageAppbar(requireActivity(), chatPageViewModel.currentChat.value!!.otherUser!!.name)
        chatPageViewModel.listenToChatChanges()
        initListeners()
    }

    private fun updateRecyclerView(chat: Chat) {
        adapter = MessagesRecyclerViewAdapter(chatPageViewModel.currentUser, chat.messages, requireContext(), ::onMessageClicked)
        binding.messagesAdapter = adapter
//        {
//            Toast.makeText(context, "Clicked on message!", Toast.LENGTH_SHORT).show()
//        }
//        binding.messagesAdapter = adapter
////        binding.recyclerView.smoothScrollToPosition(binding.messagesAdapter.itemCount - 1)
    }

    fun onMessageClicked(message: Message) {

    }

    override fun initListeners() {
        chatPageViewModel.currentChat.observe(viewLifecycleOwner) {
            updateRecyclerView(it)
        }

        binding.btnSendText.setOnClickListener {
            if (binding.textBox.text.toString().isNotEmpty()) {
                chatPageViewModel.addMessageToChat(binding.textBox.text.toString())
                binding.textBox.setText("")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "ChatPageFragment is onDestroyView")
        chatPageViewModel.detachCurrentChatListeners()
        _binding = null
    }

}