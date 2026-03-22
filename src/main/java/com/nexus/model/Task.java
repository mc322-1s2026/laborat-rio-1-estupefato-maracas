package com.nexus.model;

import java.time.LocalDate;

import com.nexus.exception.NexusValidationException;

/**
 * Representa uma tarefa no sistema Nexus, contendo informações como título,
 * prazo, status, proprietário, esforço estimado e projeto vinculado.
 * Inclui métricas globais para rastreamento de tarefas criadas, erros de validação
 * e carga de trabalho ativa.
 */
public class Task {
    // Métricas Globais (Alunos implementam a lógica de incremento/decremento)
    /**
     * Contador global do número total de tarefas criadas no sistema.
     */
    public static int totalTasksCreated = 0;
    /**
     * Contador global do número total de erros de validação ocorridos no sistema.
     */
    public static int totalValidationErrors = 0;
    /**
     * Contador global da carga de trabalho ativa (tarefas em progresso).
     */
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
     *
     * @param message a mensagem de erro
     * @throws NexusValidationException sempre lançado
     */
    public static void throwNexusError(String message) {
        totalValidationErrors++;
        throw new NexusValidationException(message);
    }

    /**
     * Constrói uma nova tarefa, inicializando-a com o status {@link TaskStatus#TO_DO}
     * e incrementando o contador global de tarefas criadas no sistema.
     *
     * @param title o título da tarefa
     * @param deadline o prazo da tarefa
     * @param estimatedEffort o esforço estimado em horas
     * @param linkedProjectName o nome do projeto vinculado
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
     * Move a tarefa para o estado {@link TaskStatus#IN_PROGRESS} e atualiza
     * a carga de trabalho ativa (activeWorkload).
     * Ignora se já estiver em progresso. Falha se não houver um {@link User}
     * atribuído ou se a tarefa estiver bloqueada.
     *
     * @throws NexusValidationException se não houver um usuário atribuído ou se estiver bloqueada
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

    /**
     * Atribui um usuário proprietário à tarefa.
     * @param user o usuário a ser atribuído; não pode ser null
     * @throws IllegalArgumentException se o usuário for null
     */
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
     *
     * @throws NexusValidationException se a tarefa estiver bloqueada
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
     *
     * @param blocked {@code true} para bloquear a tarefa, {@code false} para desbloquear
     * @throws NexusValidationException se tentar bloquear uma tarefa concluída
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
     * Delega a mudança de estado para os métodos específicos
     * com base no {@link TaskStatus} fornecido.
     *
     * @param status o novo estado desejado
     * @throws NexusValidationException se a mudança de estado não for permitida
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
    /**
     * Retorna o ID único da tarefa.
     * @return o ID da tarefa
     */
    public int getId() { return id; }
    /**
     * Retorna o status atual da tarefa.
     * @return o status da tarefa
     */
    public TaskStatus getStatus() { return status; }
    /**
     * Retorna o título da tarefa.
     * @return o título da tarefa
     */
    public String getTitle() { return title; }
    /**
     * Retorna o prazo da tarefa.
     * @return o prazo da tarefa
     */
    public LocalDate getDeadline() { return deadline; }
    /**
     * Retorna o proprietário da tarefa.
     * @return o proprietário da tarefa
     */
    public User getOwner() { return owner; }
    /**
     * Retorna o esforço estimado da tarefa.
     * @return o esforço estimado
     */
    public int getEstimatedEffort() {return estimatedEffort; }
    /**
     * Retorna o nome do projeto vinculado, ou "N/A" se não houver.
     * @return o nome do projeto ou "N/A"
     */
    public String getProjectName() {
        return (linkedProjectName.isBlank() ? "N/A" : linkedProjectName);
    }
}