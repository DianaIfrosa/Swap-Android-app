package com.diana.bachelorthesis.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.FragmentEditProfileDialogBinding
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.utils.ProfileOptionsListener
import com.diana.bachelorthesis.viewmodel.ProfileViewModel
import com.diana.bachelorthesis.viewmodel.UserViewModel
import com.squareup.picasso.Picasso
import java.lang.Exception
import java.util.regex.Pattern

class EditProfileDialogFragment : DialogFragment() {

    private val TAG: String = EditProfileDialogFragment::class.java.name

    private var _binding: FragmentEditProfileDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var fragmentParent: ProfileFragment
    private lateinit var toolbar: Toolbar
    private lateinit var profileViewModel: ProfileViewModel
    lateinit var userViewModel: UserViewModel

    private val PICK_IMAGE_CODE = 10
    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "EditProfileDialogFragment is onCreate")
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "EditProfileDialogFragment is onCreateView")
        _binding = FragmentEditProfileDialogBinding.inflate(inflater, container, false)
        val root: View = binding.root

        toolbar = root.findViewById(R.id.toolbar_dialog_profile)
        customizeToolbar()

        fragmentParent = parentFragment as ProfileFragment
        getViewModels()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        restoreValues()
        updateUI()
        initListeners()

        return root
    }

    private fun getViewModels() {
        profileViewModel = ViewModelProvider(fragmentParent)[ProfileViewModel::class.java]
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
    }

    private fun updateUI() {
        if (userViewModel.userLoggedInWithGoogle()) {
            binding.denyChangeText.visibility = View.VISIBLE
            binding.layoutsPasswords.visibility = View.GONE
        } else {
            binding.oldPassInputLayout.apply {
                isHelperTextEnabled = false
                helperText = ""
            }

            binding.newPassInputLayout.apply {
                isHelperTextEnabled = false
                helperText = ""
            }

            binding.denyChangeText.visibility = View.GONE
            binding.layoutsPasswords.visibility = View.VISIBLE
        }
    }

    private fun restoreValues() {
        if (profileViewModel.profilePhoto != null)
            Picasso.get().load(profileViewModel.profilePhoto).into(binding.profilePhoto)
    }

    private fun initListeners() {
        binding.btnSelectPhoto.setOnClickListener {
            choosePhoto()
        }
    }

    private fun customizeToolbar() {
        toolbar.title = getString(R.string.edit_profile)
        toolbar.setTitleTextColor(resources.getColor(R.color.purple_dark))
        toolbar.setNavigationOnClickListener {
            dialog!!.dismiss()
        }
        toolbar.inflateMenu(R.menu.dialog_fragment_menu)
        toolbar.setOnMenuItemClickListener {
            val listener: ProfileOptionsListener = fragmentParent
            val validPasswords = validatePasswords()
            if (validPasswords) {
                if (!binding.newPassEdittext.text.isNullOrEmpty() && !binding.oldPassEdittext.text.isNullOrEmpty()) {
                    profileViewModel.verifyOldPass(
                        binding.oldPassEdittext.text.toString(),
                        object : NoParamCallback {
                            override fun onComplete() {
                                listener.saveProfileChanges(
                                    profileViewModel.newprofilePhoto,
                                    binding.newPassEdittext.text.toString()
                                )
                                dialog!!.dismiss()
                            }

                            override fun onError(e: Exception?) {
                                binding.oldPassInputLayout.apply {
                                    isHelperTextEnabled = true
                                    helperText = getString(R.string.old_pass_invalid)
                                }
                            }
                        })
                } else {
                    // the user didn't want to change the password
                    listener.saveProfileChanges(
                        profileViewModel.newprofilePhoto,
                        null
                    )
                    dialog!!.dismiss()
                }
            }
            true
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        Log.d(TAG, "onActivityResult from EditProfileDialogFragment")
        when (requestCode) {
            PICK_IMAGE_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val imageUri: Uri? = intent?.data
                    imageUri?.let {
                        Picasso.get().load(imageUri).into(binding.profilePhoto)
                        profileViewModel.newprofilePhoto = imageUri
                    }
                }
            }
        }
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

    private fun validatePasswords(): Boolean {
        var isValid = true
        if (binding.newPassEdittext.text.toString()
                .isEmpty() && binding.oldPassEdittext.text.toString().isEmpty()
        ) {
            // the user didn't want to change the password
            return true
        } else {
            // new password
            if (binding.newPassEdittext.text.toString().isEmpty()) {
                binding.newPassInputLayout.apply {
                    isHelperTextEnabled = true
                    helperText = getString(R.string.pass_required)
                }
                isValid = false
            } else if (validPass(binding.newPassEdittext.text.toString())) {
                binding.newPassInputLayout.apply {
                    isHelperTextEnabled = false
                    helperText = ""
                }
            } else {
                // invalid password
                binding.newPassInputLayout.apply {
                    isHelperTextEnabled = true
                    helperText = getString(R.string.pass_invalid)
                }
                isValid = false
            }

            if (binding.oldPassEdittext.text.toString().isEmpty()) {
                binding.oldPassInputLayout.apply {
                    isHelperTextEnabled = true
                    helperText = getString(R.string.pass_required)
                }
                isValid = false

            } else if (validPass(binding.oldPassEdittext.text.toString())) {
                binding.oldPassInputLayout.apply {
                    isHelperTextEnabled = false
                    helperText = ""
                }
            } else {
                // invalid old password
                binding.oldPassInputLayout.apply {
                    isHelperTextEnabled = true
                    helperText = getString(R.string.pass_invalid)
                }
                isValid = false
            }
        }
        return isValid
    }

    private fun validPass(pass: String): Boolean {
        val regex = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$"
        val pattern = Pattern.compile(regex)
        return pattern.matcher(pass).matches()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "EditProfileDialogFragment is onDestroyView")
        _binding = null
    }
}