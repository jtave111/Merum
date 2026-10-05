package com.manager.server.service.networkSession.fingerprint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkAutomationFingerprintService {

    private final Map<String, Process> activeProcesses = new ConcurrentHashMap<>();


    public Map<String, Process> getActiveProcesses(){

        return this.activeProcesses;
    }

    //TODO: abstrair metodo
    public File getRootPath(){
        File currentDir = new File(System.getProperty("user.dir"));


        return  currentDir.equals("server")? currentDir.getParentFile(): null;

    }

    public String execRequestAutomation(String scriptName, String JSESSIONID, String URL ){

        if (activeProcesses.containsKey(scriptName)) {

            return "ALREADY_RUNNING";
        }

        List<String> command = new ArrayList<>();

        StringBuilder output = new StringBuilder();


        try {

            File root = getRootPath();
            File scriptFile = new File(root, "modules/python/localFingerPrint/requestAutomation/" + scriptName);

            if(!scriptFile.exists()) throw new FileNotFoundException();

            command.add("python3");
            command.add(scriptFile.getAbsolutePath());
            command.add(JSESSIONID);
            command.add(URL);


            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            activeProcesses.put(scriptName, process);

            try(BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getInputStream()))){
                String line;

                while ((line = buffer.readLine()) != null) {

                    output.append(line).append("\n");
                    System.out.println(line);
                }
            }

            process.waitFor();
            activeProcesses.remove(scriptName);

        } catch (Exception e) {
            activeProcesses.remove(scriptName);
            e.printStackTrace();
            System.out.println("ERROR " + e.getMessage());

        }

        return output.toString();
    }
}
