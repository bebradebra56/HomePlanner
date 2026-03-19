package com.homeplane.plmeas.bfg.presentation.di

import com.homeplane.plmeas.bfg.data.repo.HomePlannerRepository
import com.homeplane.plmeas.bfg.data.shar.HomePlannerSharedPreference
import com.homeplane.plmeas.bfg.data.utils.HomePlannerPushToken
import com.homeplane.plmeas.bfg.data.utils.HomePlannerSystemService
import com.homeplane.plmeas.bfg.domain.usecases.HomePlannerGetAllUseCase
import com.homeplane.plmeas.bfg.presentation.pushhandler.HomePlannerPushHandler
import com.homeplane.plmeas.bfg.presentation.ui.load.HomePlannerLoadViewModel
import com.homeplane.plmeas.bfg.presentation.ui.view.HomePlannerViFun
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homePlannerModule = module {
    factory {
        HomePlannerPushHandler()
    }
    single {
        HomePlannerRepository()
    }
    single {
        HomePlannerSharedPreference(get())
    }
    factory {
        HomePlannerPushToken()
    }
    factory {
        HomePlannerSystemService(get())
    }
    factory {
        HomePlannerGetAllUseCase(
            get(), get(), get()
        )
    }
    factory {
        HomePlannerViFun(get())
    }
    viewModel {
        HomePlannerLoadViewModel(get(), get(), get())
    }
}