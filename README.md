# Aplicando os conceitos de inversão de dependência e open closed do SOLID utilizando Spring Boot

## Introdução

Imagine que existam duas classes de validação que implementam diferentes ações e devem depender de uma única abstração.

```java
public interface ValidacaoReajuste {
    void validar(Funcionario funcionario, BigDecimal aumento) throws Exception;
}

@Component
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

@Component
public class ValidacaoPeriodicidadeEntreReajustes implements ValidacaoReajuste {

    @Override
    public void validar(Funcionario funcionario, BigDecimal aumento) throws Exception {
        LocalDate dataUltimoReajuste = funcionario.getDataUltimoReajuste();
        LocalDate dataAtual = LocalDate.now();
        long mesesDesdeUltimoReajuste = ChronoUnit.MONTHS.between(dataUltimoReajuste, dataAtual);
        if(mesesDesdeUltimoReajuste < 6){
            throw new Exception("Intervalo entre reajustes deve ser menor que 6 meses");
        }
    }
}
```

Como indicamos para o Spring qual deve ser a inplementação utilizada ao injetarmos estas validações em um serviço.

## @Primary

Anotação utiizada para definir a implementaçao principal, que deverá ser utilizada caso não haja nenhum _qualifier_ especificado, de uma interface injetada em um Bean qualquer.

Caso o objetivo seja priorizar uma determinada implementação, devemos utilizar a anotação **@Primary**. Esta anotação define, dentre as implementações da interface que será injetada, qual deve ser utilizada prioritariamente.

No nosso exemplo, para definirmos a implementação de _ValidacaoPeriodicidadeEntreReajustes_ como a principal, devemos realizar as seguintes alterações

```java
@Component
@Primary
public class ValidacaoPeriodicidadeEntreReajustes implements ValidacaoReajuste {
    // implementação demonstrada anteriormente
}

@Service
public class ReajustePercentualService {

    @Autowired
    private ValidacaoReajuste validacaoPrincipal; // considera a classe anotada com @Primary

    public void reajustarSalarioFuncionarioValidandoPrincipal(Funcionario funcionario, BigDecimal aumento) throws Exception {
        validacaoPrincipal.validar(funcionario, aumento); // executa a implementação de ValidacaoPeriodicidadeEntreReajustes
        realizarReajuste(funcionario, aumento);
    }
```

Agora, o que fazer para utilizar a implementação de outra classe?

## @Qualifier

 A anotação **@Qualifier** serve para a identificação de qual implementação deverá ser utilizada pela interface injetada em um Bean qualquer.

No nosso exemplo, para definirmos que a validação utilizada pela injeção dessa abstração seja especificamente a impementada por _ValidacaoPercentualReajuste_, devemos realizar as seguintes alterações

```java
@Component
@Qualifier("validacaoPercentualReajuste")
public class ValidacaoPercentualReajuste implements ValidacaoReajuste {
    // implementação demonstrada anteriormente
}

@Service
public class ReajustePercentualService {

    @Autowired
    @Qualifier("validacaoPercentualReajuste")
    private ValidacaoReajuste validacaoPercentualReajuste;

    public void reajustarSalarioFuncionarioValidandoPercentual(Funcionario funcionario, BigDecimal aumento) throws Exception {
        validacaoPercentualReajuste.validar(funcionario, aumento); // executa a implementação de ValidacaoPercentualReajuste
        realizarReajuste(funcionario, aumento);
    }
```

Uma percepção importante é que o uso do **@Qualifier** não inviabiliza o uso do **@Primary** e que não é obrigatório anotar uma classe com **@Primary**. Você pode somente definir os _qualifiers_ nas implementações e referencia-los a medida que desejar realizar a injeção da abstração em outras classes. Porém, ao optar por não anotar nenhuma classe com **@Primary**, sempre que houver a injeção de dependência da abstração, deve-se especificar qual _qualifier_ deverá ser utilizado.

Será que é necessário realizar a injeção de uma dependência por vez para o caso em que desejamos utilizar várias implementações?

## Realizando todas as validações

No nosso exemplo, para realizamos todas as validações implementadas nos componentes de validação, basta injetarmos uma lista do tipo _ValidacaoReajuste_. Veja o exemplo abaixo

```java
@Service
public class ReajustePercentualService {

    @Autowired
    private List<ValidacaoReajuste> listaValidacoesReajuste;

    public void reajustarSalarioFuncionarioComTodasValidacoes(Funcionario funcionario, BigDecimal aumento) throws Exception{
        for(ValidacaoReajuste validacao: listaValidacoesReajuste){
            validacao.validar(funcionario, aumento);
        }
        realizarReajuste(funcionario, aumento);
    }
```

Dessa forma, executaremos todas as validações necessárias antes de realizar o ajuste do salário do funcionário.
