package com.springboot.contactManager.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;

import com.springboot.contactManager.dao.ContactRepository;
import com.springboot.contactManager.dao.UserRepository;
import com.springboot.contactManager.entity.ContactDetails;
import com.springboot.contactManager.entity.User;
import com.springboot.contactManager.helper.Message;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;

	@ModelAttribute
	public void commonUserData(Model m, Principal p) {
		String username = p.getName();
		User userDetail = userRepository.getUserbyUserName(username);
		System.out.println("UserDetails: " + userDetail);
		System.out.println("UserName: " + username);
		m.addAttribute("user", userDetail);
	}

	@GetMapping("/index")
	public String dashboard(Model m, Principal p) {

		m.addAttribute("title", "User Dashboard");
		return "normal/dashboard";
	}

	@GetMapping("/add-contact")
	public String addContact(Model m) {
		m.addAttribute("title", "Add Contact");
		m.addAttribute("contact", new ContactDetails());

		return "/normal/add-contact";
	}

	@PostMapping("/process-contact")
	// @RequestMapping(value = { "/process-contact" }, method = RequestMethod.POST,
	// consumes = {"multipart/form-data"})
	public String processContact(
			Model m,
			@ModelAttribute ContactDetails contact,
			@RequestParam("uploadImage") MultipartFile file,
			Principal p,
			HttpSession session) {
		try {
			System.out.println("Entering in Process control form...");
			m.addAttribute("title", "Process Form");
			String name = p.getName();
			User user = this.userRepository.getUserbyUserName(name);
			LocalDateTime current = LocalDateTime.now();
			DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
			String formatedDateTime = current.format(format);
			String fileName = formatedDateTime.concat(file.getOriginalFilename());
			// processing and uploading file
			if (file.isEmpty()) {
				System.out.println("File is empty");
				contact.setImageName(formatedDateTime.concat("contact.png"));

			} else {
				contact.setImageName(fileName);
				// System.out.println("File: " + file.toString());
				contact.setUploadImage(file.getOriginalFilename().getBytes());
				File saveFile = new ClassPathResource("static/images").getFile();

				Path path = Paths
						.get(saveFile.getAbsolutePath() + File.separator +
								fileName);

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("Image is uploaded");

			}

			// System.out.println("contact : " + contact);
			contact.setUser(user);
			user.getCdList().add(contact);

			this.userRepository.save(user);
			System.out.println("User is save to the datebase");

			// Success message...
			session.setAttribute("message", new Message("You contact is added successfully...", "success"));

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
			// Error Message...
			session.setAttribute("message", new Message("Error in saving contact..!!", "danger"));
		}

		return "/normal/add-contact";
	}

	// Showing Contacts
	// Per page 5 contact
	// Current page = 0 page
	@GetMapping("/show-contact/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal p, HttpSession session) {
		m.addAttribute("title", "Show Contacts");
		String name = p.getName();
		User user = this.userRepository.getUserbyUserName(name);
		Pageable pageable = PageRequest.of(page, 5);
		Page<ContactDetails> contacts = this.contactRepository.findContactByUser(user.getUid(), pageable);
		// System.out.println(contacts);

		m.addAttribute("contacts", contacts);
		m.addAttribute("currectPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());
		System.out.println("Contact Printed Successfully...");

		return "normal/show-contacts";
	}

	// Showing individual user detail by ID
	@RequestMapping("/{cid}/contact")
	public String showContactDetails(@PathVariable("cid") Integer cid, Model m, Principal p) {
		System.out.println("CID: " + cid);
		Optional<ContactDetails> contactByID = this.contactRepository.findById(cid);
		ContactDetails contactDetailsByID = contactByID.get();
		String name = p.getName();
		User user = this.userRepository.getUserbyUserName(name);

		if (user.getUid() == contactDetailsByID.getUser().getUid()) {
			m.addAttribute("details", contactDetailsByID);
			m.addAttribute("title", contactDetailsByID.getName());
		}

		return "normal/contact-details";
	}

	// handle delete contact
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid, HttpSession session, Principal p) {
		try {
			System.out.println("CID for deletion:" + cid);
			Optional<ContactDetails> findById = this.contactRepository.findById(cid);
			ContactDetails contactToDelete = findById.get();

			// Commenting down below line because contact isnot deleted
			// contactToDelete.setUser(null);

			// Removing photos before removing contact
			String fileName = contactToDelete.getImageName();
			File saveFile = new ClassPathResource("static/images").getFile();
			Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + fileName);
			System.out.println("Image Path:" + path);
			Files.delete(path);
			System.out.println("Successfully Deleted.." + fileName);

			System.out.println("Image is uploaded");

			// Commenting below line because contact not delete bcz of cascade.all
			// this.contactRepository.delete(contactToDelete);

			User user = this.userRepository.getUserbyUserName(p.getName());
			user.getCdList().remove(contactToDelete);
			this.userRepository.save(user);

			System.out.println("Contact Deleted Successfully...");
			session.setAttribute("message", new Message("Contact Delete Successfully...", "success"));
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
		}

		return "redirect:/user/show-contact/0";
	}

	// open update form Handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model m) {
		m.addAttribute("title", "Update Contact");
		System.out.println("CID for update form:" + cid);
		ContactDetails contactToUpdate = this.contactRepository.findById(cid).get();
		m.addAttribute("contact", contactToUpdate);
		return "normal/update-form";
	}

	// update contact Handler
	@PostMapping("/update-contact")
	public String updateFormHandler(
			@ModelAttribute ContactDetails contact,
			@RequestParam("uploadImage") MultipartFile file,
			Model m,
			HttpSession session,
			Principal p) {
		try {
			System.out.println("Contact Id: " + contact.getCid());
			System.out.println("Contact Name: " + contact.getName());
			ContactDetails oldContact = this.contactRepository.findById(contact.getCid()).get();
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss-");
			String dateStr = dateFormat.format(cal.getTime());
			String fileName = dateStr.concat(file.getOriginalFilename());
			if (!file.isEmpty()) {
				// Deleting previous image from the location
				File oldFile = new ClassPathResource("static/images").getFile();
				Path oldPath = Paths.get(oldFile.getAbsolutePath() + File.separator + oldContact.getImageName());
				System.out.println("Old File Name: " + oldPath);
				Files.delete(oldPath);

				// Updating new Image
				File saveFile = new ClassPathResource("static/images").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + fileName);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setUploadImage(file.getOriginalFilename().getBytes());
				contact.setImageName(fileName);
				// if (oldContact.getImageName() == null) {
				// // Updating new Image
				// File saveFile = new ClassPathResource("static/images").getFile();
				// String fileName = file.getOriginalFilename().concat(dateStr);
				// Path path = Paths.get(saveFile.getAbsolutePath() + File.separator +
				// fileName);
				// Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				// contact.setUploadImage(file.getOriginalFilename().getBytes());
				// contact.setImageName(file.getOriginalFilename());
				// } else {

				// }

			} else {
				contact.setUploadImage(oldContact.getUploadImage());
				contact.setImageName(oldContact.getImageName());
			}

			User user = this.userRepository.getUserbyUserName(p.getName());
			contact.setUser(user);

			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Profile Update Successfully...", "success"));

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return "redirect:/user/" + contact.getCid() + "/contact";
	}

	@GetMapping("/profile")
	public String getProfile(Model m) {
		m.addAttribute("title", "User Profile");
		return "/normal/profile";
	}

	@InitBinder
	protected void initBinder(HttpServletRequest request,
			ServletRequestDataBinder binder) throws ServletException {

		// Convert multipart object to byte[]
		binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());

	}

}
