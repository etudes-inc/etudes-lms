/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/login/login-tool/tool/src/java/org/sakaiproject/login/tool/LoginTool.java $
 * $Id: LoginTool.java 7522 2014-03-01 22:04:24Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.login.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.AuthenticationException;
import org.sakaiproject.user.api.AuthenticationMultipleException;
import org.sakaiproject.user.api.Evidence;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.AuthenticationManager;
import org.sakaiproject.util.IdPwEvidence;
import org.sakaiproject.util.IdPwInstEvidence;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * <p>
 * Login tool for Sakai. Works with the ContainerLoginTool servlet to offer container or internal login.
 * </p>
 * <p>
 * This "tool", being login, is not placed, instead each user can interact with only one login at a time. The Sakai Session is used for attributes.
 * </p>
 */
@SuppressWarnings("serial")
public class LoginTool extends HttpServlet
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(LoginTool.class);

	/** Session attribute used to store a message between steps. */
	protected static final String ATTR_MSG = "sakai.login.message";

	/** Session attribute set and shared with ContainerLoginTool: URL for redirecting back here. */
	public static final String ATTR_RETURN_URL = "sakai.login.return.url";

	/** Session attribute set and shared with ContainerLoginTool: if set we have failed container and need to check internal. */
	public static final String ATTR_CONTAINER_CHECKED = "sakai.login.container.checked";

	/** The name of the cookie we use to keep sakai session. */
	public static final String SESSION_COOKIE = "JSESSIONID";

	class NameAndCode
	{
		String name;
		String code;
		public NameAndCode(String name, String code)
		{
			this.name = name;
			this.code = code;
		}
	}

	/** The name of the system property that will be used when setting the value of the session cookie. */
	protected static final String SAKAI_SERVERID = "sakai.serverId";

	private static ResourceLoader rb = new ResourceLoader("auth");

	/**
	 * Access the Servlet's information display.
	 * 
	 * @return servlet information.
	 */
	public String getServletInfo()
	{
		return "Sakai Login";
	}

	/**
	 * Initialize the servlet.
	 * 
	 * @param config
	 *        The servlet config.
	 * @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		M_log.info("init()");
	}

	/**
	 * Shutdown the servlet.
	 */
	public void destroy()
	{
		M_log.info("destroy()");

		super.destroy();
	}

	/**
	 * Respond to requests.
	 * 
	 * @param req
	 *        The servlet request.
	 * @param res
	 *        The servlet response.
	 * @throws ServletException.
	 * @throws IOException.
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		// get the session
		Session session = SessionManager.getCurrentSession();

		// get my tool registration
		Tool tool = (Tool) req.getAttribute(Tool.TOOL);

		// recognize what to do from the path
		String option = req.getPathInfo();

		// maybe we don't want to do the container this time
		boolean skipContainer = false;

		// perhaps we are doing the full authentication
		boolean fullAuthentication = false;

		// if missing, set it to "/login"
		if ((option == null) || ("/".equals(option)))
		{
			option = "/login";
		}

		// look for the extreme login (i.e. to skip container checks)
		else if ("/xlogin".equals(option))
		{
			option = "/login";
			skipContainer = true;
		}
		else if ("/zlogin".equals(option))
		{
			option = "/login";
			skipContainer = true;
			fullAuthentication = true;
		}

		// get the parts (the first will be "", second will be "login" or "logout")
		String[] parts = option.split("/");

		if (parts[1].equals("logout"))
		{
			// get the session info complete needs, since the logout will invalidate and clear the session
			String returnUrl = (String) session.getAttribute(Tool.HELPER_DONE_URL);
			if (returnUrl == null) returnUrl = (String) ThreadLocalManager.get(Tool.HELPER_DONE_URL);
	
			// mark the usage session as user-ended
			UsageSession usageSession = UsageSessionService.getSession();
			if (usageSession != null)
			{
				// TODO: add setEndedByUser() to UsageSession to communicate this -GGOLDEN
				// usageSession.setEndedByUser();
				ThreadLocalManager.set("LoginTool:userLogout", "true");
			}

			// logout the user
			UsageSessionService.logout();

			complete(returnUrl, null, tool, res);
			return;
		}
		else
		{
			// see if we need to check container
			boolean checkContainer = ServerConfigurationService.getBoolean("container.login", false);
			if (checkContainer && !skipContainer)
			{
				// if we have not checked the container yet, check it now
				if (session.getAttribute(ATTR_CONTAINER_CHECKED) == null)
				{
					// save our return path
					session.setAttribute(ATTR_RETURN_URL, Web.returnUrl(req, null));

					String containerCheckPath = this.getServletConfig().getInitParameter("container");
					String containerCheckUrl = Web.serverUrl(req) + containerCheckPath;

					// support query parms in url for container auth
					String queryString = req.getQueryString();
					if (queryString != null) containerCheckUrl = containerCheckUrl + "?" + queryString;

					res.sendRedirect(res.encodeRedirectURL(containerCheckUrl));
					return;
				}
			}

			// get the session info complete needs, since the logout will invalidate and clear the session
			String returnUrl = (String) ThreadLocalManager.get(Tool.HELPER_DONE_URL);
			if (returnUrl == null) returnUrl = (String) session.getAttribute(Tool.HELPER_DONE_URL);

			String msg = req.getParameter("msg");
			if (returnUrl == null) returnUrl = req.getParameter("final");

			// send the form
			sendForm(req, res, fullAuthentication, returnUrl, msg);
		}
	}

	/**
	 * Send the login form
	 * 
	 * @param req
	 *        Servlet request.
	 * @param res
	 *        Servlet response.
	 * @throws IOException
	 */
	protected void sendForm(HttpServletRequest req, HttpServletResponse res, boolean fullAuthentication, String finalReturnUrl, String msgCode) throws IOException
	{
		final String headHtml = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
				+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">"
				+ "  <head>"
				+ "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"
				+ "    <link href=\"SKIN_ROOT/tool_base.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />"
				+ "    <link href=\"SKIN_ROOT/DEFAULT_SKIN/tool.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />"
				+ "    <meta http-equiv=\"Content-Style-Type\" content=\"text/css\" />"
				+ "    <title>UI.SERVICE</title>"
				+ "    <script type=\"text/javascript\" language=\"JavaScript\" src=\"/library/js/headscripts.js\"></script>"
				+ "  </head>"
				+ "  <body onload=\" setFocus(focus_path);parent.updCourier(doubleDeep, ignoreCourier);\">"
				+ "<script type=\"text/javascript\" language=\"JavaScript\">" + "  focus_path = [\"eid\"];" + "</script>";

		final String tailHtml = "</body></html>";

		final String loginHtml = "<table class=\"login\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" summary=\"layout\">" + "		<tr>"
				+ "			<th colspan=\"2\">" + "				Login Required" + "			</th>" + "		</tr>" + "		<tr>" + "			<td class=\"logo\">" + "			</td>"
				+ "			<td class=\"form\">" + "				<form method=\"post\" action=\"ACTION\" enctype=\"application/x-www-form-urlencoded\">"
				+ "                                        MSG" + "							<table border=\"0\" class=\"loginform\" summary=\"layout\">"
				+ "								<tr>" + "									<td>" + "										<label for=\"eid\">EID</label>" + "									</td>" + "									<td>"
				+ "										<input name=\"eid\" id=\"eid\"  type=\"text\"/>" + "									</td>" + "								</tr>" + "								<tr>" + "									<td>"
				+ "										<label for=\"pw\">PW</label>" + "									</td>" + "									<td>"
				+ "										<input name=\"pw\" id=\"pw\"  type=\"password\"/>" + "									</td>" + "								</tr>" + "								<tr>"
				+ "									<td colspan=\"2\">" + "										<input name=\"submit\" type=\"submit\" id=\"submit\" value=\"LoginSubmit\"/>"
				+ "									</td>" + "								</tr>" + "							</table>"
				+ "                        <input name=\"final\" type=\"hidden\" value=\"FINAL\" />"
				+ "						</form>" + "					</td>" + "				</tr>" + "			" + "RESETLINK</table>";

		final String login2Html = "<table class=\"login\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" summary=\"layout\">           "
				+ "		<tr>                                                                                                             "
				+ "			<th colspan=\"2\">                                                                                           "
				+ "				Authentication Required                                                                                  "
				+ "			</th>                                                                                                        "
				+ "		</tr>                                                                                                            "
				+ "		<tr>                                                                                                             "
				+ "			<td class=\"logo\">                                                                                          "
				+ "			</td>                                                                                                        "
				+ "			<td class=\"form\">                                                                                          "
				+ "				<form method=\"post\" action=\"ACTION\" enctype=\"application/x-www-form-urlencoded\">                   "
				+ "                                        MSG                                                                           "
				+ "							<table border=\"0\" class=\"loginform\" summary=\"layout\">                                  "
				+ "								<tr>                                                                                     "
				+ "									<td>                                                                                 "
				+ "										<label for=\"inst\">INST</label>                                                 "
				+ "									</td>                                                                                "
				+ "									<td>                                                                                 "
				+ "										<select name=\"inst\" id=\"inst\">                                               "
				+ "                                         ICODEOPTIONS                                                                  "
				+ "                                     </select>                                                                        "
				+ "									</td>                                                                                "
				+ "								</tr>                                                                                    "
				+ "								<tr>                                                                                     "
				+ "									<td>                                                                                 "
				+ "										<label for=\"iid\">IID</label>                                                   "
				+ "									</td>                                                                                "
				+ "									<td>                                                                                 "
				+ "										<input name=\"iid\" id=\"iid\"  type=\"text\"/>                                  "
				+ "									</td>                                                                                "
				+ "								</tr>                                                                                    "
				+ "								<tr>                                                                                     "
				+ "									<td>                                                                                 "
				+ "										<label for=\"pw\">PW</label>                                                     "
				+ "									</td>                                                                                "
				+ "									<td>                                                                                 "
				+ "										<input name=\"pw\" id=\"pw\"  type=\"password\"/>                                "
				+ "									</td>                                                                                "
				+ "								</tr>                                                                                    "
				+ "								<tr>                                                                                     "
				+ "									<td colspan=\"2\">                                                                   "
				+ "										<input name=\"submit\" type=\"submit\" id=\"submit\" value=\"LoginSubmit\"/>     "
				+ "									</td>                                                                                "
				+ "								</tr>                                                                                    "
				+ "							</table>                                                                                     "
				+ "                         <input name=\"final\" type=\"hidden\" value=\"FINAL\" />                                     "
				+ "						</form>                                                                                          "
				+ "					</td>                                                                                                "
				+ "				</tr>                                                                                                    "
				+ "RESETLINK</table>";

		// get the Sakai session
		Session session = SessionManager.getCurrentSession();

		// get my tool registration
		// Tool tool = (Tool) req.getAttribute(Tool.TOOL);

		// fragment or not?
		boolean fragment = Boolean.TRUE.toString().equals(req.getAttribute(Tool.FRAGMENT));

		String eidWording = rb.getString("userid");
		String pwWording = rb.getString("log.pass");
		String loginRequired = rb.getString("log.logreq");
		String loginWording = rb.getString("log.login");
		String instWording = rb.getString("log.inst");
		String iidWording = rb.getString("log.iid");
		String fullPwWording = rb.getString("log.fullpass");

		if (!fragment)
		{
			// set our response type
			res.setContentType("text/html; charset=UTF-8");
			res.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
			res.addDateHeader("Last-Modified", System.currentTimeMillis());
			res.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
			res.addHeader("Pragma", "no-cache");
		}

		String defaultSkin = ServerConfigurationService.getString("skin.default");
		String skinRoot = ServerConfigurationService.getString("skin.repo");
		String uiService = ServerConfigurationService.getString("ui.service");

		// get our response writer
		PrintWriter out = res.getWriter();

		if (!fragment)
		{
			// start our complete document
			String head = headHtml.replaceAll("DEFAULT_SKIN", defaultSkin);
			head = head.replaceAll("SKIN_ROOT", skinRoot);
			head = head.replaceAll("UI.SERVICE", uiService);
			out.println(head);
		}

		// if we are in helper mode, there might be a helper message
		if (session.getAttribute(Tool.HELPER_MESSAGE) != null)
		{
			out.println("<p>" + session.getAttribute(Tool.HELPER_MESSAGE) + "</p>");
		}

		String html = null;
		if (fullAuthentication)
		{
			html = login2Html;
		}
		else
		{
			html = loginHtml;
		}

		// add our return URL
		String returnUrl = res.encodeURL(Web.returnUrl(req, null));
		html = html.replaceAll("ACTION", res.encodeURL(returnUrl));

		// add the final return URL
		html = html.replaceAll("FINAL", finalReturnUrl == null ? "" : finalReturnUrl);

		// add our wording
		html = html.replaceAll("EID", eidWording);
		if (fullAuthentication)
		{
			html = html.replaceAll("PW", fullPwWording);
		}
		else
		{
			html = html.replaceAll("PW", pwWording);
		}
		html = html.replaceAll("Login Required", loginRequired);
		html = html.replaceAll("LoginSubmit", loginWording);
		html = html.replaceAll("INST", instWording);
		html = html.replaceAll("IID", iidWording);

		// for full, the college codes (read from config)
		if (fullAuthentication)
		{
			String[] codes = ServerConfigurationService.getStrings("etudes.roster.institution.codes");
			String[] names = ServerConfigurationService.getStrings("etudes.roster.institution.names");

			if ((codes != null) && (names != null) && (codes.length == names.length))
			{
				List<NameAndCode> nacs = new ArrayList<NameAndCode>();
				for (int i = 0; i < codes.length; i++)
				{
					// some institutions process 2 roster files, so are in here twice - the second time with the name "DUP"
					if (names[i].equals("DUP")) continue;

					NameAndCode nac = new NameAndCode(names[i], codes[i]);
					nacs.add(nac);
				}

				Collections.sort(nacs, new Comparator<NameAndCode>()
				{
					public int compare(NameAndCode arg0, NameAndCode arg1)
					{
						int rv = arg0.name.compareTo(arg1.name);
						return rv;
					}
				});

				StringBuilder buf = new StringBuilder();
				for (NameAndCode nac : nacs)
				{
					String instCodeOption = "<option value=\"" + nac.code + "\">" + nac.name + "</option>";
					buf.append(instCodeOption);
				}

				html = html.replaceAll("ICODEOPTIONS", buf.toString());
			}
		}

		// add the default skin
		html = html.replaceAll("DEFAULT_SKIN", defaultSkin);
		html = html.replaceAll("SKIN_ROOT", skinRoot);

		// write a message if present
		String msg = (String) session.getAttribute(ATTR_MSG);
		if (msg == null)
		{
			if (msgCode != null)
			{
				msg = rb.getString(msgCode);
			}
		}
		if (msg != null)
		{
			html = html.replaceAll("MSG", "<div class=\"alertMessage\">" + rb.getString("gen.alert") + " " + msg + "</div>");
			html = html.replaceAll(
					"RESETLINK",
					"<tr><td colspan=\"2\" align=\"center\"><a href=\"/portal/site/!gateway/page/b0177bb8-e4b4-49f1-00b7-186d01ab8a11\">"
							+ rb.getString("log.resetpw") + "</a> " + rb.getString("log.or") + " <a href=\"" + rb.getString("log.help.url")
							+ "\" target=\"_blank\">" + rb.getString("log.help") + "</a><br><br></td></tr>");
			session.removeAttribute(ATTR_MSG);
		}
		else
		{
			html = html.replaceAll("MSG", "");
			html = html.replaceAll("RESETLINK", "");
		}

		// write the login screen
		out.println(html);

		if (!fragment)
		{
			// close the complete document
			out.println(tailHtml);
		}
	}

	/**
	 * Respond to data posting requests.
	 * 
	 * @param req
	 *        The servlet request.
	 * @param res
	 *        The servlet response.
	 * @throws ServletException.
	 * @throws IOException.
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		// get the Sakai session
		Session session = SessionManager.getCurrentSession();

		// get my tool registration
		Tool tool = (Tool) req.getAttribute(Tool.TOOL);

		// recognize what to do from the path
		boolean fullAuthentication = false;
		String option = req.getPathInfo();
		if ((option != null) && ("/zlogin".equals(option)))
		{
			fullAuthentication = true;
		}

		// here comes the data back from the form...
		String eid = StringUtil.trimToZero(req.getParameter("eid"));
		String pw = StringUtil.trimToZero(req.getParameter("pw"));
		String inst = StringUtil.trimToZero(req.getParameter("inst"));
		String iid = StringUtil.trimToZero(req.getParameter("iid"));
		String finalUrl = StringUtil.trimToNull(req.getParameter("final"));

		String finalComponent = "";
		if (finalUrl != null)
		{
			finalComponent = "&final=" + finalUrl;
		}

		// one of these will be there, one null, depending on how the submit was done
		// String submit = req.getParameter("submit");
		String cancel = req.getParameter("cancel");

		// cancel
		if (cancel != null)
		{
			session.setAttribute(ATTR_MSG, rb.getString("log.canceled"));

			// get the session info complete needs, since the logout will invalidate and clear the session
			String returnUrl = (String) session.getAttribute(Tool.HELPER_DONE_URL);
			if (returnUrl == null) returnUrl = finalUrl;

			// TODO: send to the cancel URL, cleanup session
			complete(returnUrl, session, tool, res);
		}

		// submit
		else
		{
			// authenticate
			try
			{
				Evidence e = null;
				if (fullAuthentication)
				{
					if ((iid.length() == 0) || (pw.length() == 0) || (inst.length() == 0))
					{
						throw new AuthenticationException("missing required fields");
					}

					e = new IdPwInstEvidence(iid, pw, inst);
				}
				else
				{
					if ((eid.length() == 0) || (pw.length() == 0))
					{
						throw new AuthenticationException("missing required fields");
					}

					e = new IdPwEvidence(eid, pw);
				}

				Authentication a = AuthenticationManager.authenticate(e);

				// login the user
				if (UsageSessionService.login(a, req))
				{
					// get the session info complete needs, since the logout will invalidate and clear the session
					String returnUrl = (String) session.getAttribute(Tool.HELPER_DONE_URL);
					if (returnUrl == null) returnUrl = finalUrl;

					// store the authentication password strength in the session
					session.setAttribute("user.password.strength", a.getPasswordStrength());
					
					complete(returnUrl, session, tool, res);
				}
				else
				{
					session.setAttribute(ATTR_MSG, rb.getString("log.tryagain"));
					res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "?msg=log.tryagain" + finalComponent)));
				}
			}
			catch (AuthenticationMultipleException ex)
			{
				session.setAttribute(ATTR_MSG, rb.getString("log.multiple"));

				// respond with a redirect back here with 'zlogin'
				String returnUrl = Web.returnUrl(req, "?msg=log.multiple" + finalComponent);
				returnUrl = returnUrl.replaceAll("/xlogin", "/zlogin");
				returnUrl = returnUrl.replaceAll("/relogin", "/zlogin");
				returnUrl = returnUrl.replaceAll("/login", "/zlogin");
				res.sendRedirect(res.encodeRedirectURL(returnUrl));
			}
			catch (AuthenticationException ex)
			{
				session.setAttribute(ATTR_MSG, rb.getString("log.invalid"));

				// respond with a redirect back here
				res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "?msg=log.invalid" + finalComponent)));
			}
		}
	}

	/**
	 * Cleanup and redirect when we have a successful login / logout
	 * 
	 * @param session
	 * @param tool
	 * @param res
	 * @throws IOException
	 */
	protected void complete(String returnUrl, Session session, Tool tool, HttpServletResponse res) throws IOException
	{
		// cleanup session
		if (session != null)
		{
			session.removeAttribute(Tool.HELPER_MESSAGE);
			session.removeAttribute(Tool.HELPER_DONE_URL);
			session.removeAttribute(ATTR_MSG);
			session.removeAttribute(ATTR_RETURN_URL);
			session.removeAttribute(ATTR_CONTAINER_CHECKED);
		}

		// if we end up with nowhere to go, go to the portal
		if (returnUrl == null)
		{
			returnUrl = ServerConfigurationService.getPortalUrl();
			// M_log.info("complete: nowhere set to go, going to portal");
		}

		// if we have no session, we just logged out... remove the JSESSIONID cookie
		if (session == null)
		{
			Cookie c = new Cookie(SESSION_COOKIE, "");
			c.setPath("/");
			c.setMaxAge(0);
			res.addCookie(c);
		}

		// if we have a session, we just logged in... write the JSESSIONID cookie, after promoting the session into a stored session (from a temp)
		else
		{
			SessionManager.startSession(session);

			// the cookie value we need to use
			String suffix = (String) ThreadLocalManager.get(SAKAI_SERVERID);
			String jSessionId = session.getId() + "." + suffix;

			Cookie c = new Cookie(SESSION_COOKIE, jSessionId);
			c.setPath("/");
			c.setMaxAge(-1);
			res.addCookie(c);
		}

		// redirect to the done URL
		res.sendRedirect(res.encodeRedirectURL(returnUrl));
	}
}
