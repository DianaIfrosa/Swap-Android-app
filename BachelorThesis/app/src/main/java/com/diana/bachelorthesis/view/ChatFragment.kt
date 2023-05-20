package com.diana.bachelorthesis.view

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.diana.bachelorthesis.databinding.FragmentChatBinding
import com.diana.bachelorthesis.utils.BasicFragment
import com.diana.bachelorthesis.viewmodel.ChatViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.diana.bachelorthesis.adapters.ChatsRecyclerViewAdapter
import com.diana.bachelorthesis.model.Chat

class ChatFragment : Fragment(), BasicFragment {

    private val TAG: String = ChatFragment::class.java.name
    private var _binding: FragmentChatBinding? = null

    private val binding get() = _binding!!
    private lateinit var chatViewModel: ChatViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "ChatFragment is onCreateView")

        _binding = FragmentChatBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "ChatFragment is onViewCreated")
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        updateRecyclerView(arrayListOf(), true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "ChatFragment is onActivityCreated")
        setMainPageAppbar(requireActivity(), requireView().findNavController().currentDestination!!.label.toString())

        chatViewModel = ViewModelProvider(requireActivity())[ChatViewModel::class.java]
        initListeners()
    }

    private fun updateRecyclerView(chats: ArrayList<Chat>, progressBarAppears: Boolean = false) {
        if (binding != null) {

            if (progressBarAppears) {
                binding.recyclerView.visibility = View.GONE
                binding.textNoChats.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
            } else if (chats.isNotEmpty()) {
                binding.progressBar.visibility = View.GONE
                binding.textNoChats.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                binding.chatsAdapter =
                    ChatsRecyclerViewAdapter(
                        (requireActivity() as MainActivity).getCurrentUser()!!,
                        chats,
                        requireContext()
                    ) { chat ->
                        val action =
                            ChatFragmentDirections.actionNavChatToChatPageFragment(chat, null)
                        requireView().findNavController().navigate(action)
                    }
            } else {
                binding.progressBar.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
                binding.textNoChats.visibility = View.VISIBLE
            }
        }
    }

    override fun initListeners() {
        chatViewModel.chats.observe(viewLifecycleOwner) {
            Log.d(TAG, "Observed chats modification!")
            updateRecyclerView(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "ChatFragment is onDestroyView")
        _binding = null
    }
}