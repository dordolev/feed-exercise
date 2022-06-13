package com.lightricks.feedexercise.ui.feed

import android.annotation.SuppressLint
import androidx.lifecycle.*
import com.lightricks.feedexercise.data.FeedItem
import com.lightricks.feedexercise.network.FeedApiServiceImpl
import com.lightricks.feedexercise.network.GetFeedResponse
import com.lightricks.feedexercise.util.Event
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import java.lang.IllegalArgumentException

/**
 * This view model manages the data for [FeedFragment].
 */
open class FeedViewModel : ViewModel() {
    private val stateInternal: MutableLiveData<State> = MutableLiveData<State>(DEFAULT_STATE)
    private val networkErrorEvent = MutableLiveData<Event<String>>()
    private val feedItems = MutableLiveData<List<FeedItem>>()
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

//        refresh()
    }

//    @SuppressLint("CheckResult")
    fun refresh() {
    isEmpty.postValue(false)
    isLoading.postValue(true)
    disposable = FeedApiServiceImpl.service.getResponse()
        .subscribeOn(Schedulers.io()) //[1]
        .observeOn(AndroidSchedulers.mainThread()) //[2]
        .subscribe({ feedResponse ->
            handleResponse(feedResponse)
        },{ error ->
            handleNetworkError(error)
        })
    }

    private fun handleNetworkError(error: Throwable) {
        networkErrorEvent.postValue(Event(error.localizedMessage))
        isLoading.postValue(false)
    }

    private fun handleResponse(feedResponse: GetFeedResponse) {
        feedItems.postValue(feedResponse.templatesMetadata.map{
            FeedItem(
                it.id,
                "https://assets.swishvideoapp.com/Android/demo/catalog/thumbnails/"+ it.templateThumbnailURI,
                it.isPremium
            )
        })
        isLoading.postValue(false)
    }

    private fun updateState(transform: State.() -> State) {
        stateInternal.value = transform(getState())
    }

    private fun getState(): State {
        return stateInternal.value!!
    }

    fun onDestroyFragment() {
        disposable?.dispose()
    }

    data class State(
        val feedItems: List<FeedItem>?,
        val isLoading: Boolean)

    companion object {
        private val DEFAULT_STATE = State(
            feedItems = null,
            isLoading = false)
    }
}

/**
 * This class creates instances of [FeedViewModel].
 * It's not necessary to use this factory at this stage. But if we will need to inject
 * dependencies into [FeedViewModel] in the future, then this is the place to do it.
 */
class FeedViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(FeedViewModel::class.java)) {
            throw IllegalArgumentException("factory used with a wrong class")
        }
        @Suppress("UNCHECKED_CAST")
        return FeedViewModel() as T
    }
}
