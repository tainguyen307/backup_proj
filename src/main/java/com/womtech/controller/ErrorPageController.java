package com.womtech.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorPageController {
	@GetMapping("/auth/access-denied")
	 public String accessDenied() { return "error/403"; } // tạo templates/error/403.html
	
	 @GetMapping("/error/403")
	    public String direct403() {
	        return "error/403";
	    }
}
