package Main;

import java.io.*;
import java.util.*;

public class MainClass {
	
	// Processes Queue And Memory And ID!
	static Queue<Map.Entry<Integer, Map.Entry<List<String>, Map<String, Object>>>> Q = new LinkedList<>();
    static Scanner sc = new Scanner(System.in);
    static int programId = 1;

    // Program Entering
    public static void enterPrograms(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader("src/Main/" + fileName))) {
            String line;
            List<String> program = new ArrayList<>();
            Map<String, Object> programMemory = new HashMap<>();
            while ((line = br.readLine()) != null) {
                program.add(line);		//Adding Text From txt To A Program
            }
            Q.add(new AbstractMap.SimpleEntry<>(programId++, new AbstractMap.SimpleEntry<>(program, programMemory))); // Add the program and its memory to the queue
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void assign(String var, String value, Map<String, Object> programMemory) {
        if (value.equals("input")) {
            System.out.println("Enter value for " + var + ": ");
            String input = sc.nextLine();
            try {
                double num = Double.parseDouble(input);
                programMemory.put(var, num);
            } catch (NumberFormatException e) {
                programMemory.put(var, input);
            }
        } else {
            try {
                double num = Double.parseDouble(value);
                programMemory.put(var, num);
            } catch (NumberFormatException e) {
                programMemory.put(var, value);
            }
        }
    }

    public static void arithmetic(String operation, String var, String op1, String op2, Map<String, Object> programMemory) {
        if (programMemory.get(op1) instanceof String || programMemory.get(op2) instanceof String) {
            System.out.println("Cannot perform arithmetic operation on a string. Please enter a new instruction.");
            return;
        }
        double result = 0;
        switch (operation) {
            case "add":
                result = (double)programMemory.get(op1) + (double)programMemory.get(op2);
                break;
            case "subtract":
                result = (double)programMemory.get(op1) - (double)programMemory.get(op2);
                break;
            case "multiply":
                result = (double)programMemory.get(op1) * (double)programMemory.get(op2);
                break;
            case "divide":
                result = (double)programMemory.get(op1) / (double)programMemory.get(op2);
                break;
        }
        programMemory.put(var, result);
    }

    public static void writeFile(String varFilename, String varContent, Map<String, Object> programMemory) {
        String filename = programMemory.get(varFilename).toString();
        String content = programMemory.get(varContent).toString();
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readFile(String var, String varFilename, Map<String, Object> programMemory, List<String> program, int programId) {
        String filename = programMemory.get(varFilename).toString();
        File file = new File(filename);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                programMemory.put(var, reader.readLine());
                programMemory.put("waitingForFile", false);  // Reset the flag
            } catch (IOException e) {
                programMemory.put(var, "Error reading file");
                e.printStackTrace();
            }
        } else {
            System.out.println("File " + filename + " not found. Program will retry later.");
            if (Q.isEmpty() || Q.size() == 2 && Q.peek().getKey().equals(programId)) {
                System.out.println("No other File Creation programs in the queue. Stopping execution.");
                return;
            }
            program.add(0, "assign " + var + " readFile " + varFilename);
            programMemory.put("waitingForFile", true);  // Set the flag
            Q.add(new AbstractMap.SimpleEntry<>(programId, new AbstractMap.SimpleEntry<>(program, programMemory)));
        }
    }
    
    public static void print(String var, Map<String, Object> programMemory) {
        System.out.println(var + " = " + programMemory.get(var));
    }
    
    public static void executeInstruction(String instruction, Map<String, Object> programMemory, List<String> program) {
        String[] parts = instruction.split(" ");
        String command = parts[0];

        switch (command) {
            case "assign":
                if (parts[2].matches("add|subtract|multiply|divide")) {
                    arithmetic(parts[2], parts[1], parts[3], parts[4], programMemory);
                } else if (parts[2].equals("readFile")) {
                	readFile(parts[1], parts[3], programMemory, program, programId);
                } else {
                    assign(parts[1], parts[2], programMemory);
                }
                break;
            case "print":
                print(parts[1], programMemory);
                break;
            case "add":
            case "subtract":
            case "multiply":
            case "divide":
                arithmetic(command, parts[1], parts[2], parts[3], programMemory);
                break;
            case "writeFile":
                writeFile(parts[1], parts[2], programMemory);
                break;
            default:
                System.out.println("Unknown command: " + command);
                break;
        }
    }
    
    public static void roundRobin(int X) {
        StringBuilder ganttChart = new StringBuilder("Gantt Chart: ");
        int clockCycle = 1;
        while (!Q.isEmpty()) {
            System.out.println("Ready queue: " + Q);
            
            Map.Entry<Integer, Map.Entry<List<String>, Map<String, Object>>> entry = Q.poll();
            int programId = entry.getKey();
            List<String> program = entry.getValue().getKey();
            Map<String, Object> programMemory = entry.getValue().getValue();
            
            System.out.println("Starting program " + programId);
            
            for (int i = 0; i < X && !program.isEmpty(); i++) {
                String instruction = program.remove(0);
                System.out.println("Executing instruction: " + instruction);
                executeInstruction(instruction, programMemory, program);
                System.out.println("Memory state: " + programMemory);
                ganttChart.append("[Clock cycle ").append(clockCycle++).append(" Program_").append(programId).append("], ");
            }
            if (!program.isEmpty() && !Boolean.TRUE.equals(programMemory.get("waitingForFile"))) {
                Q.add(entry);
            } else {
                System.out.println("Finished executing program " + programId);
            }
        }
        System.out.println(ganttChart.toString());
    }

    public static void shortestJobFirst() {
        PriorityQueue<Map.Entry<Integer, Map.Entry<List<String>, Map<String, Object>>>> pq = new PriorityQueue<>(
            Comparator.comparingInt(e -> e.getValue().getKey().size())
        );
        pq.addAll(Q);
        Q.clear();
        
        StringBuilder ganttChart = new StringBuilder("Gantt Chart: ");
        int clockCycle = 1;
        while (!pq.isEmpty()) {
            System.out.println("Ready queue: " + pq);
            Map.Entry<Integer, Map.Entry<List<String>, Map<String, Object>>> entry = pq.poll();
            int programId = entry.getKey();
            List<String> program = entry.getValue().getKey();
            Map<String, Object> programMemory = entry.getValue().getValue();
            System.out.println("Starting program " + programId);
           
            while (!program.isEmpty()) {
                String instruction = program.remove(0);
                System.out.println("Executing instruction: " + instruction);
                executeInstruction(instruction, programMemory, program);
                System.out.println("Memory state: " + programMemory);
                ganttChart.append("[Clock cycle ").append(clockCycle++).append(" Program_").append(programId).append("], ");
            }
            System.out.println("Finished executing program " + programId);
        }
        System.out.println(ganttChart.toString());
    }

    public static void executePrograms() {
        while (!Q.isEmpty()) {
            Map.Entry<Integer, Map.Entry<List<String>, Map<String, Object>>> entry = Q.poll();
            int programId = entry.getKey();
            List<String> program = entry.getValue().getKey();
            Map<String, Object> programMemory = entry.getValue().getValue();
            System.out.println("Starting program " + programId);
            while (!program.isEmpty()) {
                String instruction = program.remove(0);
                System.out.println("Executing instruction: " + instruction);
                executeInstruction(instruction, programMemory, program);
            }
            System.out.println("Finished executing program " + programId);
        }
    }

    public static void main(String[] args) {
        enterPrograms("Program_1.txt");
        enterPrograms("Program_2.txt");
        enterPrograms("Program_3.txt");
        System.out.println("Please Enter The Execution Style You Want To See 'R' (RoundRobin) 'E' (Normal Excution) 'S' (Shortest Job First)");
        char c = sc.next().charAt(0);
        while (!Character.isLetter(c)) {
            System.out.println("Invalid input. Please enter a character: ");
            c = sc.next().charAt(0);
        }
        if(c == 'R' || c == 'r') {
            System.out.println("What Would you like the time-slice for (RoundRobin) to be?");
            while (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter an integer: ");
                sc.next();
            }
            int x = sc.nextInt();
            sc.nextLine();
            roundRobin(x);
        } else if (c == 'E' || c == 'e') {
        	sc.nextLine();
            executePrograms();
        } else if (c == 's' || c == 'S') {
        	sc.nextLine();
            shortestJobFirst();
        }
    }
}
