package de.tum.`in`.tumcampusapp.component.tumui.feedback.di

import dagger.Binds
import dagger.Module
import de.tum.`in`.tumcampusapp.component.tumui.feedback.FeedbackContract
import de.tum.`in`.tumcampusapp.component.tumui.feedback.FeedbackPresenter

@Module
interface FeedbackModule {

    @Binds
    fun bindsFeedbackPresenter(impl: FeedbackPresenter): FeedbackContract.Presenter

}
