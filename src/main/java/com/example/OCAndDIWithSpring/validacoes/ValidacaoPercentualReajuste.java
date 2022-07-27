package com.example.OCAndDIWithSpring.validacoes;

import com.example.OCAndDIWithSpring.modelos.Funcionario;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@Qualifier("validacaoPercentualReajuste")
public class ValidacaoPercentualReajuste implements ValidacaoReajuste {
    @Override
    public void validar(Funcionario funcionario, BigDecimal aumento) throws Exception {
        BigDecimal salarioAtual = funcionario.getSalario();
        BigDecimal percentualReajuste = aumento.divide(salarioAtual, RoundingMode.HALF_UP);
        if(percentualReajuste.compareTo(new BigDecimal("0.4")) > 0){
            throw new Exception("Reajuste não pode ser superior a 40%");
        }
    }
}
