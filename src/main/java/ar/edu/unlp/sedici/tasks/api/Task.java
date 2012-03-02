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

import java.io.Serializable;

import ar.edu.unlp.sedici.tasks.exception.InvalidTaskException;

/**
 * A task is a working instruction for a droid. One can limit the depth of the
 * task. That is based on the fact that a droid can extract more tasks from the
 * one that it is currently working on. However sometimes one want to limit the
 * number of nested task, this is what is determined by the depth.
 * 
 * @version 1.0
 * 
 */
public interface Task extends Serializable {
  /**
   * @return The id of the task
   */
  String getId();
  
  /**
   * Realiza las acciones necesarias pervias a la ejecucion del Task y determina y este debe ejecutarse o no.
   * 
   * @return boolean Indica si el Task debe ejecutarse (en caso negativo, no se registra ningun error, simplemente se descarta este Task)
   * @throws InvalidTaskException
   */
  boolean prepare() throws InvalidTaskException;
  
  /**
   * Invocado luego de la ejecucion del Task (solo si no se disparó ninguna Exception). 
   * Realiza las acciones necesarias posteriores a la ejecucion del Task (como verificacion/actualizacion de estados, 
   * liberacion de recursos, etc).
   */
  void close() ;
}
