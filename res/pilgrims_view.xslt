<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns="http://www.w3.org/1999/xhtml" 
	xmlns:pil="http://www.livegameengine.com/schemas/pilgrims.xsd"
	xmlns:view="http://www.livegameengine.com/schemas/view.xsd"
	xmlns:game="http://www.livegameengine.com/schemas/game.xsd"
	xmlns:scxml="http://www.w3.org/2005/07/scxml"
	xmlns:xalan="http://xml.apache.org/xalan"
	xmlns:ex="http://exslt.org/common"
	xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:svg="http://www.w3.org/2000/svg"
	exclude-result-prefixes="xsl xalan fn scxml game ex">
	
	<xsl:param name="game-meta-uri" select="'game://current/meta'" />
	
	<xsl:variable name="sqrt3over2" select="0.86602540378" />
	<xsl:variable name="edgeLength" select="85" />
	<xsl:variable name="polycorners">
		<game:corner x="1" y="0" />
		<game:corner x="3" y="0" />
		<game:corner x="4" y="1" />
		<game:corner x="3" y="2" />
		<game:corner x="1" y="2" />
		<game:corner x="0" y="1" />
	</xsl:variable>
	<xsl:variable name="pc" select="ex:node-set($polycorners)" />
	
	<xsl:template name="mx">
		<xsl:param name="nx" />
		
		<xsl:variable name="cx0">
			<xsl:call-template name="cx">
				<xsl:with-param name="nx">
					<xsl:value-of select="$nx + $pc/game:corner[0]/@x" />
				</xsl:with-param>
			</xsl:call-template>
		</xsl:variable>
				
		<xsl:variable name="cx4">
			<xsl:call-template name="cx">
				<xsl:with-param name="nx">
					<xsl:value-of select="$nx + $pc/game:corner[4]/@x" />
				</xsl:with-param>
			</xsl:call-template>
		</xsl:variable>
		
		<xsl:value-of select="(cx0 + cx4) div 2.0" />
	</xsl:template>
	
	<xsl:template name="my">
		<xsl:param name="ny" />
				
		<xsl:variable name="cy0">
			<xsl:call-template name="cy">
				<xsl:with-param name="ny">
					<xsl:value-of select="$ny + $pc/game:corner[0]/@y" />
				</xsl:with-param>
			</xsl:call-template>
		</xsl:variable>
				
		<xsl:variable name="cy4">
			<xsl:call-template name="cy">
				<xsl:with-param name="ny">
					<xsl:value-of select="$ny + $pc/game:corner[4]/@y" />
				</xsl:with-param>
			</xsl:call-template>
		</xsl:variable>
		
		<xsl:value-of select="(cy0 + cy4) div 2.0" />
	</xsl:template>
	
	<!--  calculate the x,y coords of a hex -->
	<xsl:template name="cx">
		<xsl:param name="nx" />
		
		<xsl:value-of select="0.5 * ($nx + 1.2) * $edgeLength" />
	</xsl:template>
	
	<xsl:template name="cy">
		<xsl:param name="ny" />
		
		<xsl:value-of select="$sqrt3over2 * ($ny + 0.5) * $edgeLength" />
	</xsl:template>
		
	<xsl:template match="/game:message">
		<xsl:apply-templates select="game:content/*" mode="copy" />
	</xsl:template>
	
	<xsl:template match="@*|*|text()" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*|*|text()" mode="copy" />
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="/scxml:datamodel">
		<xsl:apply-templates select="scxml:data/pil:board" />
	</xsl:template>
	
	<xsl:template match="scxml:data/pil:board">
		<html>
			<head>
				<title>Pilgrims of Natac</title>
				<style type="text/css">
					.hex {
						fill:#F5F5FF;
						stroke: #CCCCCC;
						stroke-width:0;
					}
				</style>
			</head>
			
			<body>
				<input value="Join" type="button">
					<view:event gameEvent="join" on="click" />
				</input>
				<input value="Start" type="button">
					<view:event gameEvent="start" on="click" />
				</input>
				
				<div id="board">
					<svg:svg width="1000px" height="1000px" baseProfile="full" version="1.1">
						<svg:g id="board">
							<xsl:apply-templates select="pil:polys/pil:poly" />
						</svg:g>
					</svg:svg>
				</div>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template match="pil:poly">
		<xsl:variable name="x" select="@x" />
		<xsl:variable name="y" select="@y" />
		
		<xsl:variable name="mx">
			<xsl:call-template name="mx">
				<xsl:with-param name="nx" select="$x" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="my">
			<xsl:call-template name="my">
				<xsl:with-param name="ny" select="$y" />
			</xsl:call-template>
		</xsl:variable>
		
		<xsl:variable name="hexmargin" select="0.1" />
				
		<svg:g>	
			<svg:polygon class="hex">
				<xsl:attribute name="points">	
					<xsl:for-each select="ex:node-set($polycorners)/game:corner">
						<xsl:variable name="cx">
							<xsl:call-template name="cx">
								<xsl:with-param name="nx" select="@x + $x" />
							</xsl:call-template>
						</xsl:variable>
						<xsl:variable name="cy">
							<xsl:call-template name="cy">
								<xsl:with-param name="ny" select="@y + $y" />
							</xsl:call-template>
						</xsl:variable>
						<xsl:value-of select="$cx" /><xsl:text> </xsl:text>
						<xsl:value-of select="$cy" /><xsl:text> </xsl:text>
					</xsl:for-each>
				</xsl:attribute>
			</svg:polygon>
			<svg:polygon class="hex-inner">
				<xsl:attribute name="points">	
					<xsl:for-each select="ex:node-set($polycorners)/game:corner">
						<xsl:variable name="cx">
							<xsl:call-template name="cx">
								<xsl:with-param name="nx" select="@x + $x" />
							</xsl:call-template>
						</xsl:variable>
						<xsl:variable name="cy">
							<xsl:call-template name="cy">
								<xsl:with-param name="ny" select="@y + $y" />
							</xsl:call-template>
						</xsl:variable>
						
						
						
						<xsl:value-of select="($cx - $mx) * (1.0 - $hexmargin) + $cx" /><xsl:text> </xsl:text>
						<xsl:value-of select="($cy - $my) * (1.0 - $hexmargin) + $cy" /><xsl:text> </xsl:text>
					</xsl:for-each>
				</xsl:attribute>
				<!-- @x + $x + $pc/game:corner[0]/@x  -->
				<blah>mx: <xsl:value-of select="$pc" /></blah>
				
			</svg:polygon>
		</svg:g>
	</xsl:template>
	
	
	
</xsl:stylesheet>