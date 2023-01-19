package de.tum.`in`.tumcampusapp.component.tumui.feedback.di

import dagger.Binds
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.tumui.feedback.FeedbackActivity
import de.tum.`in`.tumcampusapp.component.tumui.feedback.FeedbackContract
import de.tum.`in`.tumcampusapp.component.tumui.feedback.FeedbackPresenter
import de.tum.`in`.tumcampusapp.utils.camera.CameraContract
import de.tum.`in`.tumcampusapp.utils.camera.CameraPresenter

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
    @Binds
    fun bindsCameraPresenter(impl: CameraPresenter): CameraContract.Presenter
}
