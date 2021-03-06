/**********************************************************************************
*
* $Id: ExportBean.java 10039 2015-02-06 01:29:15Z mallikamt $
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.gradebook.AbstractGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.Gradebook;


/**
 * Backing bean to export gradebook data. Currently we support two export
 * formats (CSV or Excel) and export two collections of data (all assignment
 * scores or all course grades).
 */
public class ExportBean extends GradebookDependentBean implements Serializable {
	private static final Log logger = LogFactory.getLog(ExportBean.class);

	// Transient pointers to business data, for easier sharing of code
	// between action methods.
	private transient List enrollments;
	private transient List gradableObjects;
	private transient Map scoresMap;	// May be course grades instead
	private boolean dropGradeDisplayed; 
	protected Gradebook gradebook;


	public Gradebook getGradebook()
	{
		return super.getGradebook();
	}
	/**
	 * Exports the roster via http as a csv document
	 *
	 * @param event The calling JSF event
	 */
	public void exportRosterCsv(ActionEvent event) {
        String filePrefix = getLocalizedString("export_gradebook_prefix");
        initScoresMap(false);
		writeAsCsv(getAsCsv(true), getFileName(filePrefix));
	}

    public void exportCourseGradeCsv(ActionEvent event) {
    //    if(logger.isInfoEnabled()) logger.info("exporting course grade as csv for gradebook " + getGradebookUid());
        String filePrefix = getLocalizedString("export_course_grade_prefix");
        initScoresMap(true);
        writeAsCsv(getAsCsv(false), getFileName(filePrefix));
    }

	public void exportRosterExcel(ActionEvent event) {
        String filePrefix = getLocalizedString("export_gradebook_prefix");
        initScoresMap(false);
		writeAsExcel(getAsExcel(true), getFileName(filePrefix));
	}

    public void exportCourseGradeExcel(ActionEvent event) {
      //  if(logger.isInfoEnabled()) logger.info("exporting course grade as excel for gradebook " + getGradebookUid());
        String filePrefix = getLocalizedString("export_course_grade_prefix");
        initScoresMap(true);
        writeAsExcel(getAsExcel(false), getFileName(filePrefix));
    }

	private void initScoresMap(boolean isCourseGrade) {
        if (isCourseGrade) {
        	gradableObjects = new ArrayList();
        } else {
        	gradableObjects = getGradebookManager().getAssignments(getGradebookId());
        }
		CourseGrade courseGrade = getGradebookManager().getCourseGradeWithStats(getGradebookId());
		gradableObjects.add(0, courseGrade);

		List avEnrollments = getAvailableEnrollments();
		
		if (avEnrollments != null && avEnrollments.size() > 0)
		{
			try
			{
				Site site = SiteService.getSite(getGradebookUid());
				Collection groups = site.getGroups();
				List<org.sakaiproject.site.api.Group> siteGroups = new ArrayList();
				for (Object groupO : groups)
				{
					org.sakaiproject.site.api.Group g = (org.sakaiproject.site.api.Group) groupO;

					// skip non-section groups
					if (g.getProperties().getProperty("sections_category") != null) siteGroups.add(g);

				}
				enrollments = new ArrayList();
				for (Iterator iter = avEnrollments.iterator(); iter.hasNext();)
				{
					EnrollmentRecord enrollRec = (EnrollmentRecord) iter.next();
					EnrollSectionRecord enrollment = new EnrollSectionRecord();
					enrollment.setSection(enrollRec, siteGroups);
					enrollments.add(enrollment);
				}
			}
			catch (IdUnusedException e)
			{
				logger.error(e);
			}
		}

		Set studentUids = new HashSet();
		for(Iterator iter = enrollments.iterator(); iter.hasNext();) {
			EnrollSectionRecord enr = (EnrollSectionRecord)iter.next();
			studentUids.add(((EnrollmentRecord)enr.getEnrollRec()).getUser().getUserUid());
		}

		List gradeRecords;
		if (isCourseGrade) {
			gradeRecords = getGradebookManager().getPointsEarnedSortedGradeRecords(courseGrade, studentUids);
		} else {
			gradeRecords = getGradebookManager().getPointsEarnedSortedAllGradeRecords(getGradebookId(), studentUids);
		}

		scoresMap = new HashMap();
		for (Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) {
			AbstractGradeRecord gradeRecord = (AbstractGradeRecord)iter.next();
			String studentUid = gradeRecord.getStudentId();
			Map studentMap = (Map)scoresMap.get(studentUid);
			if (studentMap == null) {
				studentMap = new HashMap();
				scoresMap.put(studentUid, studentMap);
			}
            studentMap.put(gradeRecord.getGradableObject().getId(), gradeRecord);
		}
		gradebook = getGradebook();
		dropGradeDisplayed = gradebook.isDropGradeDisplayed();
		if (!isCourseGrade && dropGradeDisplayed) flagDroppedEntries(scoresMap);
	}
	
