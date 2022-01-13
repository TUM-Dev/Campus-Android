package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model

enum class CafeteriaLocation {
    STUCAFE_KARLSTR,
    STUCAFE_BOLTZMANNSTR,
    STUCAFE_GARCHING,
    STUCAFE_PASING,
    STUCAFE_AKADEMIE_WEIHENSTEPHAN,
    STUCAFE_ADALBERTSTR,
    STUBISTRO_SCHELLINGSTR,
    STUBISTRO_ROSENHEIM,
    STUBISTRO_ARCISSTR,
    STUBISTRO_GROSSHADERN,
    STUBISTRO_GOETHESTR,
    FMI_BISTRO,
    IPP_BISTRO,
    MENSA_PASING,
    MENSA_LOTHSTR,
    MENSA_LEOPOLDSTR,
    MENSA_GARCHING,
    MENSA_ARCISSTR,
    MENSA_MARTINSRIED,
    MENSA_WEIHENSTEPHAN,
    MEDIZINER_MENSA;

    /**
     * Used to get the actual raw string cafeteriaId stored in the database. This method should be called when interacting
     * with a DAO method that requires a cafeteriaId as a parameter.
     */
    fun toId(): String {
        return this.toString().replace('_','-').lowercase()
    }
}
