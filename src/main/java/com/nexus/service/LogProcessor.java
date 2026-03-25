package com.nexus.service;

import com.nexus.exception.NexusValidationException;
import com.nexus.model.Project;
import com.nexus.model.Task;
import com.nexus.model.TaskStatus;
import com.nexus.model.User;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Processador de logs para o sistema Nexus.
 * <p>Lê arquivos de log contendo comandos estruturados e executa operações
 * no workspace. Suporta ações como criação de usuários, projetos, tarefas,
 * atribuição de usuários e mudanças de status.
 */
public class LogProcessor {

    /**
     * Processa um arquivo de log e executa todos os comandos no workspace.
     * <p>O arquivo deve estar no classpath. Linhas em branco ou começando com '#'
     * são ignoradas.
     *
     * @param fileName o nome do arquivo de log no classpath
     * @param workspace o workspace onde as operações serão aplicadas
     */
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

    /**
     * Processa uma linha individual de comando do log.
     *
     * @param workspace o workspace alvo
     * @param line a linha do comando a processar
     */
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
        } catch (NullPointerException e) {
            System.err.println(String.format(
                    "[ERRO DE INPUT] Tarefa não existe no comando '%s': %s", line, e.getMessage()));
        }
    }

    /**
     * Cria um novo usuário e o adiciona ao workspace.
     *
     * @param workspace o workspace alvo
     * @param p array de parâmetros: [CREATE_USER, username, email]
     */
    private void createUser(Workspace workspace, String[] p) {
        if (workspace.findUserByName(p[1]) != null) {
            throw new IllegalArgumentException("Já existe outro usuário com esse nome.");
        }
        workspace.addUser(new User(p[1], p[2]));
        System.out.println("[LOG] Usuário criado: " + p[1]);
    }

    /**
     * Cria um novo projeto e o adiciona ao workspace.
     *
     * @param workspace o workspace alvo
     * @param p array de parâmetros: [CREATE_PROJECT, projectName, budgetHours]
     */
    private void createProject(Workspace workspace, String[] p) {
        Project project = new Project(p[1], Integer.parseInt(p[2]));
        workspace.addProject(project);
        System.out.println("[LOG] Projeto criado: " + p[1]);
    }

    /**
     * Cria uma nova tarefa e a adiciona ao workspace e seu projeto associado.
     *
     * @param workspace o workspace alvo
     * @param p array de parâmetros: [CREATE_TASK, title, deadline, estimatedEffort, projectName]
     */
    private void createTask(Workspace workspace, String[] p) {
        Task t = new Task(p[1], LocalDate.parse(p[2]), Integer.parseInt(p[3]), p[4]);
        workspace.addTask(t);
        Project project = workspace.findProjectByName(p[4]);
        if (project != null) {
            project.addTask(t);
        }

        System.out.println("[LOG] Tarefa criada: " + p[1]);
    }

    /**
     * Atribui um usuário a uma tarefa existente.
     *
     * @param workspace o workspace alvo
     * @param p array de parâmetros: [ASSIGN_USER, taskId, username]
     */
    private void assignUser(Workspace workspace, String[] p) {
        Task task = workspace.findTaskById(Integer.parseInt(p[1]));
        task.assignUser(workspace.findUserByName(p[2]));
        System.out.println("[LOG] Tarefa atribuída a usuário");
    }

    /**
     * Altera o status de uma tarefa existente.
     *
     * @param workspace o workspace alvo
     * @param p array de parâmetros: [CHANGE_STATUS, taskId, newStatus]
     */
    private void changeStatus(Workspace workspace, String[] p) {
        Task task = workspace.findTaskById(Integer.parseInt(p[1]));
        if (task == null) {
            Task.throwNexusError("Tarefa não existe.");
        }
        task.changeStatus(TaskStatus.valueOf(p[2]));
        System.out.println("[LOG] Status alterado com sucesso");
    }

    /**
     * Gera um relatório de status com análises do workspace.
     *
     * @param workspace o workspace alvo
     * @param p array de parâmetros: [REPORT_STATUS]
     */
    private void reportStatus(Workspace workspace, String[] p) {
        System.out.println("[LOG] Relatórios analíticos:");
        workspace.printTopPerformers();
        workspace.printOverloadedUsers();
        workspace.printProjectHealth();
        workspace.printGlobalBottleneck();
    }
}
