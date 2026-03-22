package com.nexus.model;

/**
 * Enumeração que define os estados possíveis de uma {@link Task} no sistema Nexus.
 * <ul>
 *   <li><b>TO_DO</b> - Tarefa criada e pronta para ser iniciada</li>
 *   <li><b>IN_PROGRESS</b> - Tarefa em andamento (requer um usuário atribuído)</li>
 *   <li><b>BLOCKED</b> - Tarefa bloqueada, não pode progredir</li>
 *   <li><b>DONE</b> - Tarefa concluída com sucesso</li>
 * </ul>
 */
public enum TaskStatus {
    /**
     * Tarefa no estado inicial, pronta para ser iniciada.
     */
    TO_DO,
    /**
     * Tarefa em execução, atribuída a um usuário.
     */
    IN_PROGRESS,
    /**
     * Tarefa bloqueada e não pode fazer progressão.
     */
    BLOCKED,
    /**
     * Tarefa concluída com êxito.
     */
    DONE,
}