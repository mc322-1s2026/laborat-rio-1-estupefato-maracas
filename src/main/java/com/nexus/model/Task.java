package com.nexus.model;

import java.time.LocalDate;

import com.nexus.exception.NexusValidationException;

public class Task {
    // Métricas Globais (Alunos implementam a lógica de incremento/decremento)
    public static int totalTasksCreated = 0;
    public static int totalValidationErrors = 0;
    public static int activeWorkload = 0;

    private static int nextId = 1;

    private final int id;
    private final LocalDate deadline;
    private String title;
    private TaskStatus status;
    private User owner;
    private int estimatedEffort;
    private String linkedProjectName;

    public static void throwNexusError(String message) {
        totalValidationErrors++;
        throw new NexusValidationException(message);
    }

    public Task(String title, LocalDate deadline, int estimatedEffort, String linkedProjectName) {
        this.id = nextId++;
        this.deadline = deadline;
        this.title = title;
        this.status = TaskStatus.TO_DO;
        this.estimatedEffort = estimatedEffort;
        this.linkedProjectName = linkedProjectName;

        // Ação do Aluno:
        totalTasksCreated++; 
    }

    /**
     * Move a tarefa para IN_PROGRESS.
     * Regra: Só é possível se houver um owner atribuído e não estiver BLOCKED.
     */
    public void moveToInProgress() {
        // TODO: Implementar lógica de proteção e atualizar activeWorkload
        // Se falhar, incrementar totalValidationErrors e lançar NexusValidationException
        if (this.owner != null && this.status == TaskStatus.TO_DO) {
            this.status = TaskStatus.IN_PROGRESS;
            activeWorkload++;
        }
        else {
            throwNexusError("Estado da Task não pode ser alterado.");
        }
    }

    public void assignUser(User user) {
        if (user != null) {
            this.owner = user;
        }
        else {  
            throw new IllegalArgumentException("Usuário inválido.");
        }
    }

    /**
     * Finaliza a tarefa.
     * Regra: Só pode ser movida para DONE se não estiver BLOCKED.
     */
    public void markAsDone() {
        // TODO: Implementar lógica de proteção e atualizar activeWorkload (decrementar)
        if (status != TaskStatus.BLOCKED) {
            if (status == TaskStatus.IN_PROGRESS) {
                activeWorkload--;
            }
            status = TaskStatus.DONE;
        }
        else{
            throwNexusError("Tarefa bloqueada, não pode ser concluída.");
        }
    }

    public void setBlocked(boolean blocked) {
        if (blocked) {
            if (status == TaskStatus.DONE) {
                throwNexusError("Tarefa concluída, não pode ser bloqueada.");
            }

            this.status = TaskStatus.BLOCKED;
        } else if (status == TaskStatus.BLOCKED) {
            this.status = TaskStatus.TO_DO; // Simplificação para o Lab
        }
    }

    public void changeStatus(TaskStatus status) {
        if (status == TaskStatus.BLOCKED) {
            setBlocked(true);
        }
        else if (status == TaskStatus.IN_PROGRESS) {
            moveToInProgress();
        }
        else if (status == TaskStatus.DONE) {
            markAsDone();
        }
        else if (status == TaskStatus.TO_DO) {
            status = TaskStatus.TO_DO;
        }
        else {
            throwNexusError("Incapaz de mudar status da task.");
        }
    }

    // Getters
    public int getId() { return id; }
    public TaskStatus getStatus() { return status; }
    public String getTitle() { return title; }
    public LocalDate getDeadline() { return deadline; }
    public User getOwner() { return owner; }
    public int getEstimatedEffort() {return estimatedEffort; }
    public String getProjectName() {return !linkedProjectName.isEmpty() && !linkedProjectName.isBlank() ? linkedProjectName : "N/A"; }
}