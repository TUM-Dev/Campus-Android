package de.tum.`in`.tumcampusapp.component.tumui.feedback.di

import dagger.BindsInstance
import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.tumui.feedback.FeedbackActivity

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
