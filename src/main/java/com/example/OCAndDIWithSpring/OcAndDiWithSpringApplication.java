package com.example.OCAndDIWithSpring;

import com.example.OCAndDIWithSpring.modelos.Funcionario;
import com.example.OCAndDIWithSpring.servicos.ReajustePercentualService;
import com.example.OCAndDIWithSpring.validacoes.ValidacaoReajusteTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootApplication
public class OcAndDiWithSpringApplication implements CommandLineRunner {

	@Autowired
	private ReajustePercentualService reajustePercentualService;

	public static void main(String[] args) {
		SpringApplication.run(OcAndDiWithSpringApplication.class, args);
	}

	/**
	 * Executa todos os métodos de ValidacaoReajuste {@link ValidacaoReajusteTest}
	 * que implementam difernetes formas de injeção de dependências a partir
	 * de uma interface com multiplas implementações
	 *
	 * Saída esperada no console:
	 *
	 * reajustarSalarioFuncionarioComTodasValidacoes: Intervalo entre reajustes deve ser menor que 6 meses
	 * reajustarSalarioFuncionarioComTodasValidacoes: Reajuste não pode ser superior a 40%
	 * reajustarSalarioFuncionarioValidandoPercentual: Reajuste não pode ser superior a 40%
	 * reajustarSalarioFuncionarioValidandoPrincipal: Intervalo entre reajustes deve ser menor que 6 meses
	 */
	@Override
	public void run(String... args) {
		Funcionario dadosFuncionario = new Funcionario(LocalDate.now(), new BigDecimal("10000"));
		try{
			reajustePercentualService.reajustarSalarioFuncionarioComTodasValidacoes(
					dadosFuncionario,
					new BigDecimal("20")
			);
		}catch (Exception exPeriodicidadeTodasValidacoes) {
			try {
				System.out.println("reajustarSalarioFuncionarioComTodasValidacoes: " + exPeriodicidadeTodasValidacoes.getMessage());
				reajustePercentualService.reajustarSalarioFuncionarioComTodasValidacoes(dadosFuncionario, new BigDecimal("10000"));
			} catch (Exception exPercentualTodasValidacoes) {
				System.out.println("reajustarSalarioFuncionarioComTodasValidacoes: " + exPercentualTodasValidacoes.getMessage());
				try{
					reajustePercentualService.reajustarSalarioFuncionarioValidandoPercentual(dadosFuncionario, new BigDecimal("10000"));
				} catch (Exception exPercentual) {
					System.out.println("reajustarSalarioFuncionarioValidandoPercentual: " + exPercentual.getMessage());
					try {
						reajustePercentualService.reajustarSalarioFuncionarioValidandoPrincipal(dadosFuncionario, new BigDecimal("10000"));
					} catch (Exception exPrimary) {
						System.out.println("reajustarSalarioFuncionarioValidandoPrincipal: " + exPrimary.getMessage());
					}
				}
			}
		}
	}
}
