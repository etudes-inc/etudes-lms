<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<!--
    /**********************************************************************************
    * $URL:  $
    * $Id:  $
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
-->
    <xsl:template match="/">
        <xsl:variable name="skinRepo" select="/entity-service/request-properties/request-attributes/request-attribute[@name='sakai.skin.repo']/value"></xsl:variable>
        <xsl:variable name="skin" select="/entity-service/request-properties/request-attributes/request-attribute[@name='sakai.skin']/value"></xsl:variable>
        <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
            <head>
                <title>
                <xsl:value-of select="/entity-service/entity/properties/property[@name='_title']" /></title>
      
                <link href="/sakai-rwiki-tool/styles/wikiStyle.css" type="text/css" rel="stylesheet"
                    media="all"/>
                    
                <script type="text/javascript" src="/sakai-rwiki-tool/scripts/stateswitcher.js"> </script>
                <script type="text/javascript" src="/sakai-rwiki-tool/scripts/ajaxpopup.js"> </script>
                <script type="text/javascript" src="/sakai-rwiki-tool/scripts/asyncload.js"> </script>
                <script type="text/javascript" src="/sakai-rwiki-tool/scripts/logger.js"> </script>
                <link type="application/rss+xml" href="/access/wiki{/entity-service/entity/properties/property[@name='realm']}/-.10.rss" title="Sakai Wiki RSS" rel="alternate"/>
             
                
                <link href="{$skinRepo}/tool_base.css" type="text/css" rel="stylesheet"
                    media="all"/>
                <link href="{$skinRepo}/{$skin}/tool.css" type="text/css" rel="stylesheet"
                    media="all"/>
                <script type="text/javascript" language="JavaScript" src="/library/js/headscripts.js"/>
            </head>
            <body>
              <div class="publicview" >
                <div id="rwiki_container">
                    <div class="portletBody">
                        <div id="rwiki_content_nosidebar">
                            <h3><xsl:value-of select="/entity-service/entity/properties/property[@name='_title']" /></h3>
                            <div class="rwikiRenderBody">
                                <div class="rwikiRenderedContent">
				  <xsl:copy-of select="/entity-service/entity/rendered-content/content/rendered/node()"/>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                </div>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
