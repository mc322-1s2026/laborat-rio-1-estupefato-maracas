package com.nexus.service;

import com.nexus.model.*;
import com.nexus.exception.NexusValidationException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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

                    processLine(workspace, line);
                }
            }
        } catch (IOException e) {
            System.err.println("[ERRO FATAL] " + e.getMessage());
        }
    }

    private void processLine(Workspace workspace, String line) {
        String[] p = line.split(";");
        String action = p[0];

        try {
            switch (action) {
                case "CREATE_USER" -> createUser(workspace, p);
                case "CREATE_PROJECT" -> createProject(workspace, p);
                case "CREATE_TASK" -> createTask(workspace, p);
                case "ASSIGN_USER" -> assignUser(workspace, p);
                case "CHANGE_STATUS" -> changeStatus(workspace, p);
                case "REPORT_STATUS" -> reportStatus(workspace, p);
                default -> System.err.println("[WARN] Ação desconhecida: " + action);
            }
        } catch (NexusValidationException e) {
            System.err.println(String.format(
                    "[ERRO DE REGRAS] Falha no comando '%s': %s", line, e.getMessage()));
        } catch (IllegalArgumentException | DateTimeParseException e) {
            System.err.println(String.format(
                    "[ERRO DE INPUT] Input incorreto no comando '%s': %s", line, e.getMessage()));
        }
    }

    private void createUser(Workspace workspace, String[] p) {
        workspace.addUser(new User(p[1], p[2]));
        System.out.println("[LOG] Usuário criado: " + p[1]);
    }

    private void createProject(Workspace workspace, String[] p) {
        Project project = new Project(p[1], Integer.parseInt(p[2]));
        workspace.addProject(project);
        System.out.println("[LOG] Projeto criado: " + p[1]);
    }

    private void createTask(Workspace workspace, String[] p) {
        Task t = new Task(p[1], LocalDate.parse(p[2]), Integer.parseInt(p[3]), p[4]);
        workspace.addTask(t);
        Project project = workspace.findProjectByName(p[4]);
        if (project != null) {
            project.addTask(t);
        }

        System.out.println("[LOG] Tarefa criada: " + p[1]);
    }

    private void assignUser(Workspace workspace, String[] p) {
        Task task = workspace.findTaskById(Integer.parseInt(p[1]));
        task.assignUser(workspace.findUserByName(p[2]));
        System.out.println("[LOG] Tarefa atribuída a usuário");
    }

    private void changeStatus(Workspace workspace, String[] p) {
        Task task = workspace.findTaskById(Integer.parseInt(p[1]));
        task.changeStatus(TaskStatus.valueOf(p[2]));
        System.out.println("[LOG] Status alterado com sucesso");
    }

    private void reportStatus(Workspace workspace, String[] p) {
        System.out.println("[LOG] Relatórios analíticos:");
        System.out.print("    - Top 3 Performers: | ");
        int i = 1;
        for (User user: workspace.topPerformers(3)) {
            System.out.printf("#%d %s | ", i++, user.getUsername());
        }
        System.out.println();
        System.out.println("    - Overloaded Users: " + workspace.overloadedUsers());
        System.out.println("    - Project Health: ");
        workspace.getProjects().stream()
                .forEach(proj -> System.out.printf(
                        "        - %s: %d%%\n",
                        proj.getName(),
                        Math.round(workspace.projectHealth(proj) * 100)
                    ));
        System.out.println("    - Global Bottleneck: " + workspace.globalBottleneck());
    }
}
