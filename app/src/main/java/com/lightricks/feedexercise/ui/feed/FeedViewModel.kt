package com.lightricks.feedexercise.ui.feed

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import androidx.room.Room
import com.lightricks.feedexercise.data.FeedItem
import com.lightricks.feedexercise.database.FeedDatabase
import com.lightricks.feedexercise.network.FeedApiServiceImpl
import com.lightricks.feedexercise.network.GetFeedResponse
import com.lightricks.feedexercise.util.Event
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import java.lang.IllegalArgumentException
import java.lang.NullPointerException

/**
 * This view model manages the data for [FeedFragment].
 */
open class FeedViewModel(private val context: Context) : ViewModel() {
    private val stateInternal: MutableLiveData<State> = MutableLiveData<State>(DEFAULT_STATE)
    private val networkErrorEvent = MutableLiveData<Event<String>>()
    private val feedItems = MutableLiveData<List<FeedItem>>()
    private val isLoading = MutableLiveData<Boolean>()
    private val isEmpty = MutableLiveData<Boolean>()
    private var disposable: Disposable? = null
    private var urlString : String = "https://assets.swishvideoapp.com/Android/demo/catalog/thumbnails/"
//    private lateinit var database: FeedDatabase

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
        disposable = FeedApiServiceImpl.service.getFeed()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ feedResponse ->
                handleResponse(feedResponse)
            }, { error ->
                handleNetworkError(error)
            })
    }


    private fun handleNetworkError(error: Throwable) {
        networkErrorEvent.postValue(Event(error?.localizedMessage ?: "NullPointerException"))
        isLoading.postValue(false)
    }

    private fun handleResponse(feedResponse: GetFeedResponse) {
        feedItems.postValue(feedResponse.templatesMetadata.map {
            FeedItem(
                it.id,
                urlString + it.templateThumbnailURI,
                it.isPremium
            )
        })
        isLoading.postValue(false)

        if(getFeedItems().value?.isEmpty() == true){
            isEmpty.postValue(true)
        }

//        database.feedItemDao().insertList(feedItems.value!!)
    }

    private fun updateState(transform: State.() -> State) {
        stateInternal.value = transform(getState())
    }

    private fun getState(): State {
        return stateInternal.value!!
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
    private val context: Context = context
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(FeedViewModel::class.java)) {
            throw IllegalArgumentException("factory used with a wrong class")
        }
        @Suppress("UNCHECKED_CAST")
        return FeedViewModel(context) as T
    }
}
