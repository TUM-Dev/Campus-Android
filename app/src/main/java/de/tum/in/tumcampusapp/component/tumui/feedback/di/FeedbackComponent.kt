package de.tum.`in`.tumcampusapp.component.tumui.feedback.di

import dagger.Binds
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.tumui.feedback.FeedbackActivity
import de.tum.`in`.tumcampusapp.component.tumui.feedback.FeedbackContract
import de.tum.`in`.tumcampusapp.component.tumui.feedback.FeedbackPresenter

@Subcomponent(modules = [FeedbackModule::class])
interface FeedbackComponent {

    fun inject(feedbackActivity: FeedbackActivity)

    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        fun lrzId(@LrzId lrzId: String): Builder

        fun build(): FeedbackComponent
    }
}

@Module
interface FeedbackModule {

    @Binds
    fun bindsFeedbackPresenter(impl: FeedbackPresenter): FeedbackContract.Presenter
}
