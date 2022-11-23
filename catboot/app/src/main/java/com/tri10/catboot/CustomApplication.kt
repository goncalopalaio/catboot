package com.tri10.catboot

import android.app.Application
import com.tri10.catboot.ui.main.logging.AndroidLogger
import com.tri10.catboot.definition.Logger
import com.tri10.catboot.ui.main.composition.CatbootComposition
import com.tri10.catboot.ui.main.viewmodel.CustomViewModelFactory

class CatbootApplication: Application() {
    val viewModelFactory = CustomViewModelFactory(CatbootComposition) // IMPROVEMENT, do proper dependency injection, this is ugly.
}