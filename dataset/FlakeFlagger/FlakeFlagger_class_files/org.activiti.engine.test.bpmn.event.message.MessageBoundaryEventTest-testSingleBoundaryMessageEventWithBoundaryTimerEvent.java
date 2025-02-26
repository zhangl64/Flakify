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

package org.activiti.engine.test.bpmn.event.message;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.TimerJobQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**

 */
public class MessageBoundaryEventTest extends PluggableActivitiTestCase {

  @Deployment public void testSingleBoundaryMessageEventWithBoundaryTimerEvent(){final Date startTime=new Date();processEngineConfiguration.getClock().setCurrentTime(startTime);runtimeService.startProcessInstanceByKey("process");assertEquals(3,runtimeService.createExecutionQuery().count());Execution execution=runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName").singleResult();assertNull(execution);Task userTask=taskService.createTaskQuery().singleResult();assertNotNull(userTask);assertEquals("task",userTask.getTaskDefinitionKey());final TimerJobQuery jobQuery=managementService.createTimerJobQuery().processInstanceId(userTask.getProcessInstanceId());assertEquals(1,jobQuery.count());processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + ((60 * 60 * 1000) + 5000)));waitForJobExecutorOnCondition(5000L,100L,new Callable<Boolean>(){public Boolean call() throws Exception{return taskService.createTaskQuery().count() == 2;}});assertEquals(1L,jobQuery.count());userTask=taskService.createTaskQuery().taskDefinitionKey("task").singleResult();assertNotNull(userTask);taskService.complete(userTask.getId());userTask=taskService.createTaskQuery().taskDefinitionKey("taskTimer").singleResult();assertNotNull(userTask);taskService.complete(userTask.getId());assertEquals(1L,jobQuery.count());userTask=taskService.createTaskQuery().singleResult();assertEquals("taskAfterTask",userTask.getTaskDefinitionKey());execution=runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName").singleResult();assertNotNull(execution);runtimeService.messageEventReceived("messageName",execution.getId());userTask=taskService.createTaskQuery().singleResult();assertNotNull(userTask);assertEquals("task",userTask.getTaskDefinitionKey());execution=runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName").singleResult();assertNull(execution);taskService.complete(userTask.getId());userTask=taskService.createTaskQuery().singleResult();assertNotNull(userTask);assertEquals("taskAfterTask",userTask.getTaskDefinitionKey());execution=runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName").singleResult();assertNotNull(execution);processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + ((2 * 60 * 60 * 1000) + 5000)));waitForJobExecutorOnCondition(2000L,100L,new Callable<Boolean>(){@Override public Boolean call() throws Exception{return taskService.createTaskQuery().count() == 2;}});assertEquals(1L,jobQuery.count());processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + ((3 * 60 * 60 * 1000) + 5000)));waitForJobExecutorOnCondition(2000L,100L,new Callable<Boolean>(){@Override public Boolean call() throws Exception{return taskService.createTaskQuery().list().size() == 3;}});assertEquals(1L,jobQuery.count());final List<Task> tasks=taskService.createTaskQuery().taskDefinitionKey("taskAfterTaskTimer").list();assertEquals(2,tasks.size());taskService.complete(tasks.get(0).getId());taskService.complete(tasks.get(1).getId());taskService.complete(userTask.getId());userTask=taskService.createTaskQuery().singleResult();assertNotNull(userTask);assertEquals("taskAfterTaskAfterTask",userTask.getTaskDefinitionKey());taskService.complete(userTask.getId());assertEquals(0,runtimeService.createProcessInstanceQuery().count());execution=runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName").singleResult();assertNull(execution);}

}
