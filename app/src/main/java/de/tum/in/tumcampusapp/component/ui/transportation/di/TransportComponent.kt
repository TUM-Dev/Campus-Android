package de.tum.`in`.tumcampusapp.component.ui.transportation.di

import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportationActivity
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportationDetailsActivity
import de.tum.`in`.tumcampusapp.component.ui.transportation.widget.MVVWidget
import de.tum.`in`.tumcampusapp.component.ui.transportation.widget.MVVWidgetConfigureActivity

@Subcomponent(modules = [TransportModule::class])
interface TransportComponent {

    fun inject(transportationActivity: TransportationActivity)

    fun inject(transportationDetailsActivity: TransportationDetailsActivity)

    fun inject(mvvWidgetConfigureActivity: MVVWidgetConfigureActivity)

    fun inject(mvvWidget: MVVWidget)

    @Subcomponent.Builder
    interface Builder {

        fun transportModule(transportModule: TransportModule): TransportComponent.Builder

        fun build(): TransportComponent

    }

}
