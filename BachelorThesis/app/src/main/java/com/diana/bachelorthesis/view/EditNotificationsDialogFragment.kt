package com.diana.bachelorthesis.view

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.FragmentEditNotificationsDialogBinding
import com.diana.bachelorthesis.utils.ProfileOptionsListener

class EditNotificationsDialogFragment : DialogFragment() {
    private val TAG: String = EditNotificationsDialogFragment::class.java.name

    private var _binding: FragmentEditNotificationsDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var fragmentParent: ProfileFragment
    private lateinit var toolbar: Toolbar
    private var categorySelected: Int = 1 // default = option regarding the preferred items

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "FilterDialogFragment is onCreate")
        setStyle(STYLE_NORMAL,  R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "EditNotificationsDialogFragment is onCreateView")
        _binding = FragmentEditNotificationsDialogBinding.inflate(inflater, container, false)
        val root: View = binding.root

        toolbar = root.findViewById(R.id.toolbar_dialog_notifications)
        customizeToolbar()

        fragmentParent = parentFragment as ProfileFragment
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        restoreValues()

        return root
    }

    private fun customizeToolbar() {
        toolbar.title = getString(R.string.edit_notifications)
        toolbar.setTitleTextColor(resources.getColor(R.color.purple_dark))
        toolbar.setNavigationOnClickListener {
            dialog!!.dismiss()
        }
        toolbar.inflateMenu(R.menu.dialog_fragment_menu)
        toolbar.setOnMenuItemClickListener {
            val listener: ProfileOptionsListener = fragmentParent
            val selectedRadioButtonId = binding.notificationsRadioButtons.checkedRadioButtonId
            categorySelected =
                binding.notificationsRadioButtons.indexOfChild(binding.notificationsRadioButtons.findViewById(selectedRadioButtonId))
            listener.saveNotificationOption(categorySelected)
            dialog!!.dismiss()
            true
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "EditNotificationsDialogFragment is onActivityCreated")
    }

    private fun restoreValues() {
        val bundle = this.arguments
        categorySelected = bundle?.getInt("notificationsOption") ?: 1
        (binding.notificationsRadioButtons.getChildAt(categorySelected) as RadioButton).isChecked = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(TAG, "EditNotificationsDialogFragment is onDestroyView")
    }
}