package com.lightricks.feedexercise.ui.feed

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.LiveDataReactiveStreams
import com.lightricks.feedexercise.data.FeedItem
import com.lightricks.feedexercise.data.FeedRepository
import com.lightricks.feedexercise.database.FeedDatabase
import com.lightricks.feedexercise.database.FeedItemEntity
import com.lightricks.feedexercise.database.*
import com.lightricks.feedexercise.network.FeedApiServiceImpl
import com.lightricks.feedexercise.network.GetFeedResponse
import com.lightricks.feedexercise.util.Event
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.lang.IllegalArgumentException

/**
 * This view model manages the data for [FeedFragment].
 */
open class FeedViewModel(private val feedRepository: FeedRepository) : ViewModel() {
    private val networkErrorEvent = MutableLiveData<Event<String>>()
    private val feedItems = MediatorLiveData<List<FeedItem>>()
    private val isLoading = MutableLiveData<Boolean>()
    private val isEmpty = MutableLiveData<Boolean>()
    private var disposable: Disposable? = null


    fun getIsLoading(): LiveData<Boolean> {
        return isLoading
    }

    fun getIsEmpty(): LiveData<Boolean> {
        return isEmpty
    }

    fun getFeedItems(): LiveData<List<FeedItem>> {
        return feedItems
    }

    fun getNetworkErrorEvent(): LiveData<Event<String>> = networkErrorEvent

    init {
        isEmpty.postValue(true)
        isLoading.postValue(false)
        feedItems.addSource(feedRepository.feedItems) {
            feedItems.postValue(it)
            isEmpty.postValue(it.isEmpty())
        }
    }

    fun refresh() {
        isLoading.postValue(true)
        disposable = feedRepository.refresh().subscribe(
            { isLoading.postValue(false) },
            { error -> handleNetworkError(error) }
        )
    }


    private fun handleNetworkError(error: Throwable) {
        networkErrorEvent.postValue(Event(error?.localizedMessage ?: "Error Occurred"))
        isLoading.postValue(false)
    }

    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
    }

    data class State(
        val feedItems: List<FeedItem>?,
        val isLoading: Boolean
    )

    companion object {
        private val DEFAULT_STATE = State(
            feedItems = null,
            isLoading = false
        )
    }
}


/**
 * This class creates instances of [FeedViewModel].
 * It's not necessary to use this factory at this stage. But if we will need to inject
 * dependencies into [FeedViewModel] in the future, then this is the place to do it.
 */
class FeedViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val feedRepository: FeedRepository =
        FeedRepository(FeedApiServiceImpl.service, getFeedDB(context))

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(FeedViewModel::class.java)) {
            throw IllegalArgumentException("factory used with a wrong class")
        }
        @Suppress("UNCHECKED_CAST")
        return FeedViewModel(feedRepository) as T
    }
}
