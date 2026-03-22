package com.nexus.service;

import com.nexus.model.Task;
import com.nexus.model.User;

import com.nexus.model.TaskStatus;
import com.nexus.model.Project;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Collections;

public class Workspace {
    private List<Task> tasks = new ArrayList<>();
    private List<User> users = new ArrayList<>();
    private List<Project> projects = new ArrayList<>();

    public void addTask(Task task) {
        tasks.add(task);
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void addProject(Project project) {
        projects.add(project);
    }

    public Task findTaskById(int id) {
        return tasks.stream()
                    .filter(t -> t.getId() == id)
                    .findFirst()
                    .orElse(null);
    }

    public User findUserByName(String name) {
        return users.stream()  
                    .filter(u -> u.getUsername().equals(name))
                    .findFirst()
                    .orElse(null);
    }

    public Project findProjectByName(String projectName) {
        return projects.stream()
                       .filter(p -> p.getName().equals(projectName))
                       .findFirst()
                       .orElse(null);
    }

    /**
     * Retorna uma lista com os {@link User} que possuem a maior
     * quantidade de {@link Task} finalizadas.
     * Utiliza a Stream API para filtrar as tarefas no estado {@link TaskStatus.DONE}, 
     * agrupá-las por dono e ordenar os resultados.
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
     */
    public List<User> overloadedUsers() {
        return users.stream()
                    .filter(u -> u.getWorkload(tasks) > 10)
                    .toList();
    }

    public void printOverloadedUsers() {
        System.out.print("    - Overloaded Users: ");
        List<User> overloaded = overloadedUsers();
        System.out.println(overloaded.isEmpty() ? "Nenhum" : overloaded);
    }

    /**
     * Calcula a saúde de um {@link Project}, representada pela proporção
     * de tarefas concluídas em relação ao total de tarefas atribuídas àquele projeto.
     * Retorna 0 caso o projeto não tenha tarefas.
     */
    public double projectHealth(Project project) {
        if (project.getTotalTasks() == 0) {
            return 0;
        }
        return (double) project.getTasks().stream()
                      .filter(t -> t.getStatus() == TaskStatus.DONE)
                      .count() / project.getTasks().size();
    }

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
     *  que é definido como o {@link TaskStatus}, excluindo os já concluídos, 
     * e acumulando a maior quantidade de {@link Task} paradas no momento.
     */
    public TaskStatus globalBottleneck() {
        return tasks.stream()
                    .filter(t -> t.getStatus() != TaskStatus.DONE)
                    .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()))
                    .entrySet().stream()
                    .max(Entry.comparingByValue())
                    .map(Entry::getKey)
                    .orElse(null);
    }

    public void printGlobalBottleneck() {
        System.out.println("    - Global Bottleneck: " + 
            (globalBottleneck() != null ? globalBottleneck() : "Nenhum"));
    }

    public long countDoneTasksForUser(User u) {
        return tasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.DONE
                            && t.getOwner() != null && t.getOwner().equals(u))
                    .count();
    }

    public List<Task> getTasks() {return Collections.unmodifiableList(tasks); }
    public List<User> getUsers() {return Collections.unmodifiableList(users); }
    public List<Project> getProjects() {return Collections.unmodifiableList(projects); }
}