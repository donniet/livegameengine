<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:txsl="http://www.w3.org/1999/XSL/Transform#Client"
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
	
	<xsl:variable name="version" select="0.1" />
	
	
	<xsl:namespace-alias stylesheet-prefix="txsl" result-prefix="xsl" />
	
	<xsl:output method="xhtml" indent="yes" cdata-section-elements="script"  />
		
	<xsl:template name="write-doctype">
		<xsl:text disable-output-escaping="yes">
&lt;!DOCTYPE html PUBLIC &quot;</xsl:text><xsl:value-of select="$doctype-public" /><xsl:text disable-output-escaping="yes">&quot; &quot;</xsl:text><xsl:value-of select="$doctype-system" /><xsl:text disable-output-escaping="yes">&quot; &gt;
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
	
	<xsl:template match="txsl:*">
		<xsl:element name="{local-name()}" namespace="http://www.w3.org/1999/XSL/Transform">
			<xsl:apply-templates select="@*|*|text()" />
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="@*|*|text()" priority="-20">
		<xsl:copy>
			<xsl:apply-templates select="@*|*|text()" />
		</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>