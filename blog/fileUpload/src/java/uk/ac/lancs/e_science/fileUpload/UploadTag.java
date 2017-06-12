/*************************************************************************************
 Copyright (c) 2006. Centre for e-Science. Lancaster University. United Kingdom.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 *************************************************************************************/
package uk.ac.lancs.e_science.fileUpload;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;


public class UploadTag extends UIComponentTag { 
   private String value;
   private String target;
   
   public void setValue(String newValue) { value = newValue; }
   public void setTarget(String newValue) { target = newValue; } 
   
   public void setProperties(UIComponent component) { 
      super.setProperties(component); 
      uk.ac.lancs.e_science.fileUpload.util.Tags.setString(component, "target", target);
      uk.ac.lancs.e_science.fileUpload.util.Tags.setString(component, "value", value);
   } 

   public void release() {
      super.release();
      value = null;
      target = null;
   }
   
   public String getRendererType() { return "uk.ac.lancs.e_science.fileUpload.Upload"; } 
   public String getComponentType() {return "uk.ac.lancs.e_science.fileUpload.Upload"; }  
}
