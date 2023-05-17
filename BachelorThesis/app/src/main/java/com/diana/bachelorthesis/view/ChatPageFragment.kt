package com.diana.bachelorthesis.view

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.adapters.ChatsRecyclerViewAdapter
import com.diana.bachelorthesis.adapters.MessagesRecyclerViewAdapter
import com.diana.bachelorthesis.databinding.FragmentChatBinding
import com.diana.bachelorthesis.databinding.FragmentChatPageBinding
import com.diana.bachelorthesis.databinding.FragmentHomeBinding
import com.diana.bachelorthesis.model.Chat
import com.diana.bachelorthesis.utils.BasicFragment
import com.diana.bachelorthesis.viewmodel.ChatPageViewModel
import com.diana.bachelorthesis.viewmodel.ChatViewModel
import com.diana.bachelorthesis.viewmodel.ItemsViewModel
import com.diana.bachelorthesis.viewmodel.UserViewModel

class ChatPageFragment : Fragment(), BasicFragment {
    private val TAG: String = ChatPageFragment::class.java.name

    private var _binding: FragmentChatPageBinding? = null
    lateinit var chatPageViewModel: ChatPageViewModel
    private val binding get() = _binding!!

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "ChatPageFragment is onViewCreated")
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "ChatPageFragment is onActivityCreated")

        chatPageViewModel = ViewModelProvider(requireActivity())[ChatPageViewModel::class.java]

        chatPageViewModel.currentUser = (requireActivity() as MainActivity).getCurrentUser()!!
        chatPageViewModel.setCurrentChat(ChatPageFragmentArgs.fromBundle(requireArguments()).chat)

        setSubPageAppbar(requireActivity(), chatPageViewModel.currentChat.value!!.otherUser!!.name)
        chatPageViewModel.listenToChatChanges()

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        initListeners()
    }

    private fun updateRecyclerView(chat: Chat) {
        binding.messagesAdapter =
            MessagesRecyclerViewAdapter(chatPageViewModel.currentUser, chat.messagesSorted, requireContext()) { chat ->
                    Toast.makeText(context, "Clicked on message!", Toast.LENGTH_SHORT).show()
            }
    }

    override fun initListeners() {
        chatPageViewModel.currentChat.observe(viewLifecycleOwner) {
            updateRecyclerView(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "ChatPageFragment is onDestroyView")
        chatPageViewModel.detachCurrentChatListeners()
        _binding = null
    }

}