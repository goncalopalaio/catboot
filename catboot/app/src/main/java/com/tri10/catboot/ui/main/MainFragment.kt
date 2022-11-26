package com.tri10.catboot.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.tri10.catboot.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel: MainViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val logLineTextView = view.findViewById<TextView>(R.id.logLine)
        val lineCountTextView = view.findViewById<TextView>(R.id.lineCount)
        val isReadingTextView = view.findViewById<TextView>(R.id.isReading)
        val errorLineTextView = view.findViewById<TextView>(R.id.errorLine)

        var count = 0
        viewModel.newLogLine.observe(viewLifecycleOwner) {
            logLineTextView.text = it
            count++
            lineCountTextView.text = "$count"

        }
        viewModel.newErrorLine.observe(viewLifecycleOwner) {
            errorLineTextView.text = it
        }
        viewModel.isReading.observe(viewLifecycleOwner) {
            isReadingTextView.text = it
        }

        viewModel.start()
    }
}