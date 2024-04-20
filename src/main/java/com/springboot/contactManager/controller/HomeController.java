package com.springboot.contactManager.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;

import com.springboot.contactManager.dao.UserRepository;
import com.springboot.contactManager.entity.User;
import com.springboot.contactManager.helper.Message;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	@GetMapping("/")
	public String home(Model m) {
		m.addAttribute("title", "Home- Contact Manager");
		return "home";
	}

	@GetMapping("/about")
	public String about(Model m) {
		m.addAttribute("title", "About-Contact Manager");
		return "about";
	}

	@GetMapping("/signup")
	public String signup(Model m) {
		m.addAttribute("title", "Register-Contact Manager");
		m.addAttribute("user", new User());
		return "signup";
	}

	// @PostMapping("/register")
	// public String newRegister(
	// @ModelAttribute("user") User user,
	// @RequestParam("image") MultipartFile file) {

	// System.out.println("File: " + file.getOriginalFilename());
	// return "";
	// }

	// Handling Sign Up form
	// @RequestMapping(value = "/register",method = RequestMethod.POST )
	@PostMapping("/register")
	public String handleSignUpForm(
			@Valid @ModelAttribute("user") User user,

			BindingResult b,
			@RequestParam("image") MultipartFile file,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement,

			Model m,
			HttpSession session) {

		try {
			System.out.println("Entering in process sign up page....");
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss-");
			String dateStr = dateFormat.format(cal.getTime());
			String fileName = dateStr.concat(file.getOriginalFilename());
			if (!agreement) {
				System.out.println("You have not agreed the term & conditions.");
				throw new Exception("You have not agreed the term & conditions.");
			}
			if (b.hasErrors()) {
				m.addAttribute("user", user);
				System.out.println("Error:" + b.toString());
				session.setAttribute("message", new Message(b.toString(), "alert-success"));
				return "signup";
			}

			// Setting image in SignUp Page

			// Cheking of image
			if (file.isEmpty()) {
				System.out.println("No Images is Selected");

			} else {

				user.setImage(file.getOriginalFilename().getBytes());
				user.setImageName(fileName);
				File filePath = new ClassPathResource("static/images").getFile();
				Path path = Paths.get(filePath.getAbsolutePath(), File.separator, fileName);
				long copy = Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("File Full Name" + copy);

			}

			user.setRole("User_RO");
			user.setEnabled(true);
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			System.out.println("T&C: " + agreement);
			System.out.println("User" + user);
			User result = this.userRepository.save(user);
			System.out.println("Result: " + result);
			m.addAttribute("user", new User());
			session.setAttribute("message", new Message("Successfully Registered", "alert-success"));
			return "signup";

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			m.addAttribute("user", user);
			session.setAttribute("message", new Message("Something Went Wrong!!" + e.getMessage(), "alert-danger"));
			return "signup";
		}

	}

	@GetMapping("/signin")
	public String customLogin(Model m) {

		m.addAttribute("title", "Login Page");
		return "login";
	}

	// @GetMapping("/login-fail")
	// public String loginError(Model m) {
	// return "login-";
	// }

	@InitBinder
	protected void initBinder(HttpServletRequest request,
			ServletRequestDataBinder binder) throws ServletException {

		// Convert multipart object to byte[]
		binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());

	}

}
