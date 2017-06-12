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

package uk.ac.lancs.e_science.sakaiproject.api.blogger.post;


public class LinkRule extends PostElement{
	private String linkExpression = null;
	private String description = null;

	
	public LinkRule(){
		
	}
	
	public LinkRule(String description, String linkExpression){
		this.description = description;
		this.linkExpression = linkExpression;
	}
	public void setDescription(String description){
		this.description = description;
	}
	public String getDescription(){
		return this.description;
	}
	public void setLinkExpression(String linkExpression){
		this.linkExpression = linkExpression;
	}
	public String getLinkExpression(){
		return linkExpression;
	}
}
