/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.standalone.escapeclause;

import java.util.HashMap;
import java.util.Map;

import org.flowable.engine.history.HistoricVariableInstance;
import org.flowable.engine.impl.history.HistoryLevel;
import org.flowable.engine.repository.DeploymentProperties;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Task;

public class HistoricVariableInstanceEscapeClauseTest extends AbstractEscapeClauseTestCase {

    private String deploymentOneId;

    private String deploymentTwoId;

    private ProcessInstance processInstance1;

    private ProcessInstance processInstance2;

    @Override
    protected void setUp() throws Exception {
        deploymentOneId = repositoryService
                .createDeployment()
                .tenantId("One%")
                .addClasspathResource("org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml")
                .deploymentProperty(DeploymentProperties.DEPLOY_AS_FLOWABLE5_PROCESS_DEFINITION, Boolean.TRUE)
                .deploy()
                .getId();

        deploymentTwoId = repositoryService
                .createDeployment()
                .tenantId("Two_")
                .addClasspathResource("org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml")
                .deploymentProperty(DeploymentProperties.DEPLOY_AS_FLOWABLE5_PROCESS_DEFINITION, Boolean.TRUE)
                .deploy()
                .getId();

        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("var%", "One%");
        processInstance1 = runtimeService.startProcessInstanceByKeyAndTenantId("oneTaskProcess", vars, "One%");
        runtimeService.setProcessInstanceName(processInstance1.getId(), "One%");

        vars = new HashMap<String, Object>();
        vars.put("var_", "Two_");
        processInstance2 = runtimeService.startProcessInstanceByKeyAndTenantId("oneTaskProcess", vars, "Two_");
        runtimeService.setProcessInstanceName(processInstance2.getId(), "Two_");

        Task task = taskService.createTaskQuery().processInstanceId(processInstance1.getId()).singleResult();
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance2.getId()).singleResult();
        taskService.complete(task.getId());

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        repositoryService.deleteDeployment(deploymentOneId, true);
        repositoryService.deleteDeployment(deploymentTwoId, true);
    }

    public void testQueryByVariableNameLike() {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
            HistoricVariableInstance historicVariable = historyService.createHistoricVariableInstanceQuery().variableNameLike("%\\%%").singleResult();
            assertNotNull(historicVariable);
            assertEquals(processInstance1.getId(), historicVariable.getProcessInstanceId());
            assertEquals("One%", historicVariable.getValue());

            historicVariable = historyService.createHistoricVariableInstanceQuery().variableNameLike("%\\_%").singleResult();
            assertNotNull(historicVariable);
            assertEquals(processInstance2.getId(), historicVariable.getProcessInstanceId());
            assertEquals("Two_", historicVariable.getValue());
        }
    }

    public void testQueryLikeByQueryVariableValue() {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
            HistoricVariableInstance historicVariable = historyService.createHistoricVariableInstanceQuery().variableValueLike("var%", "%\\%%").singleResult();
            assertNotNull(historicVariable);
            assertEquals(processInstance1.getId(), historicVariable.getProcessInstanceId());

            historicVariable = historyService.createHistoricVariableInstanceQuery().variableValueLike("var_", "%\\_%").singleResult();
            assertNotNull(historicVariable);
            assertEquals(processInstance2.getId(), historicVariable.getProcessInstanceId());
        }
    }

    public void testQueryLikeByQueryVariableValueIgnoreCase() {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
            HistoricVariableInstance historicVariable = historyService.createHistoricVariableInstanceQuery().variableValueLikeIgnoreCase("var%", "%\\%%").singleResult();
            assertNotNull(historicVariable);
            assertEquals(processInstance1.getId(), historicVariable.getProcessInstanceId());

            historicVariable = historyService.createHistoricVariableInstanceQuery().variableValueLikeIgnoreCase("var_", "%\\_%").singleResult();
            assertNotNull(historicVariable);
            assertEquals(processInstance2.getId(), historicVariable.getProcessInstanceId());
        }
    }
}
