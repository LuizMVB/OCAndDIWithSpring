package com.example.OCAndDIWithSpring.modelos;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Funcionario {
    private LocalDate dataUltimoReajuste;
    private BigDecimal salario;

    public Funcionario(LocalDate dataUltimoReajuste, BigDecimal salario) {
        this.dataUltimoReajuste = dataUltimoReajuste;
        this.salario = salario;
    }

    public LocalDate getDataUltimoReajuste() {
        return dataUltimoReajuste;
    }

    public void setDataUltimoReajuste(LocalDate dataUltimoReajuste) {
        this.dataUltimoReajuste = dataUltimoReajuste;
    }

    public BigDecimal getSalario() {
        return salario;
    }

    public void setSalario(BigDecimal salario) {
        this.salario = salario;
    }

    public void atualizarSalario(BigDecimal salarioReajustado) {
        this.salario = salarioReajustado;
        this.dataUltimoReajuste = LocalDate.now();
    }
}
