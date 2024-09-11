package org.radarbase.datadashboard.api.resource

import jakarta.annotation.Resource
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import org.slf4j.LoggerFactory

@Resource
@Path("exception")
class ExceptionResource() {
    @GET
    @Path("throw")
    fun throwException() {
        return throw RuntimeException("This is a test exception; 2nd try")
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
