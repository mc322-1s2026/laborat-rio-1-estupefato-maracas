package com.nexus.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Project {
    private String name;
    private List<Task> tasks;
    private int totalBudget;

    public Project(String name, int totalBudget) {
        this.name = name.trim();
        this.totalBudget = totalBudget;
        this.tasks = new ArrayList<>();
    }

    public void addTask(Task t) {
        int currentEffort = estimateProjectEffort();
        
        if (currentEffort + t.getEstimatedEffort() > totalBudget) {
            Task.throwNexusError("Tarefa excederia o orçamento do projeto, "
                    + "não pode ser adicionada. \nOrçamento utilizado/total: "
                    + String.format("(%d/%d)", currentEffort, totalBudget));
        }
        
        tasks.add(t);
    }

    public int estimateProjectEffort() {
        return getTasks().stream()
            .reduce(0, (total, task) -> total + task.getEstimatedEffort(), Integer::sum);
    }

    public String getName() {
        return name;
    }

    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    public int getTotalBudget() {
        return totalBudget;
    }

    public int getTotalTasks() {
        if (tasks == null) {return 0; }
        return (int) tasks.size();
    }
}
