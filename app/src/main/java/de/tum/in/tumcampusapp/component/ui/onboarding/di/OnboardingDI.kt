package de.tum.`in`.tumcampusapp.component.ui.onboarding.di

import dagger.BindsInstance
import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.ui.onboarding.CheckTokenFragment
import de.tum.`in`.tumcampusapp.component.ui.onboarding.OnboardingActivity
import de.tum.`in`.tumcampusapp.component.ui.onboarding.OnboardingExtrasFragment
import de.tum.`in`.tumcampusapp.component.ui.onboarding.OnboardingStartFragment
import javax.inject.Scope

@Scope
annotation class OnboardingScope

@OnboardingScope
@Subcomponent
interface OnboardingComponent {

    fun inject(onboardingActivity: OnboardingActivity)
    fun inject(startFragment: OnboardingStartFragment)
    fun inject(checkTokenFragment: CheckTokenFragment)
    fun inject(extrasFragment: OnboardingExtrasFragment)

    @Subcomponent.Factory
    interface Factory {
        fun create(
            @BindsInstance activity: OnboardingActivity
        ): OnboardingComponent
    }
}

interface OnboardingComponentProvider {
    fun onboardingComponent(): OnboardingComponent
}
