package com.concepts.conceptsDemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@CrossOrigin("http://localhost:4200")
@RequestMapping("payroll")
public class Controller {
    @Autowired
    private PayrollProcessingService payrollProcessingService;

    @GetMapping(value = "/demo",produces = "application/json")
    public ResponseEntity<Map<Long, Map<String, Double>>> processPayroll() {
        Map<Long, Map<String, Double>> payrollReport = payrollProcessingService.processPayrollItems();
        return new ResponseEntity<>(payrollReport, HttpStatus.OK);
    }
}
