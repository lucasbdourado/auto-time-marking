package com.lucasbdourado.autotimemarking.modules.scheduler.domain;

public interface MarkingWorkflow {
    /**
     * Executes one check and marking cycle on BMAquiosque.
     * Throws Exception on failure to allow scheduler to track errors.
     */
    void executeMarkingCycle() throws Exception;
}
