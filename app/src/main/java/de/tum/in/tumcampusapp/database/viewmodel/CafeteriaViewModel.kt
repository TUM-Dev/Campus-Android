package de.tum.`in`.tumcampusapp.database.viewmodel

import android.arch.lifecycle.ViewModel
import android.location.Location
import de.tum.`in`.tumcampusapp.database.repository.LocalRepository
import de.tum.`in`.tumcampusapp.database.repository.RemoteRepository
import de.tum.`in`.tumcampusapp.models.cafeteria.Cafeteria
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CafeteriaViewModel(val localLocalRepository: LocalRepository, val remoteRepository: RemoteRepository,
                         val compositeDisposable: CompositeDisposable) : ViewModel() {


    fun getAllCafeteria(location: Location): Flowable<List<Cafeteria>> {
        return localLocalRepository.getAllCafeterias()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { transformCafeteria(it, location) }
    }

    private fun transformCafeteria(cafeterias: List<Cafeteria>, location: Location): List<Cafeteria> {
        return cafeterias.map {
            val results = FloatArray(1)
            Location.distanceBetween(it.latitude, it.longitude, location.latitude, location.longitude, results)
            it.distance = results[0]
            it
        }
    }

    fun getCafeteriasFromService() {
        compositeDisposable.add(Observable.just(1)
                .subscribeOn(Schedulers.computation())
                .flatMap { remoteRepository.getAllCafeterias() }.observeOn(Schedulers.io())
                .subscribe({ t -> t.forEach { localLocalRepository.addCafeteria(it) } })
        )
    }

}