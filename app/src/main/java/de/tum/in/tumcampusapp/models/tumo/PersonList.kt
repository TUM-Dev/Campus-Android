package de.tum.`in`.tumcampusapp.models.tumo

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

/**
 * Wrapper class holding a list of persons. Note: This model is based on the
 * TUMOnline web service response format for a corresponding request.
 */

@Root(name = "rowset")
data class PersonList(@field:ElementList(inline = true, required = false)
                      var persons: List<Person> = mutableListOf())
