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
        return mapStringToIdPattern(this.toString())
    }

    companion object {
        /**
         * Finds the CafeteriaLocation corresponding to the provided string. Used to get a CafeteriaLocation from strings stored in the preferences or database.
         */
        fun fromString(raw: String?): CafeteriaLocation? {
            if(raw != null){
                values().forEach { cafeteriaLocation: CafeteriaLocation ->
                    if(cafeteriaLocation.toId() == mapStringToIdPattern(raw))
                        return cafeteriaLocation
                }
            }

            return null
        }

        /**
         * @param raw the input string upon which the cafeteria id pattern is to be enforced
         * @return string complying with the cafeteria if pattern
         */
        private fun mapStringToIdPattern(raw: String): String {
            return raw.replace('_','-').lowercase()
        }
    }
}
