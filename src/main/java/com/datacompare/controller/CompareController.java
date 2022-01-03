package com.datacompare.controller;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.datacompare.model.AppProperties;
import com.datacompare.service.CompareService;

@Controller
public class CompareController {
	
	private Boolean toolRunning = Boolean.FALSE;
	
	public static String reportFileName = "XXX";
	
	public static String reportOutputFolder = null;

	@GetMapping("/")
	public String index(Model model) {
		
		if(toolRunning.booleanValue()) {
			
			model.addAttribute("msg", "Data Compare is in progress.");
			model.addAttribute("fileName", reportFileName);
			
			return "result";
			
		} else {
			
			reportFileName = "XXX";
			
			AppProperties appProperties = new AppProperties();
			
			appProperties.setMaxDecimals(5); 
			appProperties.setMaxNoofThreads(1);
			appProperties.setFetchSize(10000); 
			
			model.addAttribute("datacompare", appProperties);
			
			return "datacompare";
		}
	}
	
	@GetMapping("/result")
	public String result(Model model, HttpServletRequest request) {
		model.addAttribute("msg", toolRunning.booleanValue() ? "Data Compare is in progress." : "Data Compare completed.");
		
		if(!"XXX".equals(reportFileName)) {
			
			model.addAttribute("fileName", request.getRequestURL().toString() + "/view/" + reportFileName);
			
			return "redirect:/result/view/" + reportFileName;
		}
		
		model.addAttribute("fileName", reportFileName);

		return "result";
	}
	
	@RequestMapping(value = "/result/view/{filename}", method = RequestMethod.GET)
	public @ResponseBody FileSystemResource resultView(Model model, @PathVariable String filename) {
		
		File f = (reportOutputFolder != null && !reportOutputFolder.isEmpty()) ? new File(reportOutputFolder, filename) : new File(filename);
		return new FileSystemResource(f);
	}
	
	@PostMapping("/datacompare")
	public String datacompareSubmit(@ModelAttribute AppProperties appProperties) {
		
		if(toolRunning.booleanValue()) {
		
			return "redirect:/result";
		}
		
		System.out.println("Properties: "+ appProperties);
		
		toolRunning = Boolean.TRUE;
		reportFileName = "XXX";
		
		CompareService compareService = new CompareService();
		compareService.startService(appProperties); 
		
		toolRunning = Boolean.FALSE;
		
		return "redirect:/result";
	}
}