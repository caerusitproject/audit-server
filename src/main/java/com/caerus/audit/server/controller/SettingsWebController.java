package com.caerus.audit.server.controller;

import com.caerus.audit.server.dto.ServerAppSettingsDto;
import com.caerus.audit.server.service.ServerAppSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings-ui")
@RequiredArgsConstructor
public class SettingsWebController {

    private final ServerAppSettingsService service;

    @GetMapping
    public String viewSettings(Model model){
        ServerAppSettingsDto settingsDto = service.getLatest();
        if(settingsDto == null){
            settingsDto = new ServerAppSettingsDto();
        }
        model.addAttribute("settings", settingsDto);
        return "settings";
    }

    @PostMapping
    public String updateSettings(@Valid @ModelAttribute("settings") ServerAppSettingsDto settingsDto,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes){
        if(result.hasErrors()){
            return "settings";
        }
        service.update(settingsDto);
        redirectAttributes.addFlashAttribute("successMessage", "Settings updated successfully!");
        return "redirect:/settings-ui";
    }
}
