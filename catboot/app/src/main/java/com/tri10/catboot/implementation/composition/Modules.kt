package com.tri10.catboot.implementation.composition

import com.tri10.catboot.definition.Logger
import com.tri10.catboot.definition.LoggerCommand
import com.tri10.catboot.ui.main.Composition
import com.tri10.catboot.implementation.command.LogcatCommand
import com.tri10.catboot.implementation.logging.AndroidLogger
import com.tri10.catboot.ui.main.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<Logger> { AndroidLogger() }
    single<LoggerCommand> { LogcatCommand(get()) }
    viewModel { MainViewModel(get(), get())}
}