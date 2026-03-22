package com.nexus.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa um projeto no sistema Nexus.
 * <p>Um projeto contém um nome, um orçamento de horas e uma lista de tarefas.
 * Define restrições de orçamento: tarefas não podem ser adicionadas se excederem
 * o total de horas disponíveis.
 */
public class Project {
    /**
     * Nome único do projeto.
     */
    private String name;
    /**
     * Lista de tarefas pertencentes ao projeto.
     */
    private List<Task> tasks;
    /**
     * Orçamento total de horas disponíveis para o projeto.
     */
    private int totalBudget;

    /**
     * Constrói um novo projeto com o nome e orçamento em horas fornecidos.
     *
     * @param name o nome único do projeto
     * @param totalBudget o orçamento total do projeto em horas
     */
    public Project(String name, int totalBudget) {
        this.name = name.trim();
        this.totalBudget = totalBudget;
        this.tasks = new ArrayList<>();
    }

    /**
     * Adiciona uma tarefa ao projeto se ela se encaixar no orçamento.
     *
     * @param t a tarefa a ser adicionada
     * @throws NexusValidationException se a tarefa exceder o orçamento disponível
     */
    public void addTask(Task t) {
        int currentEffort = estimateProjectEffort();
        
        if (currentEffort + t.getEstimatedEffort() > totalBudget) {
            Task.throwNexusError("Tarefa excederia o orçamento do projeto, "
                    + "não pode ser adicionada. \nOrçamento utilizado/total: "
                    + String.format("(%d/%d)", currentEffort, totalBudget));
        }
        
        tasks.add(t);
    }

    /**
     * Estima o esforço total do projeto somando o esforço estimado de todas as tarefas.
     *
     * @return o esforço total em horas
     */
    public int estimateProjectEffort() {
        return getTasks().stream()
            .reduce(0, (total, task) -> total + task.getEstimatedEffort(), Integer::sum);
    }

    /**
     * Retorna o nome do projeto.
     *
     * @return o nome do projeto
     */
    public String getName() {
        return name;
    }

    /**
     * Retorna uma lista imutável de todas as tarefas do projeto.
     *
     * @return lista de tarefas
     */
    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    /**
     * Retorna o orçamento total de horas do projeto.
     *
     * @return o orçamento total em horas
     */
    public int getTotalBudget() {
        return totalBudget;
    }

    /**
     * Retorna o número total de tarefas no projeto.
     *
     * @return o número de tarefas
     */
    public int getTotalTasks() {
        if (tasks == null) {return 0; }
        return (int) tasks.size();
    }
}
