package com.bqliang.leavesheet.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bqliang.leavesheet.databinding.FragmentEditPersonalInfoBinding


class EditPersonalInfoFragment : Fragment() {

    private lateinit var binding: FragmentEditPersonalInfoBinding
    private val viewModel: EditViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditPersonalInfoBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }
}