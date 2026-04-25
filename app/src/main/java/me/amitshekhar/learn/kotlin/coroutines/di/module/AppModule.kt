package me.amitshekhar.learn.kotlin.coroutines.di.module

import me.amitshekhar.learn.kotlin.coroutines.data.api.*
import me.amitshekhar.learn.kotlin.coroutines.data.local.*
import me.amitshekhar.learn.kotlin.coroutines.data.repository.UserRepositoryImpl
import me.amitshekhar.learn.kotlin.coroutines.domain.repository.UserRepository
import me.amitshekhar.learn.kotlin.coroutines.domain.usecase.*
import me.amitshekhar.learn.kotlin.coroutines.ui.errorhandling.supervisor.IgnoreErrorAndContinueViewModel
import me.amitshekhar.learn.kotlin.coroutines.ui.errorhandling.trycatch.TryCatchViewModel
import me.amitshekhar.learn.kotlin.coroutines.ui.retrofit.parallel.ParallelNetworkCallsViewModel
import me.amitshekhar.learn.kotlin.coroutines.ui.retrofit.series.SeriesNetworkCallsViewModel
import me.amitshekhar.learn.kotlin.coroutines.ui.retrofit.single.SingleNetworkCallViewModel
import me.amitshekhar.learn.kotlin.coroutines.ui.room.RoomDBViewModel
import me.amitshekhar.learn.kotlin.coroutines.ui.task.onetask.LongRunningTaskViewModel
import me.amitshekhar.learn.kotlin.coroutines.ui.task.twotasks.TwoLongRunningTasksViewModel
import me.amitshekhar.learn.kotlin.coroutines.ui.timeout.TimeoutViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { RetrofitBuilder.apiService }
    single<ApiHelper> { ApiHelperImpl(get()) }
    single { DatabaseBuilder.getInstance(androidContext()) }
    single<DatabaseHelper> { DatabaseHelperImpl(get()) }

    // DataSources
    single<UserRemoteDataSource> { UserRemoteDataSourceImpl(get()) }
    single<UserLocalDataSource> { UserLocalDataSourceImpl(get()) }

    // Repository
    single<UserRepository> { UserRepositoryImpl(get(), get()) }

    // UseCases
    factory { GetUsersUseCase(get()) }
    factory { GetMoreUsersUseCase(get()) }
    factory { GetUsersFromDbUseCase(get()) }
    factory { GetUsersWithErrorUseCase(get()) }
    factory { GetUsersSeriesUseCase(get()) }
    factory { GetUsersParallelUseCase(get()) }
}

val viewModelModule = module {
    viewModel { SingleNetworkCallViewModel(get()) }
    viewModel { SeriesNetworkCallsViewModel(get()) }
    viewModel { ParallelNetworkCallsViewModel(get()) }
    viewModel { RoomDBViewModel(get()) }
    viewModel { TimeoutViewModel(get()) }
    viewModel { TryCatchViewModel(get()) }
    viewModel { IgnoreErrorAndContinueViewModel(get(), get()) }
    viewModel { LongRunningTaskViewModel() }
    viewModel { TwoLongRunningTasksViewModel() }
}
