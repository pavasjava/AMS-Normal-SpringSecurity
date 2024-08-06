package com.ams.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
	

	  @GetMapping("/login")
	    public String showLoginPage() {
	        return "login";
	    }
}

//@GetMapping("/login")
//public String showLoginPage(@RequestParam(name = "error", required = false) String error, Model model) {
//  if (error != null) {
//        model.addAttribute("error", true);
//    }
//    return "loginDemo";
//}
