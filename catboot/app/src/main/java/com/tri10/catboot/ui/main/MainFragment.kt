package com.tri10.catboot.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.tri10.catboot.CatbootApplication
import com.tri10.catboot.R
import kotlin.math.log

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this, (requireContext().applicationContext as CatbootApplication).viewModelFactory)[MainViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val logLineTextView = view.findViewById<TextView>(R.id.logLine)
        val errorLineTextView = view.findViewById<TextView>(R.id.errorLine)

        viewModel.start()
        viewModel.newLogLine.observe(viewLifecycleOwner) {
            logLineTextView.text = it
        }
        viewModel.newErrorLine.observe(viewLifecycleOwner) {
            errorLineTextView.text = it
        }
    }
}