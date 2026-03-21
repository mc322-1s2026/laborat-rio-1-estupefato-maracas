package com.nexus.service;

import com.nexus.model.*;
import com.nexus.exception.NexusValidationException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class LogProcessor {

    public void processLog(String fileName, Workspace workspace) {
        try {
            // Busca o arquivo dentro da pasta de recursos do projeto (target/classes)
            var resource = getClass().getClassLoader().getResourceAsStream(fileName);
            
            if (resource == null) {
                throw new IOException("Arquivo não encontrado no classpath: " + fileName);
            }

            try (java.util.Scanner s = new java.util.Scanner(resource).useDelimiter("\\A")) {
                String content = s.hasNext() ? s.next() : "";
                List<String> lines = List.of(content.split("\\R"));
                
                for (String line : lines) {
                    if (line.isBlank() || line.startsWith("#")) continue;

                    String[] p = line.split(";");
                    String action = p[0];

                    try {
                        switch (action) {
                            case "CREATE_USER" -> {
                                workspace.addUser(new User(p[1], p[2]));
                                System.out.println("[LOG] Usuário criado: " + p[1]);
                            }
                            case "CREATE_TASK" -> {
                                Task t = new Task(p[1], LocalDate.parse(p[2]), Integer.parseInt(p[3]), p[4]);
                                workspace.addTask(t);
                                Project project = workspace.findProjectByName(p[4]);
                                if (project != null) {
                                    project.addTask(t);
                                }

                                System.out.println("[LOG] Tarefa criada: " + p[1]);
                            }
                            case "CREATE_PROJECT" -> {
                                Project project = new Project(p[1], Integer.parseInt(p[2]));
                                workspace.addProject(project);
                                System.out.println("[LOG] Projeto criado: " + p[1]);
                            }
                            case "ASSIGN_USER" -> {
                                Task task = workspace.findTaskById(Integer.parseInt(p[1]));
                                task.assignUser(workspace.findUserByName(p[2]));
                                System.out.println("[LOG] Tarefa atribuída a usuário");
                            }
                            case "CHANGE_STATUS" -> {
                                Task task = workspace.findTaskById(Integer.parseInt(p[1]));
                                task.changeStatus(TaskStatus.valueOf(p[2]));
                                System.out.println("[LOG] Status alterado com sucesso");
                            }
                            case "REPORT_STATUS" -> {
                                // continue when stream api at workspace when is done
                            }
                            default -> System.err.println("[WARN] Ação desconhecida: " + action);
                        }
                    } catch (NexusValidationException e) {
                        System.err.println("[ERRO DE REGRAS] Falha no comando '" + line + "': " + e.getMessage());
                    } catch (IllegalArgumentException e) {
                        System.err.println("[ERRO DE INPUT] Input incorreto no comando '" + line + "': " +  e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[ERRO FATAL] " + e.getMessage());
        }
    }
}