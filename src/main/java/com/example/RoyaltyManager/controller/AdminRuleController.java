package com.example.RoyaltyManager.controller;

import com.example.RoyaltyManager.model.*;
import com.example.RoyaltyManager.repository.*;
import com.example.RoyaltyManager.service.RoyaltyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminRuleController {

    @Autowired private PlatformConfigRepository platformRepo;
    @Autowired private RoyaltyRuleRepository royaltyRuleRepo;
    @Autowired private RegionRateRepository regionRateRepo;
    @Autowired private AuditLogRepository auditLogRepo;
    @Autowired private RoyaltyService royaltyService;
    @Autowired private DisputeRepository disputeRepo;
    @Autowired private ContractRepository contractRepo;

    // ─── Helper: log an admin action ───
    private void log(String action, String entity, String entityId, String desc, String severity) {
        auditLogRepo.save(new AuditLog(action, entity, entityId, "SysAdmin", desc, severity));
    }

    // ══════════════════════════════════════
    //  1. Platform Master
    // ══════════════════════════════════════
    @GetMapping("/platforms")
    public String platforms(HttpSession session, Model model) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        model.addAttribute("platforms", platformRepo.findAll());
        model.addAttribute("newPlatform", new PlatformConfig());
        return "platform_master";
    }

    @PostMapping("/platforms/save")
    public String savePlatform(@ModelAttribute PlatformConfig p, HttpSession session) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        if (p == null) return "redirect:/admin/platforms?error=null";
        platformRepo.save(p);
        log("CREATE", "PlatformConfig", p.getPlatformName(), "Added platform: " + p.getPlatformName(), "INFO");
        return "redirect:/admin/platforms";
    }

    @PostMapping("/platforms/delete/{id}")
    public String deletePlatform(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        if (id == null) return "redirect:/admin/platforms";
        platformRepo.findById(id).ifPresent(p -> {
            log("DELETE", "PlatformConfig", p.getPlatformName(), "Removed platform: " + p.getPlatformName(), "WARNING");
            platformRepo.delete(p);
        });
        return "redirect:/admin/platforms";
    }

    // ══════════════════════════════════════
    //  2. Royalty Rule Configuration
    // ══════════════════════════════════════
    @GetMapping("/royalty-rules")
    public String royaltyRules(HttpSession session, Model model) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        model.addAttribute("rules", royaltyRuleRepo.findAll());
        model.addAttribute("newRule", new RoyaltyRule());
        model.addAttribute("platforms", platformRepo.findAll());
        return "royalty_rule_config";
    }

    @PostMapping("/royalty-rules/save")
    public String saveRoyaltyRule(@ModelAttribute RoyaltyRule rule, HttpSession session) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        if (rule == null) return "redirect:/admin/royalty-rules";
        // Auto-compute label split
        rule.setLabelSplitPercent(100.0 - rule.getArtistSplitPercent());
        royaltyRuleRepo.save(rule);
        log("CREATE", "RoyaltyRule", rule.getRuleName(), "Rule saved: " + rule.getRuleName(), "INFO");
        return "redirect:/admin/royalty-rules";
    }

    @PostMapping("/royalty-rules/delete/{id}")
    public String deleteRule(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        if (id == null) return "redirect:/admin/royalty-rules";
        royaltyRuleRepo.findById(id).ifPresent(r -> {
            log("DELETE", "RoyaltyRule", r.getRuleName(), "Deleted rule: " + r.getRuleName(), "WARNING");
            royaltyRuleRepo.delete(r);
        });
        return "redirect:/admin/royalty-rules";
    }

    // ══════════════════════════════════════
    //  3. Region-based Rate Setup
    // ══════════════════════════════════════
    @GetMapping("/region-rates")
    public String regionRates(HttpSession session, Model model) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        model.addAttribute("rates", regionRateRepo.findAll());
        model.addAttribute("newRate", new RegionRate());
        return "region_rate_setup";
    }

    @PostMapping("/region-rates/save")
    public String saveRegionRate(@ModelAttribute RegionRate rate, HttpSession session) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        if (rate == null) return "redirect:/admin/region-rates";
        regionRateRepo.save(rate);
        log("CREATE", "RegionRate", rate.getRegionCode(), "Region rate saved: " + rate.getRegionCode(), "INFO");
        return "redirect:/admin/region-rates";
    }

    @PostMapping("/region-rates/delete/{id}")
    public String deleteRegionRate(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        if (id == null) return "redirect:/admin/region-rates";
        regionRateRepo.deleteById(id);
        log("DELETE", "RegionRate", id.toString(), "Region rate deleted", "WARNING");
        return "redirect:/admin/region-rates";
    }

    // ══════════════════════════════════════
    //  4. Contract Type Management
    // ══════════════════════════════════════
    @GetMapping("/contract-types")
    public String contractTypes(HttpSession session, Model model) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        model.addAttribute("contracts", royaltyService.getAllContracts());
        model.addAttribute("artists", royaltyService.getAllArtists());
        model.addAttribute("newContract", new Contract());
        return "contract_type_mgmt";
    }

    @PostMapping("/contracts/save")
    public String saveContract(@ModelAttribute Contract c, HttpSession session) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        if (c == null) return "redirect:/admin/contract-types";
        royaltyService.saveContract(c);
        log("CREATE", "Contract", c.getId() != null ? c.getId().toString() : "NEW", "Contract saved for artist: " + (c.getArtist() != null ? c.getArtist().getName() : "Unknown"), "INFO");
        return "redirect:/admin/contract-types";
    }

    @PostMapping("/contracts/delete/{id}")
    public String deleteContract(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        if (id == null) return "redirect:/admin/contract-types";
        contractRepo.findById(id).ifPresent(c -> {
            log("DELETE", "Contract", id.toString(), "Deleted contract for: " + (c.getArtist() != null ? c.getArtist().getName() : "Unknown"), "WARNING");
            contractRepo.delete(c);
        });
        return "redirect:/admin/contract-types";
    }

    // ══════════════════════════════════════
    //  5. Revenue Share Rule Editor
    // ══════════════════════════════════════
    @GetMapping("/revenue-share")
    public String revenueShare(HttpSession session, Model model) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        model.addAttribute("rules", royaltyRuleRepo.findAll());
        model.addAttribute("platforms", platformRepo.findAll());
        model.addAttribute("totalRevenue", royaltyService.getTotalRevenue());
        return "revenue_share_editor";
    }

    // ══════════════════════════════════════
    //  6. Dispute Detection Rule Form
    // ══════════════════════════════════════
    @GetMapping("/dispute-rules")
    public String disputeRules(HttpSession session, Model model) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        model.addAttribute("disputes", disputeRepo.findAll());
        model.addAttribute("rules", royaltyRuleRepo.findAll());
        return "dispute_rule_form";
    }

    // ══════════════════════════════════════
    //  7. Royalty Calculation Engine View
    // ══════════════════════════════════════
    @GetMapping("/calc-engine")
    public String calcEngine(HttpSession session, Model model) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        List<com.example.RoyaltyManager.model.RoyaltyTransaction> txns = royaltyService.getAllTransactions();
        double totalGross = royaltyService.getTotalRevenue();
        model.addAttribute("transactions", txns);
        model.addAttribute("totalGross", totalGross);
        model.addAttribute("artistPayout", totalGross * 0.70);
        model.addAttribute("labelCut", totalGross * 0.30);
        model.addAttribute("platforms", platformRepo.findAll());
        model.addAttribute("rules", royaltyRuleRepo.findAll());
        return "calculation_engine";
    }

    @PostMapping("/calc-engine/trigger")
    public String triggerCalc(HttpSession session, Model model) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        log("ACTION", "CalcEngine", "RUN", "Manual royalty calculation triggered", "INFO");
        return "redirect:/admin/calc-engine?success=true";
    }

    // ══════════════════════════════════════
    //  8. System Audit Logs
    // ══════════════════════════════════════
    @GetMapping("/audit-logs")
    public String auditLogs(@RequestParam(required = false) String severity, HttpSession session, Model model) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        
        List<AuditLog> logs;
        if (severity != null && !severity.isEmpty() && !severity.equals("ALL")) {
            logs = auditLogRepo.findBySeverity(severity);
        } else {
            logs = auditLogRepo.findAllByOrderByTimestampDesc();
        }
        
        model.addAttribute("logs", logs);
        model.addAttribute("totalLogs", auditLogRepo.count());
        model.addAttribute("criticalCount", auditLogRepo.findBySeverity("CRITICAL").size());
        model.addAttribute("warningCount", auditLogRepo.findBySeverity("WARNING").size());
        return "audit_logs";
    }
}
