package com.tri10.catboot.ui.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tri10.catboot.ui.main.Composition
import com.tri10.catboot.ui.main.MainViewModel
import java.lang.RuntimeException

class CustomViewModelFactory(private val composition: Composition): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass == MainViewModel::class.java) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(composition.logger, composition.command) as T
        }
        throw RuntimeException("Unexpected view model | modelClass=$modelClass") // TODO only run this in debug builds or switch to a more reliable method of creating viewmodels.
        // return super.create(modelClass)
    }
}