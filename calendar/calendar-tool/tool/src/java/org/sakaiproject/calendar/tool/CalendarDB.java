/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/calendar/calendar-tool/tool/src/java/org/sakaiproject/calendar/tool/CalendarDB.java $
 * $Id: CalendarDB.java 3 2008-10-20 18:44:42Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.calendar.tool;

//import java.util.Enumeration;
import java.util.Hashtable;


public class CalendarDB {
    

  private Hashtable events;
  private static final String[] time = {"8:00 am", "9:00 am", "10:00 am", "11:00 am", "12:00 pm", 
					"1:00 pm", "2:00 pm", "3:00 pm", "4:00 pm", "5:00 pm", "6:00 pm",
					"7:00 pm", "8:00 pm" };
  public static final int rows = 12;
  
  
// Event class  
  
  public class Event {

  String hour;
  String description;


  public Event (String hourx) {
    hour = hourx;
    description = "";

  }

  public String getHour () {
    return hour;
  }


  public String getDescription () {
    if (description.equals("")) return "";
    else return description;
  }

  public void setDescription (String descr) {
    description = descr;
  }
 
}

// End of event class 

  public CalendarDB () {   
   events = new Hashtable (rows);
   for (int i=0; i < rows; i++) {
     events.put (time[i], new Event(time[i]));
   }
  }

  public int getRows () {
    return rows;
  }

  public Event getEvent (int index) {
    return (Event)events.get(time[index]);
  }

  public int getIndex (String tm) {
    for (int i=0; i<rows; i++)
      if(tm.equals(time[i])) return i;
    return -1;
  }

  public void addEvent (String tm, String desc) {
    int index = getIndex (tm);
    if (index >= 0) {
      String descr = desc;
      Event e = (Event)events.get(time[index]);
      e.setDescription (descr);
    }
  }

}



