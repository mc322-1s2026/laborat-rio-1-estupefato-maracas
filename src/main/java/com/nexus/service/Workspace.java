package com.nexus.service;

import com.nexus.model.Project;
import com.nexus.model.Task;
import com.nexus.model.TaskStatus;
import com.nexus.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Representa o espaço de trabalho central do sistema Nexus.
 * <p>Mantém listas de tarefas, usuários e projetos e fornece métodos para
 * gerenciamento, consulta e análise de métricas do sistema.
 */
public class Workspace {
    /**
     * Lista de todas as tarefas do workspace.
     */
    private List<Task> tasks = new ArrayList<>();
    /**
     * Lista de todos os usuários do workspace.
     */
    private List<User> users = new ArrayList<>();
    /**
     * Lista de todos os projetos do workspace.
     */
    private List<Project> projects = new ArrayList<>();

    /**
     * Adiciona uma tarefa ao workspace.
     *
     * @param task a tarefa a ser adicionada
     */
    public void addTask(Task task) {
        tasks.add(task);
    }

    /**
     * Adiciona um usuário ao workspace.
     *
     * @param user o usuário a ser adicionado
     */
    public void addUser(User user) {
        users.add(user);
    }

    /**
     * Adiciona um projeto ao workspace.
     *
     * @param project o projeto a ser adicionado
     */
    public void addProject(Project project) {
        projects.add(project);
    }

    /**
     * Procura uma tarefa pelo seu ID único.
     *
     * @param id o ID da tarefa
     * @return a tarefa encontrada, ou {@code null} se não existir
     */
    public Task findTaskById(int id) {
        return tasks.stream()
                    .filter(t -> t.getId() == id)
                    .findFirst()
                    .orElse(null);
    }

    /**
     * Procura um usuário pelo seu nome de usuário.
     *
     * @param name o nome de usuário
     * @return o usuário encontrado, ou {@code null} se não existir
     */
    public User findUserByName(String name) {
        return users.stream()  
                    .filter(u -> u.getUsername().equals(name))
                    .findFirst()
                    .orElse(null);
    }

    /**
     * Procura um projeto pelo seu nome único.
     *
     * @param projectName o nome do projeto
     * @return o projeto encontrado, ou {@code null} se não existir
     */
    public Project findProjectByName(String projectName) {
        return projects.stream()
                       .filter(p -> p.getName().equals(projectName))
                       .findFirst()
                       .orElse(null);
    }

    /**
     * Retorna uma lista com os {@link User} que possuem a maior
     * quantidade de {@link Task} finalizadas.
     * Utiliza a Stream API para filtrar as tarefas no estado {@link TaskStatus#DONE}, 
     * agrupá-las por dono e ordenar os resultados.
     *
     * @param n o número máximo de usuários a retornar
     * @return lista dos n principais usuários com mais tarefas concluídas
     */
    public List<User> topPerformers(int n) {
        return tasks.stream()
            .filter(t -> t.getStatus() == TaskStatus.DONE)
            .map(t -> t.getOwner())
            .filter(t -> t != null)
            .collect(Collectors.groupingBy(user -> user, Collectors.counting()))
            .entrySet().stream()
            .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
            .limit(n)
            .map(p -> p.getKey())
            .toList();
    }

    /**
     * Imprime na saída padrão os 3 principais usuários com mais tarefas concluídas.
     * Exibe "N/A" se não houver usuários com tarefas finalizadas.
     */
    public void printTopPerformers() {
        System.out.print("    - Top 3 Performers: ");
        List<User> topUsers = topPerformers(3);
        if (topUsers.isEmpty()) {
            System.out.println("N/A");
            return;
        }
        System.out.print("| ");
        int i = 1;
        for (User user: topPerformers(3)) {
            System.out.printf("#%d %s | ", i++, user.getUsername());
        }
        System.out.println();
    }

    /**
     * Identifica e retorna uma lista de {@link User} que estão sobrecarregados.
     * Um usuário sobrecarregado é aquele que tem carga de trabalho atual maior que 10.
     *
     * @return lista dos usuários com carga de trabalho acima do limite
     */
    public List<User> overloadedUsers() {
        return users.stream()
                    .filter(u -> u.calculateWorkload(tasks) > 10)
                    .toList();
    }

    /**
     * Imprime a lista de usuários sobrecarregados ou "Nenhum" se não houver.
     */
    public void printOverloadedUsers() {
        System.out.print("    - Overloaded Users: ");
        List<User> overloaded = overloadedUsers();
        System.out.println(overloaded.isEmpty() ? "Nenhum" : overloaded);
    }

    /**
     * Calcula a saúde de um {@link Project}, representada pela proporção
     * de tarefas concluídas em relação ao total de tarefas atribuídas àquele projeto.
     * Retorna 0 caso o projeto não tenha tarefas.
     *
     * @param project o projeto cuja saúde será calculada
     * @return valor entre 0 e 1 representando a proporção de tarefas concluídas
     */
    public double projectHealth(Project project) {
        if (project.getTotalTasks() == 0) {
            return 0;
        }
        return (double) project.getTasks().stream()
                      .filter(t -> t.getStatus() == TaskStatus.DONE)
                      .count() / project.getTasks().size();
    }

    /**
     * Imprime a saúde de todos os projetos em percentual.
     */
    public void printProjectHealth() {
        System.out.println("    - Project Health: ");
        getProjects().stream()
                .forEach(proj -> System.out.printf(
                        "        - %s: %d%%\n",
                        proj.getName(),
                        Math.round(projectHealth(proj) * 100)
                    ));
    }

    /**
     * Analisa o backlog do sistema para identificar o gargalo global,
     * que é definido como o {@link TaskStatus}, excluindo os já concluídos, 
     * e acumulando a maior quantidade de {@link Task} paradas no momento.
     *
     * @return o status que representa o gargalo global, ou {@code null} se não houver tarefas ativas
     */
    public TaskStatus globalBottleneck() {
        return tasks.stream()
                    .filter(t -> t.getStatus() != TaskStatus.DONE)
                    .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
    }

    /**
     * Imprime o gargalo global da entrega de tarefas.
     */
    public void printGlobalBottleneck() {
        System.out.println("    - Global Bottleneck: " + 
            (globalBottleneck() != null ? globalBottleneck() : "Nenhum"));
    }

    /**
     * Conta a quantidade de tarefas concluídas por um usuário específico.
     *
     * @param u o usuário
     * @return o número de tarefas concluídas
     */
    public long countDoneTasksForUser(User u) {
        return tasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.DONE
                            && t.getOwner() != null && t.getOwner().equals(u))
                    .count();
    }

    /**
     * Retorna uma lista imutável de todas as tarefas.
     *
     * @return lista de tarefas
     */
    public List<Task> getTasks() {return Collections.unmodifiableList(tasks); }
    /**
     * Retorna uma lista imutável de todos os usuários.
     *
     * @return lista de usuários
     */
    public List<User> getUsers() {return Collections.unmodifiableList(users); }
    /**
     * Retorna uma lista imutável de todos os projetos.
     *
     * @return lista de projetos
     */
    public List<Project> getProjects() {return Collections.unmodifiableList(projects); }
}