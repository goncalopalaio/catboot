package com.tri10.catboot.implementation.composition

import com.tri10.catboot.definition.LogSource
import com.tri10.catboot.definition.Logger
import com.tri10.catboot.implementation.command.LogcatSource
import com.tri10.catboot.implementation.command.LoggerCommand
import com.tri10.catboot.implementation.logging.AndroidLogger
import com.tri10.catboot.ui.main.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<Logger> { AndroidLogger() }
    single<LogSource> {LogcatSource(get())}
    single { LoggerCommand(get(), get()) }
    viewModel { MainViewModel(get(), get())}
}