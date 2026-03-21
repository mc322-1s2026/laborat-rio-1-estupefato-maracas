package com.nexus.service;

import com.nexus.model.Task;
import com.nexus.model.User;

import com.nexus.model.TaskStatus;
import com.nexus.model.Project;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    public long countDoneTasksForUser(User u) {
        return tasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.DONE
                            && t.getOwner() != null && t.getOwner().equals(u))
                    .count();
    }

    public List<User> topPerformers(int n) {
        return tasks.stream()
            .filter(t -> t.getStatus() == TaskStatus.DONE)
            .map(t -> t.getOwner())
            .collect(Collectors.groupingBy(user -> user, Collectors.counting()))
            .entrySet().stream()
            .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
            .limit(n)
            .map(p -> p.getKey())
            .toList();
    }

    public List<Task> getTasks() {return Collections.unmodifiableList(tasks); }
    public List<User> getUsers() {return Collections.unmodifiableList(users); }
    public List<Project> getProjects() {return Collections.unmodifiableList(projects); }
}