@Library('bizapps-global-lib') _

bizappUiTestPipeline {
    projectName = 'bizap-income-analyzer-tests-ui'
    javaVersion = 11

    rpCfg.project = 'income-analyzer'
    parallelRest()

    branchesToExecuteTestByDefault = ['master']
    forceRestAndUiParallel = true
    defaultEnvs = ['qa-ia', 'dev-ia']

    mavenCustomProperties = '-Djunit.jupiter.execution.parallel.enabled=true -Djunit.jupiter.execution.parallel.config.strategy=fixed -Djunit.jupiter.execution.parallel.config.fixed.parallelism=3 -Dbizapps.LoanDocumentType=mismo'
}
