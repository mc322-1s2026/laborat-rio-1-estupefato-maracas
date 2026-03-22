package com.nexus.exception;

/**
 * Exceção customizada que herda de {@link RuntimeException} para validações
 * específicas do sistema Nexus. É utilizada quando regras de negócio são violadas
 * durante operações de criar, atualizar ou deletar entidades.
 */
public class NexusValidationException extends RuntimeException {
    /**
     * Constrói uma nova exceção de validação Nexus com a mensagem fornecida.
     *
     * @param message a mensagem de erro descritiva
     */
    public NexusValidationException(String message) {
        super(message);
    }
}