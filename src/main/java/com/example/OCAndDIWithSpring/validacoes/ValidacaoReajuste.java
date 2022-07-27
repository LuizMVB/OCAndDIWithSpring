package com.example.OCAndDIWithSpring.validacoes;

import com.example.OCAndDIWithSpring.modelos.Funcionario;

import java.math.BigDecimal;

public interface ValidacaoReajuste {
    void validar(Funcionario funcionario, BigDecimal aumento) throws Exception;
}
