package com.nikolas.mechanicalmanagementsystem.controller;

import com.nikolas.mechanicalmanagementsystem.dtos.InvoiceDataDTO;
import com.nikolas.mechanicalmanagementsystem.entity.*;
import com.nikolas.mechanicalmanagementsystem.exception.InvoiceNotFoundException;
import com.nikolas.mechanicalmanagementsystem.exception.MachineNotFoundException;
import com.nikolas.mechanicalmanagementsystem.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Controller
@RequiredArgsConstructor
@Slf4j
public class MachineController {
    private final MachineService machineService;
    private final ImagesService imagesService;
    private final UserService userService;
    private final MachinePartService machinePartService;
    private final CompanyService companyService;
    private final InvoiceService invoiceService;

    //    handler method to create new machine
    @PostMapping("/machines/addNew")
    public String addNewMachine(@ModelAttribute Machine machine, @RequestParam("companyId") Long companyId, Model model) {
        try {
            companyService.addNewMachineToCompany(machine, companyId);
            return "redirect:/machines";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }


    //handler method to inactivate machine
    @GetMapping("/deleteMachine/{id}")
    public String deleteMachine(@PathVariable Long id) {
        machineService.deleteMachineById(id);
        return "redirect:/machines";
    }

    //handler method to delete machine
    @GetMapping("/deleted_machines/{id}")
    public String deleteInactiveMachine(@PathVariable Long id) {
        machineService.deleteInactiveMachineById(id);
        return "redirect:/deleted_machines";
    }

    //get machines by year, month and selected company
    @RequestMapping(value = "/machines", method = {RequestMethod.GET, RequestMethod.POST})
    public String getMachinesBySelectedYearMonthCompanyIds(@RequestParam(name = "year", required = false) Integer year,
                                     @RequestParam(name = "month", required = false) Integer month,
                                     @RequestParam(name = "companyIds", required = false) List<Long> selectedCompanyIds,
                                     Model model, HttpSession session) {
        User loggedInUser = userService.getLoggedInUser();

        List<Year> years = IntStream.rangeClosed(LocalDateTime.now().getYear() - 5, Year.now().getValue())
                .mapToObj(Year::of)
                .collect(Collectors.toList());
        List<Month> months = Arrays.asList(Month.values());

        List<Company> userCompanies = companyService.getAllCompaniesByUser(loggedInUser);
        List<Long> selectedCompanies = companyService.getSelectedCompaniesByIds(session, selectedCompanyIds, userCompanies);
        Integer selectedYear = companyService.getMachinesByYear(session, year);
        Integer selectedMonth = companyService.getMachinesByMonth(session, month);
        List<Machine> machines = machineService.getActiveMachinesByYearMonthAndCompanyId(selectedYear, selectedMonth, selectedCompanies);
        BigDecimal totalPartsAmount = machineService.calculateTotalPartsAmountByMonthAndYearAndCompanies(selectedYear, selectedMonth, selectedCompanies);
        BigDecimal totalChargedAmount = machineService.calculateTotalChargedAmountByMonthAndYearAndCompanies(selectedYear, selectedMonth, selectedCompanies);
        BigDecimal totalProfit = machineService.calculateTotalProfitByMonthAndYearAndCompanies(selectedYear, selectedMonth, selectedCompanies);

        model.addAttribute("companies", userCompanies);
        model.addAttribute("selectedCompanies", selectedCompanies);
        model.addAttribute("years", years);
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("months", months);
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("machines", machines);
        model.addAttribute("totalPartsAmount", totalPartsAmount);
        model.addAttribute("totalChargedAmount", totalChargedAmount);
        model.addAttribute("totalProfit", totalProfit);

        return "machines";
    }


    //one machine view form
    @GetMapping("/machines/view/{id}")
    public String getMachineDetails(@PathVariable Long id, Model model) {
        try {
            User loggedInUser = userService.getLoggedInUser();
            Machine machine = machineService.getMachineForUser(loggedInUser, id);
            machineService.convertImagesToBase64(machine);
            model.addAttribute("machine", machine);
            return "machine_details";
        } catch (MachineNotFoundException e) {
            log.warn("MachineController::getMachineDetails - Machine not found with id: " + id);
            return "redirect:/error?machineNotFound";
        } catch (AccessDeniedException e) {
            log.warn("MachineController::getMachineDetails - No access to the machine: " + id);
            return "redirect:/accessDenied";
        }
    }


    //update machine
    @PostMapping("/updateMachines/{id}")
    public String updateMachineDetails(@Valid @PathVariable Long id, @ModelAttribute("machine") @Valid Machine machine, BindingResult result) {
        if (result.hasErrors()) {
            for (FieldError error : result.getFieldErrors()) {
                log.error("MachineController::updateMachineDetails - Error in the field: " + error.getField() + ", error message: " + error.getDefaultMessage());
            }
            return "redirect:error?errorDuringUpdateMachine";
        }
        machineService.updateMachine(machine);
        return "redirect:/machines/view/" + id + "?successfullyUpdated";
    }

    //Update Tax
    @PostMapping("/updateTax/{id}")
    public String updateTax(@PathVariable("id") Long machineId, @RequestParam("taxInPercent") BigDecimal taxInPercent) {
        User loggedInUser = userService.getLoggedInUser();
        Machine machine = machineService.getMachineForUser(loggedInUser, machineId);
        machineService.updateMachinePartsTax(machine, taxInPercent);
        return "redirect:/machines/view/" + machineId;
    }

    //add machinePart to machine
    @PostMapping("/addMachinePart/{id}")
    public String saveNewPart(@PathVariable("id") Long machineId, @ModelAttribute("machinePart") MachinePart machinePart) {
        User loggedInUser = userService.getLoggedInUser();
        Machine machine = machineService.getMachineForUser(loggedInUser, machineId);
        machinePartService.addMachinePartToMachine(machine, machinePart);
        return "redirect:/machines/view/" + machineId;
    }

    //Delete machinePart from Machine
    @GetMapping("/deleteMachinePart/{machineId}/{partId}")
    public String deleteMachinePart(@PathVariable("machineId") Long machineId, @PathVariable("partId") Long partId) {
        machineService.deleteMachinePart(machineId, partId);
        return "redirect:/machines/view/" + machineId;
    }

    //List of deleted/deActivated machines
    @GetMapping("/deleted_machines")
    public String getDeletedMachinesByMonth(Model model) {
        List<Machine> deletedMachines = machineService.getDeletedMachines();
        model.addAttribute("deletedMachines", deletedMachines);
        return "deleted_machines";
    }


    //Upload Image
    @PostMapping("/machines/view/{machineId}/uploadImage")
    public String uploadImage(@PathVariable Long machineId, @RequestParam("image") List<MultipartFile> file) {
        User loggedInUser = userService.getLoggedInUser();
        Machine machine = machineService.getMachineForUser(loggedInUser, machineId);
        try {
            imagesService.uploadAndSaveImage(file, machine);
            return "redirect:/machines/view/" + machineId;
        } catch (MaxUploadSizeExceededException | IOException e) {
            return "redirect:/machines/view/" + machineId + "?uploadFailed=true";
        } catch (Exception e) {
            return "redirect:/error?imageUploadError";
        }
    }


    // Invoice generation
    @GetMapping("/invoice/{id}")
    public String redirectToInvoice(@PathVariable Long id, Model model) {
        try {
            InvoiceDataDTO invoiceDataDTO = invoiceService.generateInvoice(id);
            model.addAttribute("invoiceData", invoiceDataDTO);
            return "invoice";
        } catch (InvoiceNotFoundException e) {
            log.warn("MachineController::redirectToInvoice - No such invoice");
            return "redirect:/error?invoiceNotFound";
        } catch (MachineNotFoundException e) {
            log.warn("MachineController::redirectToInvoice - Invoice not found to this machine!");
            return "redirect:/error?invoiceNotFound";
        }
    }

    // Delete image
    @GetMapping("/deleteImage/{imageId}")
    public String deleteImage(@PathVariable Long imageId, @RequestParam(name = "machineId") Long machineId) {
        imagesService.deleteImage(imageId);
        return "redirect:/machines/view/" + machineId;
    }

}
