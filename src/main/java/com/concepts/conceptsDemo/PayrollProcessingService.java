package com.concepts.conceptsDemo;

import com.fathzer.soft.javaluator.DoubleEvaluator;
import com.concepts.conceptsDemo.domain.CalculatingConcept;
import com.concepts.conceptsDemo.domain.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PayrollProcessingService {
    @Autowired
    private ConceptRepository conceptRepository;
    private final String[] operators = {"+", "-", "*", "/", "<", ">", "if(", "(", ")", ":", ";", "SALARIO"};
    //private final List<CalculatingConcept> establishedConcepts;
    private final DoubleEvaluator evaluator = new DoubleEvaluator();

    public Map<Long, Map<String, Double>> processPayrollItems() {

        List<CalculatingConcept> establishedConcepts = new ArrayList<>();

        Map<String, CalculatingConcept> concepts = conceptRepository.getCalculatingConcept();

        Map<String, CalculatingConcept> numericConcepts = concepts.entrySet()
                .stream().filter(concept -> concept.getValue().getValueType().equals("NUMERICO"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, CalculatingConcept> formulationConcepts = concepts.entrySet()
                .stream().filter(concept -> concept.getValue().getValueType().equals("FORMULACION"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        for (String key : formulationConcepts.keySet()) {
            CalculatingConcept concept = formulationConcepts.get(key);
            String value = concept.getValue();

            String[] tokens = value.split(" ");

            String[] variables = Arrays.stream(tokens)
                    .filter(elem -> Arrays.stream(operators).noneMatch(op -> op.equals(elem)) && !elem.matches("\\d+"))
                    .distinct()
                    .toArray(String[]::new);

            for (String var : variables){
                if (numericConcepts.containsKey(var)){
                    for (int i = 0; i < tokens.length; i++){
                        if (tokens[i].equals(var)) tokens[i] = numericConcepts.get(var).getValue();
                    }
                }
            }
            concept.setConceptName(key);
            concept.setValue(convertListToString(Arrays.stream(tokens).toList()));
            establishedConcepts.add(concept);
        }

        establishedConcepts.sort(Comparator.comparingInt(CalculatingConcept::getPriority));

        for (CalculatingConcept concept : establishedConcepts){
            System.out.println(concept.getConceptName());
            System.out.println(concept.getValue());
            System.out.println(concept.getPriority());
        }

        //Esto se solicita de la db pidiendo los Employee con vinculaciones activads
        Employee Employee1 = new Employee(11111111L, 2500000);
        Employee Employee2 = new Employee(22222222L, 5000000);

        List<Employee> Employees = new ArrayList<>();

        Employees.add(Employee1);
        Employees.add(Employee2);

        Map<Long, Map<String, Double>> payroll = new HashMap<>();

        for (Employee emp : Employees){
            Long key = emp.getId();
            Map<String, Double> itemsPayroll = new HashMap<>();

            for (CalculatingConcept concept : establishedConcepts){
                String formula = concept.getValue();
                if (concept.getPriority() == 1) formula = formula.replaceAll("SALARIO", String.valueOf(emp.getSalary()));
                else {
                    for (String conceptKey : itemsPayroll.keySet()){
                        if (formula.contains(conceptKey)) formula = formula.replaceAll(conceptKey, itemsPayroll.get(conceptKey).toString());
                    }
                }
                Double result = calculateItemsPayroll(formula);
                itemsPayroll.put(concept.getConceptName(), result);
            }

            payroll.put(key, itemsPayroll);
        }

        return payroll;
    }

    private Double calculateItemsPayroll(String formula) {
        if (!formula.contains("if(")){
            return evaluator.evaluate(formula);
        }

        boolean isCondition = false;
        double resultCondition = 0.0;

        int blockNumber = 0;
        int ifsNumber = 0;

        List<String> operationTokens = new ArrayList<>();
        List<String> conditionTokens = new ArrayList<>();
        List<String> tokens = new ArrayList<>(Arrays.stream(formula.split(" ")).toList());

        for (String token : tokens ) {
            if (token.equals(";")) {
                if (ifsNumber > 0){
                    ifsNumber--;
                    conditionTokens.add(token);
                } else {
                    String subFunction = convertListToString(conditionTokens);
                    operationTokens.add(calculateItemsPayroll(subFunction).toString());
                    conditionTokens.clear();
                    resultCondition = 0.0;
                }
            } else if (resultCondition == 1.0){
                if (!token.equals(":") && isCondition) {
                    conditionTokens.add(token);
                    if (token.equals("if(")) ifsNumber++;
                }
                else if (token.equals(":")) {
                    if (ifsNumber > 0) conditionTokens.add(token);
                    else isCondition = false;
                }
            } else if (resultCondition == 2.0) {
                if (!token.equals(":") && !isCondition) conditionTokens.add(token);
                else if (token.equals(":")) isCondition = false;
            } else if (token.equals("if(")){
                isCondition = true;
            } else if (token.equals("(") && isCondition) {
                blockNumber++;
                conditionTokens.add(token);
            } else if (isCondition && blockNumber > 0 && token.equals(")")) {
                blockNumber--;
                conditionTokens.add(token);
            } else if (isCondition && blockNumber == 0 && token.equals(")")) {
                resultCondition = evaluateCondition(conditionTokens);
                conditionTokens.clear();
            } else if (isCondition) {
                conditionTokens.add(token);
            } else operationTokens.add(token);
        }

        String subFunction = convertListToString(operationTokens);
        return evaluator.evaluate(subFunction);
    }

    private Double evaluateCondition(List<String> conditionTokens){
        List<String> listAux = new ArrayList<>();
        String operator = "";

        while (!conditionTokens.isEmpty()){
            String token = conditionTokens.remove(0);
            if (!token.equals("<") && !token.equals(">")) listAux.add(token);
            else {
                operator = token;
                break;
            }
        }

        String condition1 = convertListToString(listAux);
        String condition2 = convertListToString(conditionTokens);

        Double number1 = evaluator.evaluate(condition1);
        Double number2 = evaluator.evaluate(condition2);

        return evaluateCondition(number1, number2, operator);
    }

    private Double evaluateCondition(double op1, double op2, String operator){
        if (operator.equals("<")) {
            return op1 < op2 ? 1.0 : 2.0;
        }
        return op1 > op2 ? 1.0 : 2.0;
    }

    private String convertListToString(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(" "); // Agregar un espacio entre elementos, excepto para el último elemento
            }
        }
        return sb.toString();
    }


}
