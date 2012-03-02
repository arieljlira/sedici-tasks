/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ar.edu.unlp.sedici.tasks.api;

import ar.edu.unlp.sedici.tasks.exception.StopExecutionException;



/**
 * A worker is the unit that is doing the actual work. A {@link Droid} is the
 * "project manger" that delegates the work to worker units. Worker units are
 * implemented as threads to scale they number if more work is to do.
 * Worker implemementations should check periodically the currentThread's  interrupt status (using 
 * {@link Thread.interrupted()} ) 
 * @version 1.0
 * 
 */
public interface Worker<T extends Task> {
     
  void execute() throws StopExecutionException, InterruptedException;
  
}
