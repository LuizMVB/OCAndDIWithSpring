package com.example.OCAndDIWithSpring.validacoes;

import com.example.OCAndDIWithSpring.modelos.Funcionario;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public interface ValidacaoReajusteTest {
    void validar(Funcionario funcionario, BigDecimal aumento) throws Exception;
}
