/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/jsf/example/src/java/example/ExampleBean.java $
 * $Id: ExampleBean.java 3 2008-10-20 18:44:42Z ggolden $
 **********************************************************************************
 *
* Copyright (c) 2003, 2004 The Sakai Foundation.
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
 ******************************************************************************/

package example;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PhaseId;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.fileupload.FileItem;

public class ExampleBean implements Serializable
{
    private static List list;

    static
    {
        // makes a whole bunch of backing beans with enough properties to
        // demo/test most anything.
        list = new ArrayList();
        String[] lastNames = { "Black", "Chang", "Dumbledore", "Granger", "McGonagle",
                "Potter", "Snape", "Weasley", };

        String[] firstNames = { "Alice", "Bruce", "Carrie", "David", "Elmer", "Fresia" };

        String[] actions = { "Accio", "Aparecium", "Avis", "Colloportus", "Deletrius",
                "Densaugeo", "Diffindo", "Engorgio", "Ennervat", "Expecto atronum",
                "Expelliamus", "Evanesco", "Ferula", "Finite Icantatum", "Flagrate",
                "Flipendo", "Furnunculus", "Immobulus", "Impedimenta", "Impervius",
                "Incarcerous", "Incendio", "Legilimens", "Locomotor Mortis", "Lumos",
                "Mobiliarbus", "Mobilicorpus", "Morsmordre", "Nox", "Obliviate ",
                "Orchideous", "Portus", "Protego", "Quietus", "Reducio", "Reducto",
                "Relashio", "Reparo", "Rictusempra", "Riddikulus", "Scourgify",
                "Serpensortia", "Silencio", "Sonorus", "Stupefy", "Tarantallegra",
                "Waddiwasi", "Wingardium Leviosa", };
        String[] classes = { "Mag. Creat.", "Herbol.", "Proph", "Transfig", "D. Arts" };

        int alen = actions.length;
        int acount = 0;

        for (int ilast = 0; ilast < lastNames.length; ilast++)
        {
            for (int ifirst = 0; ifirst < firstNames.length; ifirst++)
            {
                for (char c = 'A'; c < 'A' + 6; c++)
                {
                    Wizard bean = new Wizard();
                    bean.setFirst(firstNames[ifirst] + " " + c + ".");
                    bean.setLast(lastNames[ilast]);
                    bean.setAddress(("" + (ilast + ifirst + c)) + " Privet Drive");
                    bean.setId(("" + Math.random()).substring(2));
                    List grades = new ArrayList();

                    for (int i = 0; i < classes.length; i++)
                    {
                        Grade grade = new Grade();
                        grade.setName(classes[i]);
                        grade.setScore(("" + Math.random()).substring(2, 4));
                        grades.add(grade);

                    }
                    bean.setGrades(grades);
                    if (acount == alen)
                    {
                        acount = 0;
                    }
                    bean.setText(actions[acount++]);

                    list.add(bean);
                }
            }
        }

    }

    public List getList()
    {
        return list;
    }

    public String handleAction()
    {
        return "myaction";
    }

    //  public static void main(String args[])
    //  {
    //    System.out.println("testing ");
    //    unitTest();
    //  }

    private static void unitTest()
    {
        ExampleBean eb = new ExampleBean();
        List alist = eb.getList();
        for (int i = 0; i < alist.size(); i++)
        {
            Wizard w = (Wizard) alist.get(i);
            System.out.println("-----------------------------------------------");
            System.out.println("w.getFirst()=" + w.getFirst());
            System.out.println("w.getLast()=" + w.getLast());
            System.out.println("w.getAddress()=" + w.getAddress());
            System.out.println("w.getId()=" + w.getId());
            System.out.println("w.getText()=" + w.getText());
            System.out.print("grades: ");
            List glist = w.getGrades();
            for (int j = 0; j < glist.size(); j++)
            {
                Grade g = (Grade) glist.get(j);
                System.out.print("g.getName()=" + g.getName() + " g.getScore()="
                        + g.getScore() + "; ");
            }

            System.out.println("");
        }
    }

     public void processFileUpload(ValueChangeEvent event)
            throws AbortProcessingException
    {
        UIComponent component = event.getComponent();
        Object newValue = event.getNewValue();
        Object oldValue = event.getOldValue();
        PhaseId phaseId = event.getPhaseId();
        Object source = event.getSource();
        System.out.println("processFileUpload() event: " + event + " component: "
                + component + " newValue: " + newValue + " oldValue: " + oldValue
                + " phaseId: " + phaseId + " source: " + source);

        if (newValue instanceof String) return;
        if (newValue == null) return;

        // must be a FileItem
        try
        {
            FileItem item = (FileItem) event.getNewValue();
	        String fieldName = item.getFieldName();
	        String fileName = item.getName();
	        long fileSize = item.getSize();
	        System.out.println("processFileUpload(): item: " + item + " fieldname: " + fieldName + " filename: " + fileName + " length: " + fileSize);

	        // Read the file as a stream (may be more memory-efficient)
	        InputStream fileAsStream = item.getInputStream();

	        // Read the contents as a byte array
	        //byte[] fileContents = item.get();

	        // now process the file.  Do application-specific processing
	        // such as parsing the file, storing it in the database,
	        // or whatever needs to happen with the uploaded file.
        }
        catch (Exception ex)
        {
            // handle exception
        }

    }

}


