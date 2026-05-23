package cz.rentflow.web.ui

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

@ControllerAdvice
class GlobalModelAdvice {
    @ModelAttribute("currentUri")
    fun currentUri(request: HttpServletRequest): String = request.requestURI
}
