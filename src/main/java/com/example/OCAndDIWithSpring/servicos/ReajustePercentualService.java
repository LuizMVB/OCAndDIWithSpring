package com.example.OCAndDIWithSpring.servicos;

import com.example.OCAndDIWithSpring.modelos.Funcionario;
import com.example.OCAndDIWithSpring.validacoes.ValidacaoReajuste;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ReajustePercentualService {

    @Autowired
    private ValidacaoReajuste validacaoPrincipal;

    @Autowired
    @Qualifier("validacaoPercentualReajuste")
    private ValidacaoReajuste validacaoPercentualReajuste;

    @Autowired
    private List<ValidacaoReajuste> listaValidacoesReajuste;

    /**
     * Realiza o reajuste de salário validando somente com a implementação presente em ValidacaoPeriodicidadeEntreReajustes
     * classe anotada como @Primary
     * @param funcionario O funcionário que terá o salaráio reajustado
     * @param aumento O valordo aumento que será acrescido ao salário atual
     * @throws Exception Lança Exception em caso de erro de validação
     */
    public void reajustarSalarioFuncionarioValidandoPrincipal(Funcionario funcionario, BigDecimal aumento) throws Exception {
        validacaoPrincipal.validar(funcionario, aumento);
        realizarReajuste(funcionario, aumento);
    }

    /**
     * Realiza o reajuste de salário validando somente o percentual
     * @param funcionario O funcionário que terá o salaráio reajustado
     * @param aumento O valordo aumento que será acrescido ao salário atual
     * @throws Exception Lança Exception em caso de erro de validação
     */
    public void reajustarSalarioFuncionarioValidandoPercentual(Funcionario funcionario, BigDecimal aumento) throws Exception {
        validacaoPercentualReajuste.validar(funcionario, aumento);
        realizarReajuste(funcionario, aumento);
    }

    /**
     * Realiza o reajuste de salário aplicando todas as validações
     * @param funcionario O funcionário que terá o salaráio reajustado
     * @param aumento O valordo aumento que será acrescido ao salário atual
     * @throws Exception Lança Exception em caso de erro de validação
     */
    public void reajustarSalarioFuncionarioComTodasValidacoes(Funcionario funcionario, BigDecimal aumento) throws Exception{
        for(ValidacaoReajuste validacao: listaValidacoesReajuste){
            validacao.validar(funcionario, aumento);
        }
        realizarReajuste(funcionario, aumento);
    }

    /**
     * Realiza o reajuste
     * @param funcionario O funcionário que terá o salaráio reajustado
     * @param aumento O valordo aumento que será acrescido ao salário atual
     */
    private void realizarReajuste(Funcionario funcionario, BigDecimal aumento){
        BigDecimal salarioReajustado = funcionario.getSalario().add(aumento);
        funcionario.atualizarSalario(salarioReajustado);
    }
}
