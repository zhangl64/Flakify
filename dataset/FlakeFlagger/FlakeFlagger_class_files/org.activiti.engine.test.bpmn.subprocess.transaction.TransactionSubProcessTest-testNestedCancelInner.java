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

package org.activiti.engine.test.bpmn.subprocess.transaction;

import java.util.List;

import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**

 */
public class TransactionSubProcessTest extends PluggableActivitiTestCase {

  @Deployment public void testNestedCancelInner(){ProcessInstance processInstance=runtimeService.startProcessInstanceByKey("transactionProcess");assertEquals(0,createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").count());assertEquals(5,createEventSubscriptionQuery().eventType("compensate").activityId("innerTxundoBookHotel").count());assertEquals(1,createEventSubscriptionQuery().eventType("compensate").activityId("innerTxundoBookFlight").count());Task taskInner=taskService.createTaskQuery().taskDefinitionKey("innerTxaskCustomer").singleResult();Task taskOuter=taskService.createTaskQuery().taskDefinitionKey("bookFlight").singleResult();assertNotNull(taskInner);assertNotNull(taskOuter);taskService.setVariable(taskInner.getId(),"confirmed",false);taskService.complete(taskInner.getId());List<String> activeActivityIds=runtimeService.getActiveActivityIds(processInstance.getId());assertTrue(activeActivityIds.contains("afterInnerCancellation"));assertEquals(0,createEventSubscriptionQuery().eventType("compensate").activityId("innerTxundoBookHotel").count());assertEquals(0,createEventSubscriptionQuery().eventType("compensate").activityId("innerTxundoBookFlight").count());assertEquals(0,createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").count());assertEquals(5,runtimeService.getVariable(processInstance.getId(),"innerTxundoBookHotel"));assertEquals(1,runtimeService.getVariable(processInstance.getId(),"innerTxundoBookFlight"));if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)){assertEquals(5,historyService.createHistoricActivityInstanceQuery().activityId("innerTxundoBookHotel").count());assertEquals(1,historyService.createHistoricActivityInstanceQuery().activityId("innerTxundoBookFlight").count());}taskService.complete(taskOuter.getId());runtimeService.trigger(runtimeService.createExecutionQuery().activityId("afterInnerCancellation").singleResult().getId());assertProcessEnded(processInstance.getId());assertEquals(0,runtimeService.createExecutionQuery().count());}

  private EventSubscriptionQueryImpl createEventSubscriptionQuery() {
    return new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutor());
  }
}
