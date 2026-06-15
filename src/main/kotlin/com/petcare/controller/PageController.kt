package com.petcare.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.view.RedirectView

@Controller
class PageController {
    @GetMapping("/")
    fun root(): RedirectView = RedirectView("/login.html")

    @GetMapping("/login")
    fun login(): RedirectView = RedirectView("/login.html")

    @GetMapping("/register")
    fun register(): RedirectView = RedirectView("/register.html")
}
