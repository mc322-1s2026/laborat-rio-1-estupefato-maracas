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

    /**
     * Lança uma {@link NexusValidationException} e incrementa a métrica global
     * de erros de validação do sistema.
     */
    public static void throwNexusError(String message) {
        totalValidationErrors++;
        throw new NexusValidationException(message);
    }

    /**
     * Constrói uma nova tarefa, inicializando-a com o status {@link TaskStatus.TO_DO}
     * e incrementando o contador global de tarefas criadas no sistema.
     */
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
     * Move a tarefa para o estado {@link TaskStatus.IN_PROGRESS} e atualiza
     * a carga de trabalho ativa (activeWorkload).
     * Ignora se já estiver em progresso. Falha se não houver um {@link User}
     * atribuído ou se a tarefa estiver bloqueada.
     */
    public void moveToInProgress() {
        if (this.status == TaskStatus.IN_PROGRESS) {
            return;
        }
        if (this.owner == null) {
            throwNexusError("Delegue a tarefa a um usuário para poder deixá-la em progresso.");
        }
        if (this.status == TaskStatus.BLOCKED) {
            throwNexusError("Tarefa bloqueada, não pode entrar em progresso.");
        }
        this.status = TaskStatus.IN_PROGRESS;
        activeWorkload++;
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
     * Finaliza a tarefa, movendo-a para {@link TaskStatus#DONE}.
     * Falha se a tarefa estiver bloqueada. Se a tarefa estava em andamento,
     * decrementa a carga de trabalho ativa.
     */
    public void markAsDone() {
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

    /**
     * Altera o estado de bloqueio da tarefa.
     * Falha ao tentar bloquear uma tarefa já concluída. Se a flag for falsa
     * e a tarefa estiver bloqueada, ela retorna para o estado inicial.
     */
    public void setBlocked(boolean blocked) {
        if (blocked) {
            if (status == TaskStatus.DONE) {
                throwNexusError("Tarefa concluída, não pode ser bloqueada.");
            }

            this.status = TaskStatus.BLOCKED;
        } else if (status == TaskStatus.BLOCKED) {
            this.status = TaskStatus.TO_DO;
        }
    }

    /**
     * Método que atribuí a mudança de estado para os métodos específicos
     * com base no {@link TaskStatus} fornecido.
     */
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
            this.status = TaskStatus.TO_DO;
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