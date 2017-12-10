package de.tum.`in`.tumcampusapp.viewmodel

import android.arch.lifecycle.ViewModel
import android.location.Location
import de.tum.`in`.tumcampusapp.models.cafeteria.Cafeteria
import de.tum.`in`.tumcampusapp.repository.CafeteriaLocalRepository
import de.tum.`in`.tumcampusapp.repository.CafeteriaRemoteRepository
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CafeteriaViewModel(private val localLocalRepository: CafeteriaLocalRepository, private val remoteRepository: CafeteriaRemoteRepository,
                         private val compositeDisposable: CompositeDisposable) : ViewModel() {


    fun getAllCafeteria(location: Location): Flowable<List<Cafeteria>> =
            localLocalRepository.getAllCafeterias()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map { transformCafeteria(it, location) }
                    .defaultIfEmpty(emptyList())


    private fun transformCafeteria(cafeterias: List<Cafeteria>, location: Location): List<Cafeteria> =
            cafeterias.map {
                val results = FloatArray(1)
                Location.distanceBetween(it.latitude, it.longitude, location.latitude, location.longitude, results)
                it.distance = results[0]
                it
            }

    fun getCafeteriasFromService() =
            compositeDisposable.add(Observable.just(1)
                    .subscribeOn(Schedulers.computation())
                    .flatMap { remoteRepository.getAllCafeterias() }.observeOn(Schedulers.io())
                    .subscribe({ t -> t.forEach { localLocalRepository.addCafeteria(it) } })
            )

}
