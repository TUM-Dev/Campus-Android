package de.tum.`in`.tumcampusapp.component.tumui.feedback.di

import android.content.Context
import dagger.Module
import dagger.Provides
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.tumui.feedback.FeedbackContract
import de.tum.`in`.tumcampusapp.component.tumui.feedback.FeedbackPresenter

@Module
class FeedbackModule {

    @Provides
    fun provideFeedbackPresenter(
            context: Context,
            @LrzId lrzId: String,
            tumCabeClient: TUMCabeClient
    ): FeedbackContract.Presenter {
        return FeedbackPresenter(context, lrzId, tumCabeClient)
    }

}
