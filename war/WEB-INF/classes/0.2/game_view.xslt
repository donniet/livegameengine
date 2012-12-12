<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:txsl="http://www.w3.org/1999/XSL/Transform#Client"
	xmlns:sxsl="http://www.w3.org/1999/XSL/Transform#Client2"
	xmlns:view="http://www.livegameengine.com/schemas/view.xsd"
	xmlns:game="http://www.livegameengine.com/schemas/game.xsd"
	xmlns="http://www.w3.org/1999/xhtml"
	xmlns:xalan="http://xml.apache.org/xalan"
	xmlns:java="http://xml.apache.org/xalan/java"
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
	<xsl:param name="titlePrefix" select="'Live Game Engine - '" />
	
	<xsl:variable name="version" select="0.2" />
	
	
	<xsl:namespace-alias stylesheet-prefix="sxsl" result-prefix="xsl" />
	
	<xsl:output method="xhtml" indent="yes" cdata-section-elements="script"  />
		
	<xsl:template name="write-doctype">
		<xsl:text disable-output-escaping="yes">
&lt;!DOCTYPE html PUBLIC &quot;</xsl:text><xsl:value-of select="$doctype-public" /><xsl:text disable-output-escaping="yes">&quot; &quot;</xsl:text><xsl:value-of select="$doctype-system" /><xsl:text disable-output-escaping="yes">&quot; [
	&lt;!ELEMENT view:templates (sxsl:stylesheet) &gt;
	&lt;!ELEMENT sxsl:stylesheet ANY &gt;
	&lt;!ATTLIST sxsl:stylesheet 
		id ID #IMPLIED
	&gt;
	&lt;!ELEMENT view:events (view:event) &gt;
	&lt;!ELEMENT view:event ANY &gt;
	&lt;!ATTLIST view:event 
		id ID #IMPLIED
	&gt;
	&lt;!ENTITY % head.misc "(script|style|meta|link|object|view:templates|view:events)*" &gt;
]&gt;
</xsl:text>
	</xsl:template>
	
	<xsl:template match="/">
		<xsl:call-template name="write-doctype" />
		
		<xsl:apply-templates select="*|text()" />
	</xsl:template>
	
	<xsl:template match="html:title">
		<xsl:copy>
			<xsl:value-of select="concat($titlePrefix, text())" />
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="html:head">
		<xsl:copy>
			<script type="text/javascript" src="/client/{$version}/view.js"></script>
			<script type="text/javascript">
				View.registerEventHandlers([<xsl:for-each select="//view:eventHandler">
						{ "elementId": "<xsl:call-template name="create-template-parent-id" />", "templateId": "<xsl:call-template name="create-template-id" />" },</xsl:for-each>
				]);
			</script>
			
			<xsl:apply-templates select="@*|*|text()" />
			
			<view:templates>
				<xsl:apply-templates select="//view:eventHandler" mode="header" />
			</view:templates>
			
			<view:events>
				<xsl:apply-templates select="//view:event" mode="header" />
			</view:events>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template name="create-template-id">
		<xsl:value-of select="concat('temp-',generate-id(.))" />
	</xsl:template>
	
	<xsl:template name="create-template-parent-id">
		<xsl:choose>
			<xsl:when test="string-length(../@id) > 0">
				<xsl:value-of select="../@id" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="generate-id(..)" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="view:eventHandler" mode="header">
		<sxsl:stylesheet version="2.0">
			<xsl:attribute name="id"><xsl:call-template name="create-template-id" /></xsl:attribute>
			<xsl:for-each select="view:parameter">
				<sxsl:param name="{@name}" />
			</xsl:for-each>
			
			<xsl:for-each select="view:template">
				<sxsl:template>
					<xsl:choose>
						<xsl:when test="string-length(@match) > 0">
							<xsl:attribute name="match"><xsl:value-of select="@match" /></xsl:attribute>
						</xsl:when>
						<xsl:otherwise>
							<xsl:attribute name="match">/</xsl:attribute>
						</xsl:otherwise>
					</xsl:choose>
					
					<xsl:apply-templates select="@*[local-name() != 'match']|*|text()" />
				</sxsl:template>
			</xsl:for-each>
			
			
			<sxsl:template match="*[namespace-uri() = 'http://www.w3.org/1999/XSL/Transform#Client']">
				<sxsl:choose>
					<sxsl:when test="count(ancestor::view:eventHandler) > 1">
						<sxsl:copy>
							<sxsl:apply-templates select="@*|*|text()" />
						</sxsl:copy>
					</sxsl:when>
					<sxsl:otherwise>
						<sxsl:element name="{local-name()}" namespace="http://www.w3.org/1999/XSL/Transform">
							<sxsl:apply-templates select="@*|*|text()" />
						</sxsl:element>
					</sxsl:otherwise>
				</sxsl:choose>
			</sxsl:template>
			
		</sxsl:stylesheet>
	</xsl:template>
	
	<xsl:template match="view:event" mode="header">
		<xsl:copy>
			<xsl:attribute name="id"><xsl:value-of select="generate-id()" /></xsl:attribute>
			
			<xsl:apply-templates select="@*|*|text()" />
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="view:eventHandler" mode="content">
		<xsl:attribute name="id">
			<xsl:call-template name="create-template-parent-id" />
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="view:event" mode="content">
		<xsl:attribute name="on{@on}">View.trigger(this, '<xsl:value-of select="generate-id(.)" />')</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="txsl:*">
		<xsl:choose>
			<xsl:when test="count(ancestor::view:eventHandler) > 1">
				<xsl:copy>
					<xsl:apply-templates select="@*|*|text()" />
				</xsl:copy>
			</xsl:when>
			<xsl:otherwise>
				<xsl:element name="{local-name()}" namespace="http://www.w3.org/1999/XSL/Transform">
					<xsl:apply-templates select="@*|*|text()" />
				</xsl:element>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	
	<xsl:template match="@*|*|text()" priority="-20">
		<xsl:copy>
			<xsl:apply-templates select="view:event" mode="content" />
			<xsl:apply-templates select="view:eventHandler" mode="content" />
			
			<xsl:apply-templates select="@*|*[namespace-uri() != 'http://www.livegameengine.com/schemas/view.xsd' or (local-name() != 'eventHandler' and local-name() != 'event')]|text()" />
		</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>