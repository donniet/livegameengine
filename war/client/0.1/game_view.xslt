<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:view="http://www.livegameengine.com/schemas/view.xsd"
	xmlns:game="http://www.livegameengine.com/schemas/game.xsd"
	xmlns="http://www.w3.org/1999/xhtml"
	xmlns:xalan="http://xml.apache.org/xalan"
	xmlns:html="http://www.w3.org/1999/xhtml">

	<xsl:param name="eventEndpointUrl" />
	<xsl:param name="eventEndpointMethod" select="'POST'" />
	<xsl:param name="joinEndpointUrl" />
	<xsl:param name="joinEndpointMethod" select="'POST'" />
	<xsl:param name="startEndpointUrl" />
	<xsl:param name="startEndpointMethod" select="'POST'" />
	<xsl:param name="doctype-public" />
	<xsl:param name="doctype-system" />
	<xsl:param name="jsapiUrl" />
	<xsl:param name="userToken" select="''" />
	
	<xsl:variable name="version" select="0.1" />

	<xsl:output method="xhtml" indent="yes" cdata-section-elements="script"  />
	
	<xsl:template match="/view:doc">
		<xsl:call-template name="write-doctype" />
		<html>
			<head>
				<xsl:apply-templates select="view:meta" />
				
				<view:events>
					<xsl:apply-templates select="//view:event" mode="header" />
				</view:events>
				
			</head>
			<body>
				<xsl:call-template name="header" />

				<xsl:apply-templates select="view:body" />

				<xsl:call-template name="footer" />
			</body>
		</html>
	</xsl:template>

	<xsl:template name="write-doctype">
		<xsl:text disable-output-escaping="yes">
&lt;!DOCTYPE html PUBLIC &quot;</xsl:text><xsl:value-of select="$doctype-public" /><xsl:text disable-output-escaping="yes">&quot; &quot;</xsl:text><xsl:value-of select="$doctype-system" /><xsl:text disable-output-escaping="yes">&quot; [
	&lt;!ELEMENT view:events (view:event) &gt;
	&lt;!ELEMENT view:event ANY &gt;
	&lt;!ATTLIST view:event id ID #IMPLIED &gt;
	&lt;!ENTITY % head.misc "(script|style|meta|link|object|view:events)*"&gt;	
]&gt;
</xsl:text>
	</xsl:template>

	<xsl:template match="view:body">
		<xsl:apply-templates select="*" />
	</xsl:template>
	
	<xsl:template match="view:meta">
		<title>
			<xsl:value-of select="view:title/text()" />
		</title>
		<xsl:call-template name="include-scripts" />
		
		<xsl:apply-templates select="view:styles" />
		<xsl:apply-templates select="view:scripts" />
		<script type="text/javascript">
			<xsl:call-template name="setViewNamespaces" />
			<xsl:call-template name="eventEndpoint" />
			<xsl:call-template name="startEndpoint" />
			<xsl:call-template name="joinEndpoint" />
			<xsl:call-template name="channelSetup" />
		</script>
	</xsl:template>
	
	<xsl:template match="view:event" mode="header">
		<xsl:copy>
			<xsl:attribute name="id"><xsl:value-of select="generate-id(.)" /></xsl:attribute>
			<xsl:apply-templates select="@*|*" />
		</xsl:copy>
	</xsl:template>
		
	<xsl:template match="view:styles">
		<xsl:choose>
			<xsl:when test="@url">
				<link rel="stylesheet" type="text/css" href="{@url}" />
			</xsl:when>
			<xsl:otherwise>
				<style type="text/css">
					<xsl:value-of select="text()" />
				</style>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="view:scripts">
		<xsl:choose>
			<xsl:when test="@url">
				<script type="text/javascript" src="{@url}"></script>
			</xsl:when>
			<xsl:otherwise>
				<script type="text/javascript">
					<value-of select="text()" />
				</script>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="eventEndpoint">
		View.getInstance().registerEventEndpoint({
			url:"<xsl:value-of select="$eventEndpointUrl" />",
			method:"<xsl:value-of select="$eventEndpointMethod" />"
		});
	</xsl:template>
	<xsl:template name="startEndpoint">
		View.getInstance().registerStartEndpoint({
			url:"<xsl:value-of select="$startEndpointUrl" />",
			method:"<xsl:value-of select="$startEndpointMethod" />"
		});
	</xsl:template>
	<xsl:template name="joinEndpoint">
		View.getInstance().registerJoinEndpoint({
			url:"<xsl:value-of select="$joinEndpointUrl" />",
			method:"<xsl:value-of select="$joinEndpointMethod" />"
		});
	</xsl:template>
	<xsl:template name="setViewNamespaces">
		<!-- TODO: replace these with namespace-from-prefix or something similar -->
		View.getInstance().setViewNamespace("http://www.livegameengine.com/schemas/view.xsd");
		View.getInstance().setGameNamespace("http://www.livegameengine.com/schemas/game.xsd");
	</xsl:template>
	<xsl:template name="channelSetup">
		<xsl:if test="string-length($userToken) > 0">
		
		View.getInstance().setUserToken("<xsl:value-of select="$userToken" />");
		
		</xsl:if>
	</xsl:template>

	<xsl:template match="view:event">
		<xsl:attribute name="on{@event}">View.getInstance().fireEvent(this, '<xsl:value-of select="generate-id(.)" />')</xsl:attribute>
	</xsl:template>

	<xsl:template match="*|@*|text()">
		<xsl:copy>
			<xsl:apply-templates select="@*" />
			<xsl:apply-templates select="view:event" />
			<xsl:apply-templates select="text()|*[namespace-uri() != 'http://www.livegameengine.com/schemas/view.xsd' or local-name() != 'event']" />
		</xsl:copy>
	</xsl:template>

	<xsl:template name="header">
		<h1>Header</h1>
	</xsl:template>

	<xsl:template name="footer">
		<div><a href="/">home</a></div>
	</xsl:template>

	<xsl:template name="include-scripts">
		<script type="text/javascript" src="{$jsapiUrl}"></script>
		<script type="text/javascript" src="/client/{$version}/util.js"></script>
		<script type="text/javascript" src="/client/{$version}/view.js"></script>
	</xsl:template>
</xsl:stylesheet>