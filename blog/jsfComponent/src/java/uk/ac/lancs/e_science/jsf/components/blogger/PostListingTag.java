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
package uk.ac.lancs.e_science.jsf.components.blogger;

import javax.faces.application.Application;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;

import com.sun.faces.util.ConstantMethodBinding;
import com.sun.faces.util.Util;

public class PostListingTag extends UIComponentTag {

	private String postListing;
	private String action;
	/**
	 * @return the symbolic name of the component type. We will define the clas for this tape latter in the faces.config file
	 */
	public String getComponentType() {
		return "uk.ac.lancs.e_science.jsf.components.blogger.PostListing";
	}
	/**
	 * @return the symbolic name of the renderer. If null, it means that the renderer name is not defined and the component will render it by itself
	 */
	public String getRendererType() {
		// null means the component renders itself
		return null;
	}
	/**
	 * This method releases any resources allocated during the execution of this tag handler. 
	 */
	public void realease(){
		//the super class method should be called
		super.release();
		postListing = null;
		action = null;
	}
	
	/**
	 * this method is used to pass attributes taken fron the JSP page to the renderer. You can use
	 * the JSF EL in the value for the tag attribute.
	 */
	protected void setProperties(UIComponent component){
		//the super classs method should be called
		super.setProperties(component);
		UICommand command = (UICommand)component;

		FacesContext context = FacesContext.getCurrentInstance();
		Application app = context.getApplication();
		
		if (postListing!=null){
			if (isValueReference(postListing)){
				ValueBinding vb = app.createValueBinding(postListing);
				component.setValueBinding("postListing",vb);
			}else{
				component.getAttributes().put("postListing",postListing);
			}
		}
	    if (action != null) {
	    	MethodBinding actionBinding = null;
	    	if (isValueReference(action)){
	    		actionBinding = app.createMethodBinding(action,null);
	    	} else {
	    		actionBinding = new ConstantMethodBinding(action);
	    	}
	    	command.setAction(actionBinding);
	    		
	    }

	}
	
	public void setPostListing(String value){
		this.postListing = value;
	}
	
	public String getPostListing(){
		return postListing;
	}
	public void setAction(String action) {
	    this.action = action;
	}
}
