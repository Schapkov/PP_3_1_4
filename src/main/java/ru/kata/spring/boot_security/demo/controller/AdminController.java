package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;


    @Autowired
    public AdminController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;

    }

    @GetMapping("/admin")
    public String showAllUsers(Model model, Principal principal) {
        model.addAttribute("user", userService.findByUsername(principal.getName()));
        model.addAttribute("allUsers", userService.findAll());
        model.addAttribute("allRoles", roleService.getAllRole());

        return "admin-page";
    }

    @GetMapping("admin/new")
    public String newUser( Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleService.getAllRole());
        return "add-new-user";
    }

    @PostMapping("admin/new")
    private String createUser(@RequestParam("roles") List<Long> roles, @ModelAttribute("user") @Valid User user, BindingResult bindingResult, Model model) {
        User checkUserName = userService.findByUsername(user.getUsername());
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", roleService.getAllRole());
            return "add-new-user";
        }
        if (checkUserName != null) {
            FieldError error = new FieldError("user", "username", "Username is already in use ");
            bindingResult.addError(error);
            model.addAttribute("roles", roleService.getAllRole());
           return "add-new-user";
        }

        user.setRoles(roleService.getRoleById(roles));
        userService.saveUser(user);
        return "redirect:/admin";
    }


    @PutMapping("/admin/{id}/edit")
    public String update(@RequestParam("roles") List<Long> roles, @ModelAttribute("user") @Valid User user, BindingResult bindingResult, Model model) {
        User checkUserName = this.userService.findByUsername(user.getUsername());

        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", roleService.getAllRole());
            return  "redirect:/admin";
        }
//        Так и не смог понять, как вернуть это в модальное окно не используя еще одно отображение?
        if (checkUserName != null &&
                !Objects.equals(checkUserName.getId(), user.getId())) {
            FieldError error = new FieldError("user", "username", "Username is already in use ");
            bindingResult.addError(error);

            return "redirect:/admin";
        }
        user.setRoles(roleService.getRoleById(roles));
        userService.saveUser(user);
        return "redirect:/admin";
    }

    @DeleteMapping("/admin/{id}/delete")
    public String delete(@PathVariable("id") Long id) {
        userService.deleteById(id);
        return "redirect:/admin";
    }


}