	private void writeAsCsv(String csvString, String fileName) {
		FacesContext faces = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse)faces.getExternalContext().getResponse();
		protectAgainstInstantDeletion(response);
		response.setContentType("text/comma-separated-values");
		response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".csv");
		response.setContentLength(csvString.length());
		OutputStream out = null;
		try {
			out = response.getOutputStream();
			out.write(csvString.getBytes());
			out.flush();
		} catch (IOException e) {
			logger.error(e);
			e.printStackTrace();
		} finally {
			try {
				if (out != null) out.close();
			} catch (IOException e) {
				logger.error(e);
				e.printStackTrace();
			}
		}
		faces.responseComplete();
	}

    private void writeAsExcel(HSSFWorkbook wb, String fileName) {
		FacesContext faces = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse)faces.getExternalContext().getResponse();
		protectAgainstInstantDeletion(response);
		response.setContentType("application/vnd.ms-excel ");
		response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");

		OutputStream out = null;
		try {
			out = response.getOutputStream();
			// For some reason, you can't write the byte[] as in the csv export.
			// You need to write directly to the output stream from the workbook.
			wb.write(out);
			out.flush();
		} catch (IOException e) {
			logger.error(e);
			e.printStackTrace();
		} finally {
			try {
				if (out != null) out.close();
			} catch (IOException e) {
				logger.error(e);
				e.printStackTrace();
			}
		}
		faces.responseComplete();
	}

	/**
	 * Constructs an excel workbook document representing the roster
	 *
     * @param gradableObjects The list of gradable objects to include in the
     * spreadsheet
     * @param showCourseGradeAsPoints Whether to display the course grade records
     * as point totals or letter grades
	 * @return The excel workbook
	 */
	private HSSFWorkbook getAsExcel(boolean showCourseGradeAsPoints) {
		// Get the active gradebook
        Gradebook gradebook = getGradebook();

		HSSFWorkbook wb = new HSSFWorkbook();
        // Excel can not handle sheet names with > 31 characters, and can't handle /\*?[] characters
        String safeGradebookName = StringUtils.left(gradebook.getName().replaceAll("\\W", "_"), 30);

		HSSFSheet sheet = wb.createSheet(safeGradebookName);
		HSSFRow headerRow = sheet.createRow((short)0);

		// Add the column headers
        headerRow.createCell((short)(0)).setCellValue(getLocalizedString("export_student_id"));
        headerRow.createCell((short)(1)).setCellValue(getLocalizedString("export_student_name"));
        headerRow.createCell((short)(2)).setCellValue(getLocalizedString("export_section_id"));

		for(short i=0; i < gradableObjects.size(); i++) {
			GradableObject go = (GradableObject)gradableObjects.get(i);
            String header;
            if(go.isCourseGrade()) {
                if(showCourseGradeAsPoints) {
                    header = getLocalizedString("roster_course_grade_column_name");
                } else {
                    header = getLocalizedString("course_grade_details_course_grade_column_name");
                }
            } else {
                header = go.getName();
            }
			headerRow.createCell((short)(i+3)).setCellValue(header); // Skip the first two columns
		}

		// Fill the spreadsheet cells
        Collections.sort(enrollments, EnrollmentTableBean.ENROLLMENT_NAME_COMPARATOR);
		for(Iterator enrollmentIter = enrollments.iterator(); enrollmentIter.hasNext();) {
			EnrollSectionRecord enr = (EnrollSectionRecord)enrollmentIter.next();
			Map studentMap = (Map)scoresMap.get(((EnrollmentRecord)enr.getEnrollRec()).getUser().getUserUid());
			HSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
            row.createCell((short)0).setCellValue(((EnrollmentRecord)enr.getEnrollRec()).getUser().getDisplayId());
            row.createCell((short)1).setCellValue(((EnrollmentRecord)enr.getEnrollRec()).getUser().getSortName());
            row.createCell((short)2).setCellValue(enr.getSectionTitle());
			
            for(short j=0; j < gradableObjects.size(); j++) {
				GradableObject go = (GradableObject)gradableObjects.get(j);
				HSSFCell cell = row.createCell((short)(j+3));
				Object agrValue = (studentMap != null) ? studentMap.get(go.getId()) : null;
				if(agrValue != null) {
					Object columnValue = null;
					if (!showCourseGradeAsPoints) {
						if (!gradebook.isDropGradeDisplayed()) columnValue = ((CourseGradeRecord)agrValue).getExportGrade();
						else columnValue = ((CourseGradeRecord)agrValue).getDisplayDropGrade();
						cell.setCellValue(columnValue.toString());
					} else {
						columnValue = ((AbstractGradeRecord)agrValue).getPointsEarned();
						if (columnValue != null)
						{
							if (columnValue instanceof Double)
							{
								if (((AbstractGradeRecord) agrValue).isDroppedEntry())
									cell.setCellValue("*" + ((Double) columnValue).doubleValue() + "*");
								else
									cell.setCellValue(((Double) columnValue).doubleValue());
							}
							else
							{
								if (((AbstractGradeRecord) agrValue).isDroppedEntry())
									cell.setCellValue("*" + columnValue.toString() + "*");
								else
									cell.setCellValue(columnValue.toString());
							}
						}
					}
				}
			}
		}


        return wb;
	}

	/**
	 * Constructs a string representing the roster as a comma-separated-values
	 * document.
     *
     * @param gradableObjects The list of gradable objects to include in the
     * spreadsheet
     * @param showCourseGradeAsPoints Whether to display the course grade records
     * as point totals or letter grades
	 * @return The csv document
	 */
	private String getAsCsv(boolean showCourseGradeAsPoints) {
		Gradebook gradebook = getGradebook();
        StringBuffer sb = new StringBuffer();

        // Add the headers
        sb.append(getLocalizedString("export_student_id"));
        sb.append(",");
        sb.append(getLocalizedString("export_student_name"));
        sb.append(",");
        sb.append(getLocalizedString("export_section_id"));
        sb.append(",");

        for(Iterator goIter = gradableObjects.iterator(); goIter.hasNext();) {
            GradableObject go = (GradableObject)goIter.next();
            String header;
            if(go.isCourseGrade()) {
                if(showCourseGradeAsPoints) {
                    header = getLocalizedString("roster_course_grade_column_name");
                } else {
                    header = getLocalizedString("course_grade_details_course_grade_column_name");
                }
            } else {
                header = go.getName();
            }
			appendQuoted(sb, header);
			if(goIter.hasNext()) {
				sb.append(",");
			} else {
				sb.append("\n");
			}
		}
		// Add the data
        Collections.sort(enrollments, EnrollmentTableBean.ENROLLMENT_NAME_COMPARATOR);

        for(Iterator enrIter = enrollments.iterator(); enrIter.hasNext();) {
            EnrollSectionRecord enr = (EnrollSectionRecord)enrIter.next();
            Map studentMap = (Map)scoresMap.get(((EnrollmentRecord)enr.getEnrollRec()).getUser().getUserUid());
            if (studentMap == null) {
                studentMap = new HashMap();
            }
            appendQuoted(sb, ((EnrollmentRecord)enr.getEnrollRec()).getUser().getDisplayId());
            sb.append(",");
            appendQuoted(sb, ((EnrollmentRecord)enr.getEnrollRec()).getUser().getSortName());
            sb.append(",");
            if (enr.getSectionTitle() != null) appendQuoted(sb, enr.getSectionTitle());
            else appendQuoted(sb, "");
            sb.append(",");


            for(Iterator goIter = gradableObjects.iterator(); goIter.hasNext();) {
                GradableObject go = (GradableObject)goIter.next();
                if(logger.isDebugEnabled()) logger.debug("userUid=" + ((EnrollmentRecord)enr.getEnrollRec()).getUser().getUserUid() + ", go=" + go + ", studentMap=" + studentMap);
                /*Object cellValue = (studentMap != null) ? studentMap.get(go.getId()) : null;
                if(cellValue != null) {
                    sb.append(cellValue);
                }*/
                Object agrValue = (studentMap != null) ? studentMap.get(go.getId()) : null;
				if(agrValue != null) {
					Object columnValue = null;
					if (!showCourseGradeAsPoints) {
						if (!gradebook.isDropGradeDisplayed()) columnValue = ((CourseGradeRecord)agrValue).getExportGrade();
						else columnValue = ((CourseGradeRecord)agrValue).getDisplayDropGrade();
						sb.append(columnValue.toString());
					} else {
						columnValue = ((AbstractGradeRecord)agrValue).getPointsEarned();
						if (columnValue != null)
						{
							if (columnValue instanceof Double)
							{
								if (((AbstractGradeRecord) agrValue).isDroppedEntry())
									sb.append("*" + ((Double) columnValue).doubleValue() + "*");
								else
									sb.append(((Double) columnValue).doubleValue());
							}
							else
							{
								if (((AbstractGradeRecord) agrValue).isDroppedEntry())
									sb.append("*" + columnValue.toString() + "*");
								else
									sb.append(columnValue.toString());

							}
						}
                }
				}	
                if(goIter.hasNext()) {
					sb.append(",");
				}
			}
			sb.append("\n");
		}

        if(logger.isDebugEnabled())logger.debug(sb.toString());
        return sb.toString();
	}

	/**
     * Gets the filename for the export
     *
	 * @param gradebookExport Whether the filename is for a whole gradebook export
	 * @return The appropriate filename for the export
	 */
    private String getFileName(String prefix) {
		Date now = new Date();
		DateFormat df = new SimpleDateFormat(getLocalizedString("export_filename_date_format"));
		StringBuffer fileName = new StringBuffer(prefix);
        String gbName = getGradebook().getName();
        if(StringUtils.trimToNull(gbName) != null) {
            gbName = gbName.replaceAll("\\s", "_"); // replace whitespace with '_'
            fileName.append("-");
            fileName.append(gbName);
        }
		fileName.append("-");
		fileName.append(df.format(now));
		return fileName.toString();
	}

	private StringBuffer appendQuoted(StringBuffer sb, String toQuote) {
		if (toQuote != null)
		{
			if ((toQuote.indexOf(',') >= 0) || (toQuote.indexOf('"') >= 0))
			{
				String out = toQuote.replaceAll("\"", "\"\"");
				if (logger.isDebugEnabled()) logger.debug("Turning '" + toQuote + "' to '" + out + "'");
				sb.append("\"").append(out).append("\"");
			}
			else
			{
				sb.append(toQuote);
			}
		}
		else
		{
			sb.append(toQuote);
		}
		return sb;
	}

    /**
     * Try to head off a problem with downloading files from a secure HTTPS
     * connection to Internet Explorer.
     *
     * When IE sees it's talking to a secure server, it decides to treat all hints
     * or instructions about caching as strictly as possible. Immediately upon
     * finishing the download, it throws the data away.
     *
     * Unfortunately, the way IE sends a downloaded file on to a helper
     * application is to use the cached copy. Having just deleted the file,
     * it naturally isn't able to find it in the cache. Whereupon it delivers
     * a very misleading error message like:
     * "Internet Explorer cannot download roster from sakai.yoursite.edu.
     * Internet Explorer was not able to open this Internet site. The requested
     * site is either unavailable or cannot be found. Please try again later."
     *
     * There are several ways to turn caching off, and so to be safe we use
     * several ways to turn it back on again.
     *
     * This current workaround should let IE users save the files to disk.
     * Unfortunately, errors may still occur if a user attempts to open the
     * file directly in a helper application from a secure web server.
     *
     * TODO Keep checking on the status of this.
     */
    public static void protectAgainstInstantDeletion(HttpServletResponse response) {
    	response.reset();	// Eliminate the added-on stuff
    	response.setHeader("Pragma", "public");	// Override old-style cache control
    	response.setHeader("Cache-Control", "public, must-revalidate, post-check=0, pre-check=0, max-age=0");	// New-style
    }
}
