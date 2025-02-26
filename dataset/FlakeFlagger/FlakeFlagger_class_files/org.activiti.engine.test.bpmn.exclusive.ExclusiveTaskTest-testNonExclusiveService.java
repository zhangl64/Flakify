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
package org.activiti.engine.test.bpmn.exclusive;

import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.test.Deployment;

/**
 * 

 */
public class ExclusiveTaskTest extends PluggableActivitiTestCase {

  @Deployment public void testNonExclusiveService(){runtimeService.startProcessInstanceByKey("exclusive");Job job=managementService.createJobQuery().singleResult();assertNotNull(job);assertFalse(((JobEntity)job).isExclusive());waitForJobExecutorToProcessAllJobs(6000L,100L);assertEquals(0,managementService.createJobQuery().count());}

}
