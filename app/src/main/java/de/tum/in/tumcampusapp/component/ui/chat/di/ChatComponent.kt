package de.tum.`in`.tumcampusapp.component.ui.chat.di

import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.ui.chat.activity.ChatActivity
import de.tum.`in`.tumcampusapp.component.ui.chat.activity.ChatRoomsActivity
import de.tum.`in`.tumcampusapp.component.ui.onboarding.WizNavExtrasActivity
import de.tum.`in`.tumcampusapp.service.SendMessageService

@Subcomponent(modules = [ChatModule::class])
interface ChatComponent {

    fun inject(chatActivity: ChatActivity)

    fun inject(chatRoomsActivity: ChatRoomsActivity)

    fun inject(wizNavExtrasActivity: WizNavExtrasActivity)

    fun inject(sendMessageService: SendMessageService)

    @Subcomponent.Builder
    interface Builder {

        fun chatModule(chatModule: ChatModule): ChatComponent.Builder

        fun build(): ChatComponent

    }

}
