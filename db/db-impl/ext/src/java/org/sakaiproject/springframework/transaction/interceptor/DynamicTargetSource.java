/*
 * *********************************************************************************
 *  $URL: https://source.etudes.org/svn/etudes/source/trunk/db/db-impl/ext/src/java/org/sakaiproject/springframework/transaction/interceptor/DynamicTargetSource.java $
 *  $Id: DynamicTargetSource.java 3 2008-10-20 18:44:42Z ggolden $
 * **********************************************************************************
 *
 *  Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 *  Licensed under the Educational Community License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ecl1.php
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *********************************************************************************
 *
 */

package org.sakaiproject.springframework.transaction.interceptor;

import org.springframework.aop.TargetSource;

/**
 * Created by IntelliJ IDEA.
 * User: John Ellis
 * Date: May 23, 2006
 * Time: 12:52:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class DynamicTargetSource implements TargetSource {

   private Object target;

   public DynamicTargetSource(Object target) {
      this.target = target;
   }

   public Class getTargetClass() {
      return target.getClass();
   }

   public boolean isStatic() {
      return false;
   }

   public Object getTarget() throws Exception {
      return target;
   }

   public void releaseTarget(Object target) throws Exception {
      // no need... is a singleton
   }

   public void setTarget(Object target) {
      this.target = target;
   }

}
