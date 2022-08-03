# Aplicando os conceitos de inversão de dependência e open closed do SOLID utilizando Spring (@Primary e @Qualifier)

## Introdução

O conhecimento dos conceitos básicos de Orientação a Objetos nem sempre são suficientes para a criação de um software de qualidade. É preciso se ter uma boa noção de como se deve organizar as funções e estruturas de dados em classe e como estas devem se relacionar entre si. Este artigo procura mostrar como se pode aplicar, de uma forma elegante, os conceitos _Open Closed_ e _Dependency Inversion_ do _SOLID_ utilizando o gerenciamento de implementações de uma abstração em Spring

## O que é SOLID?

Uma série de princípios que auxiliam a manter boas práticas arquiteturais a partir de definições de como se organizar e interconectar os agrupamentos de software (classes, funções e etc.). O propósito final da aplicação desses princípios é, principalmente, a preservação da manutenibilidade a partir da controle do acoplamento, coesão e encapsulamento.

>**S**ingle Responsability Principle
>
>**O**pen Closed Principle
>
>**L**iskov Substitution Principle
>
>**I**nterface Segregation Principle
>
>**D**ependecy Inversion Principle

## Situação Problema

Você está desenvolvendo um software que controla a distribuição do salário dos funcionários de uma empresa que implementa a seguinte regra de negócio

> O reajuste salarial deve respeitar às seguintes regras
>
> - Em caso de reajuste percentual, o salário não pode aumentar em mais que 40% do salário anterior.
> - O reajuste periódico só pode ocorrer de seis em seis meses.

Você vai até o código fonte e encontra uma classe de serviço que implementa o reajuste do qual a regra de negócio está falando. Sua tarefa é refatora-lo para que, futuramente, novas alterações causem menor impacto.

```java
public class ReajusteService {

    public void reajustarSalarioDoFuncionario(Funcionario funcionario, BigDecimal aumento) throws Exception{

        // realizando a validação de percentual
        BigDecimal salarioAtual = funcionario.getSalario();
        BigDecimal percentualReajuste = aumento.divide(salarioAtual, RoundingMode.HALF_UP);
        if(percentualReajuste.compareTo(new BigDecimal("0.4")) > 0){
            throw new Exception("Reajuste não pode ser superior a 40%");
        }

        // realizando a validação de periodicidade
        LocalDate dataUltimoReajuste = funcionario.getDataUltimoReajuste();
        LocalDate dataAtual = LocalDate.now();
        long mesesDesdeUltimoReajuste = ChronoUnit.MONTHS.between(dataUltimoReajuste, dataAtual);
        if(mesesDesdeUltimoReajuste < 6){
            throw new Exception("Intervalo entre reajustes não deve ser menor que 6 meses");
        }

        BigDecimal salarioReajustado = funcionario.getSalario().add(aumento);
        funcionario.atualizarSalario(salarioReajustado);
    }
}
```

## O Princípio Open Closed (OCP)

> "_Entidades de software (classes, módulos, funções, etc.) devem estar abertas para extensão, porém fechadas para a modificação_"

Segundo o _OCP_, popularizado por Bertrand Meyes na década de 1980, a construção de um software de fácil manutenção envolve projeta-lo de forma a permitir mudanças de comportamento adicionando código ao invés de se modificar o que já existe.

Perceba que, no exemplo ilustrado, a medida que novas validações de reajuste forem surgindo, esse método crescerá indefinidamente, tornando sua manutenção cada vez mais difícil a longo prazo.

## Uma maneira elegante de se resolver

O objetivo aqui, a grosso modo, é pensar em uma forma de mover toda a lógica por trás das validações para fora do serviço de reajuste.

Ambas validações de reajuste, tanto a validação de periodicidade quanto a de percentual, possuem duas dependências em comum: o funcionário e o aumento salarial. Além disso, ambas cumprem o mesmo propósito: validar o reajuste. Ou seja, podemos criar uma _abstração_ que será implementada individualmente por cada tipo de validação de reajuste necessária.

```java
public interface ValidacaoReajuste {

    void validar(Funcionario funcionario, BigDecimal aumento) throws Exception;
}
```

Agora, podemos isolar as validações de reajuste em diferentes classes que cumprem exatamento um único papel.

```java
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
            throw new Exception("Intervalo entre reajustes não deve ser menor que 6 meses");
        }
    }
}
```

Até aqui tudo bem, mas como indicamos para o Spring qual deve ser a inplementação utilizada ao injetarmos estas validações no serviço de reajuste salarial?

## A anotação @Primary

Anotação utiizada para definir a implementaçao principal, que deverá ser utilizada, caso não haja nenhum _qualifier_ especificado na injeção dessa depência, de uma interface injetada em um _Bean_ qualquer.

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
        BigDecimal salarioReajustado = funcionario.getSalario().add(aumento);
        funcionario.atualizarSalario(salarioReajustado);
    }
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
        BigDecimal salarioReajustado = funcionario.getSalario().add(aumento);
        funcionario.atualizarSalario(salarioReajustado);
    }
}
```

> Uma percepção importante é que o uso do **@Qualifier** não inviabiliza o uso do **@Primary** e que não é obrigatório anotar uma classe com **@Primary**. Você pode somente definir os _qualifiers_ nas implementações e referencia-los a medida que desejar realizar a injeção da abstração em outras classes. Porém, ao optar por não anotar nenhuma classe com **@Primary**, sempre que houver a injeção de dependência da abstração, deve-se especificar qual _qualifier_ deverá ser utilizado.

Será que é necessário realizar a injeção de uma dependência por vez para o caso em que desejamos utilizar várias implementações?

## Realizando todas as validações do exemplo ilustrado

No nosso exemplo, para realizamos todas as validações implementadas nos componentes de validação, basta injetarmos uma lista do tipo _ValidacaoReajuste_. Veja o exemplo abaixo

```java
@Service
public class ReajustePercentualService {

    @Autowired
    private List<ValidacaoReajuste> listaValidacoesReajuste;

    public void reajustarSalarioFuncionarioComTodasValidacoes(Funcionario funcionario, BigDecimal aumento) throws Exception {
        for (ValidacaoReajuste validacao : listaValidacoesReajuste) {
            validacao.validar(funcionario, aumento);
        }
        BigDecimal salarioReajustado = funcionario.getSalario().add(aumento);
        funcionario.atualizarSalario(salarioReajustado);
    }
}
```

Dessa forma, executaremos todas as validações necessárias antes de realizar o ajuste do salário do funcionário.

## Okay, mas onde está a aplicação do princípio da Inversão de Dependência?

### Primeiro, o que diz o Princípio da Inversão de Dependência?

> "_Abstrações não devem depender de implementações. Implementações devem depender de abtrações_"

As políticas de alto nível que são implementadas em um código não devem depender dos detalhes de mais baixo nível, mas sim o contrário.

Isso parece familiar ao revisitarmos nosso exemplo. Note que ao projetarmos as validações a partir de uma interface (_ValidacaoReajuste_), a implementação de como essa validação deve ser feita depende da definição de uma abstração predefinida. Caso futuramente novas regras de negócio surjam com o objetivo de se criar novas validações de reajuste, estas também irão depender da interface.

## Conclusão

A aplicação dos princípios _SOLID_ são útei principalmente quando pensamos em manutenibilidade de software. Renegar a projeção de um software pode significar dores de cabeça no futuro.
