package com.lucasbdourado.autotimemarking.modules.calculation.domain;

public enum MarkingType {
    ENTRY(1, "Entrada"),
    LUNCH_OUT(2, "Saída para Almoço"),
    LUNCH_RETURN(3, "Retorno do Almoço"),
    EXIT(4, "Saída");

    private final int sequence;
    private final String label;

    MarkingType(int sequence, String label) {
        this.sequence = sequence;
        this.label = label;
    }

    public int getSequence() {
        return sequence;
    }

    public String getLabel() {
        return label;
    }
}
