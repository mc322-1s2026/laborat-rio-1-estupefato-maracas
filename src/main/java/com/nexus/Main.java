package com.nexus;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import java.util.List;

import com.nexus.exception.NexusValidationException;
import com.nexus.model.Project;
import com.nexus.model.Task;
import com.nexus.model.TaskStatus;
import com.nexus.model.User;
import com.nexus.service.LogProcessor;
import com.nexus.service.Workspace;

/**
 * Ponto de entrada para a aplicação Nexus.
 * *
 * Esta classe fornece uma interface simples baseada em console usada no
 * trabalho de laboratório. Gerencia uma coleção de {@link User usuários}
 * e um {@link Workspace} onde as {@link Task tarefas} são armazenadas. As
 * operações são realizadas através de um laço de menu e delegadas a métodos
 * auxiliares.
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Workspace workspace = new Workspace();
    private static final LogProcessor logProcessor = new LogProcessor();

    /**
     * Inicia a aplicação e processa comandos do usuário até a terminação.
     *
     * @param args argumentos de linha de comando (ignorados)
     */
    public static void main(String[] args) {
        boolean running = true;

        while (running) {
            displayMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "0" -> {
                    System.out.println("Encerrando Nexus Motor...");
                    running = false;
                }
                case "1" -> addUser();
                case "2" -> addProject();
                case "3" -> addTask();
                case "4" -> assignUser();
                case "5" -> changeStatus();
                case "6" -> reportStatus();
                case "7" -> listTasks();
                case "8" -> {
                    System.out.println("1. Carregar Log V1 (Básico)\n2. Carregar Log V2 (Desafio)");
                    String logChoice = scanner.nextLine();
                    String file = (logChoice.equals("1")) ? "log_v1.txt" : "log_v2.txt";
                    logProcessor.processLog(file, workspace);
                }
                default -> System.out.println("\n[!] Opção inválida.");
            }
        }
    }

    /**
     * Imprime o menu principal na saída padrão.
     * <p>As opções do menu correspondem às escolhas tratadas em
     * {@link #main(String[])}.
     * </p>
     */
    private static void displayMenu() {
        System.out.print("""
            
            ======= NEXUS CORE: MENU =======
            1. Adicionar Usuário
            2. Adicionar Projeto
            3. Adicionar Tarefa
            4. Atribuir Usuário à Tarefa
            5. Mudar Estado
            6. Reportar Estado
            7. Listar Todas as Tarefas
            8. Processar Log de Ações
            0. Sair
            Escolha uma opção:\s""");
    }

    /**
     * Solicita ao usuário nome de usuário e email, cria um novo {@link User} e
     * adiciona-o à lista interna. Exceções de validação são relatadas no
     * fluxo de erro.
     */
    private static void addUser() {
        try {
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine();

            User newUser = new User(username, email);
            workspace.addUser(newUser);
            System.out.println("[OK] Usuário cadastrado.");
        } catch (IllegalArgumentException e) {
            System.err.println("[ERRO DE ENTRADA] " + e.getMessage());
        }
    }

    /**
     * Cria um novo {@link Project} com o nome e orçamento (em horas) fornecidos
     * pelo usuário, o adicionando ao backlog de projetos.
     * O nome de cada projeto deve ser único.
     * Erros são relatados no stderr.
     */
    private static void addProject() {
        System.out.print("Nome do projeto: ");
        String projectName = scanner.nextLine().strip();
        
        if (workspace.findProjectByName(projectName) != null) {
            System.err.println("[ERRO DE ENTRADA] Já existe outro projeto com esse nome.");
            return;
        }

        System.out.print("Orçamento (horas): ");
        int budgetHours;
        try {
            budgetHours = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.err.println("[ERRO DE ENTRADA] Parâmetro de horas inválido, digite um número inteiro.");
            return;
        }

        Project project = new Project(projectName, budgetHours);
        workspace.addProject(project);

        System.out.println("[OK] Projeto criado.");
    }

    /**
     * Coleta detalhes da tarefa do usuário, constrói uma {@link Task} e
     * a acrescenta ao workspace. Erros de parsing de data são informados no
     * stderr.
     */
    private static void addTask() {
        System.out.print("Título da Tarefa: ");
        String title = scanner.nextLine();
        
        System.out.print("Prazo (AAAA-MM-DD): ");
        LocalDate deadline;
        try {
            deadline = LocalDate.parse(scanner.nextLine());
        } catch (DateTimeParseException e) {
            System.err.println("[ERRO DE ENTRADA] Formato de data inválido. Use AAAA-MM-DD.");
            return;
        }
        
        System.out.print("Esforço Estimado (Horas): ");
        int effort;
        try {
            effort = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.err.println("[ERRO DE ENTRADA] Parâmetro de horas inválido, digite um número inteiro.");
            return;
        }
        
        System.out.print("Nome do Projeto: ");
        String projectName = scanner.nextLine().strip();
        
        Project project = workspace.findProjectByName(projectName);
        if (project == null) {
            System.err.println("[ERRO DE ENTRADA] Não existe um projeto com esse nome.");
            return;
        }

        Task newTask = new Task(title, deadline, effort, projectName);
        
        try {
            project.addTask(newTask);
            
            workspace.addTask(newTask);
            System.out.println("[OK] Tarefa adicionada ao backlog.");
            
        } catch (NexusValidationException e) {
            System.err.println("[ERRO DE REGRAS] " + e.getMessage());
        }
    }

    /**
     * Solicita ao usuário o ID de uma tarefa e o nome de um {@link User},
     * realizando a atribuição (owner) da tarefa caso ambos existam.
     * Erros de formatação de ID ou violações de regras de negócio 
     * são relatados no fluxo de erro stderr.
     */
    public static void assignUser() {
        System.out.println("Id da Tarefa: ");
        String id = scanner.nextLine();
        Task task = null;
        try {
            task = workspace.findTaskById(Integer.parseInt(id));
            if (task == null) {
                System.err.println("Tarefa não existe.");
                return;
            }
        } catch (NumberFormatException e) {
            System.err.println("[ERRO DE ENTRADA] Parâmetro inválido, digite número inteiro.");
            return;
        }

        System.out.println("Nome do usuário: ");
        String username = scanner.nextLine();

        User user = workspace.findUserByName(username);
        if (user == null) {
            System.err.println("Usuário não existe.");
            return;
        }

        try {
            task.assignUser(user);
            System.out.println("[OK] Usuário atribuído com sucesso.");
        } catch (NexusValidationException e) {
            System.err.println("[ERRO DE REGRAS] " + e.getMessage());
        }
    }

    /**
     * Solicita o ID de uma tarefa e o novo estado desejado,
     * acionando a máquina de estados para realizar a transição na {@link Task}.
     * Erros de digitação de status ou violações das regras de transição
     * são relatados no fluxo de erro stderr.
     */
    public static void changeStatus() {
        System.out.println("Id da Tarefa: ");
        String id = scanner.nextLine();
        Task task = null;
        try {
            task = workspace.findTaskById(Integer.parseInt(id));
            if (task == null) {
                System.err.println("Tarefa não existe.");
                return;
            }
        } catch (NumberFormatException e) {
            System.err.println("[ERRO DE ENTRADA] Parâmetro inválido, digite número inteiro.");
            return;
        }

        System.out.println("Novo Estado: ");
        String statusStr = scanner.nextLine();
        
        try {
            TaskStatus novoStatus = TaskStatus.valueOf(statusStr.toUpperCase());
            
            task.changeStatus(novoStatus);
            
            System.out.println("[OK] Status alterado com sucesso para " + novoStatus);
            
        } catch (IllegalArgumentException e) {
            System.err.println("[ERRO DE ENTRADA] Status '" + statusStr + "' não existe. Use: TO_DO, IN_PROGRESS, BLOCKED ou DONE.");
            
        } catch (NexusValidationException e) {
            System.err.println("[ERRO DE REGRAS] " + e.getMessage());
        }
    }

    /**
     * Exibe no console um painel analítico com métricas globais do sistema.
     * Invoca os relatórios do {@link Workspace} para listar top performers,
     * usuários sobrecarregados, gargalos globais e a saúde atual dos projetos.
     */
    public static void reportStatus() {
        System.out.println("\n====== NEXUS GLOBAL REPORT ======");
        System.out.println("Total de Tarefas Criadas: " + Task.totalTasksCreated);
        System.out.println("Total de Erros de Regra: " + Task.totalValidationErrors);
        System.out.println("\n[LOG] Relatórios analíticos:");
        
        System.out.print("    - Top 3 Performers: | ");
        int i = 1;
        for (User user: workspace.topPerformers(3)) {
            System.out.printf("#%d %s | ", i++, user.getUsername());
        }
        System.out.println();
        
        System.out.print("    - Overloaded Users: ");
        List<User> overloaded = workspace.overloadedUsers();
        if (overloaded.isEmpty()) {
            System.out.println("Nenhum");
        } else {
            List<String> nomesSobrecarregados = overloaded.stream()
                                                          .map(User::getUsername)
                                                          .toList();
            System.out.println(nomesSobrecarregados);
        }
        
        System.out.println("    - Project Health: ");
        workspace.getProjects().forEach(proj -> 
            System.out.printf("        - %s: %d%%\n", proj.getName(), Math.round(workspace.projectHealth(proj) * 100))
        );
        
        System.out.println("    - Global Bottleneck: " + 
            (workspace.globalBottleneck() != null ? workspace.globalBottleneck() : "Nenhum"));
    }

    /**
     * Exibe todas as tarefas atualmente armazenadas no {@link Workspace} em
     * formato de tabela simples. Se não existirem tarefas, imprime uma mensagem
     * de notificação.
     */
    private static void listTasks() {
        List<Task> tasks = workspace.getTasks();
        if (tasks.isEmpty()) {
            System.out.println("\n[!] Nenhuma tarefa no sistema.");
            return;
        }

        String header = "+----+----------------------+-------------+------------+------------+------------+";
        System.out.println("\n" + header);
        System.out.printf("| %-2s | %-20s | %-11s | %-10s | %-10s | %-10s |%n", "ID", "TÍTULO", "STATUS", "DEADLINE", "EFFORT", "PROJECT");
        System.out.println(header);

        for (Task t : tasks) {
            System.out.printf("| %-2s | %-20s | %-11s | %-10s | %-10s | %-10s |%n",
                    t.getId(),
                    truncar(t.getTitle(), 20),
                    t.getStatus(),
                    t.getDeadline(),
                    t.getEstimatedEffort(),
                    t.getProjectName());
        }
        System.out.println(header);
        System.out.println("Total de tarefas: " + Task.totalTasksCreated);
    }

    /**
     * Trunca uma string para um comprimento máximo, acrescentando reticências
     * se ela for maior que o tamanho especificado.
     *
     * @param str a string a ser truncada (pode ser {@code null})
     * @param tam o comprimento máximo permitido
     * @return uma string possivelmente reduzida; nunca {@code null}
     */
    private static String truncar(String str, int tam) {
        if (str == null) return "";
        return str.length() > tam ? str.substring(0, tam - 3) + "..." : str;
    }
}