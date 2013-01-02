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
	<xsl:variable name="meta-doc" select="document($game-meta-uri)" />
	
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
		
	<!--  calculate the x,y coords of a hex -->
	<xsl:template name="cx">
		<xsl:param name="nx" />
		
		<xsl:value-of select="0.5 * ($nx + 1.2) * $edgeLength" />
	</xsl:template>
	
	<xsl:template name="cy">
		<xsl:param name="ny" />
		
		<xsl:value-of select="$sqrt3over2 * ($ny + 0.5) * $edgeLength" />
	</xsl:template>
	
	
	<xsl:variable name="polyradius">		
		<xsl:variable name="x0">
			<xsl:call-template name="cx">
				<xsl:with-param name="nx">
					<xsl:value-of select="$pc/game:corner[6]/@x" />
				</xsl:with-param>
			</xsl:call-template>
		</xsl:variable>
				
		<xsl:variable name="x1">
			<xsl:call-template name="cx">
				<xsl:with-param name="nx">
					<xsl:value-of select="$pc/game:corner[3]/@x" />
				</xsl:with-param>
			</xsl:call-template>
		</xsl:variable>
		
		<xsl:value-of select="$x1 - $x0" />
	</xsl:variable>
	
	<xsl:template name="mx">
		<xsl:param name="nx" />
		
		<xsl:variable name="cx0">
			<xsl:call-template name="cx">
				<xsl:with-param name="nx">
					<xsl:value-of select="$nx + $pc/game:corner[1]/@x" />
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
		
		<xsl:value-of select="($cx0 + $cx4) div 2.0" />
	</xsl:template>
	
	<xsl:template name="my">
		<xsl:param name="ny" />
				
		<xsl:variable name="cy0">
			<xsl:call-template name="cy">
				<xsl:with-param name="ny">
					<xsl:value-of select="$ny + $pc/game:corner[1]/@y" />
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
		
		<xsl:value-of select="($cy0 + $cy4) div 2.0" />
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
					@font-face {
						font-family: "Fanwood";
						src: url("/client/0.2/fonts/Fanwood.otf");
					}
				
					.hex {
						fill:#F5F5FF;
						stroke: #CCCCCC;
						stroke-width:0.5;
					}
					.hex-inner {
						fill: rgba(255,0,0,0.1);
						stroke: #FF0000;
						stroke-width: 1px;
					}
					.hexlabel-back {
						fill:#F5F5FF;
						stroke:#CCCCCC;
					}
					.hexlabel {
						font-family:"Fanwood", serif;
						font-size:26px;
						stroke:#000000;
						fill:#000000;
						dominant-baseline:central;
						text-anchor:middle;
					}
					.labelemph {
						stroke:#FF0000;
						fill:#FF0000;
					}
					.Fields {
					    fill:#FFEF4F;
					    stroke:#DDCD2D;
					    stroke-width:1;
					}
					.Forest {
					    fill:#00932C;
					    stroke:#00710A;
					    stroke-width:1;
					}
					.Pasture {
					    fill:#BFE882;
					    stroke:#9DC770;
					    stroke-width:1;
					}
					.Hills {
					    fill:#B22222;
					    stroke:#900000;
					    stroke-width:1;
					}
					.Mountains {
					    fill:#787887;
					    stroke:#565665;
					    stroke-width:1;
					}
					.Desert {
					    fill:#fafad2;
					    stroke:#D8D8B0;
					    stroke-width:1;
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
							
							<view:eventHandler event="game.startGame" mode="replace" />
						</svg:g>
					</svg:svg>
				</div>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template match="/game:message[game:event = 'game.startGame']">
		<xsl:apply-templates select="$meta-doc/game:game/game:mostRecentState//scxml:data[@name='state']/game:board/game:polys/game:poly" />
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
		<xsl:variable name="circleratio" select="1.0 div 7.5" />
		
		<xsl:variable name="outerpoints">
			<xsl:for-each select="$pc/game:corner">
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
		</xsl:variable>
		
		<xsl:variable name="innerpoints">
			<xsl:for-each select="$pc/game:corner">
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
						
				<xsl:value-of select="($cx - $mx) * (1.0 - $hexmargin) + $mx" /><xsl:text> </xsl:text>
				<xsl:value-of select="($cy - $my) * (1.0 - $hexmargin) + $my" /><xsl:text> </xsl:text>
			</xsl:for-each>
		</xsl:variable>
				
		<svg:g>	
			<svg:polygon class="hex" points="{$outerpoints}" />
			<svg:polygon class="hex-inner {@type}" points="{$innerpoints}" />
			<xsl:if test="string-length(@value) &gt; 0">
				<svg:circle class="hexlabel-back" cx="{$mx}" cy="{$my}" r="{$polyradius * $circleratio}" />
				<svg:text x="{$mx}" y="{$my}">
					<xsl:choose>
						<xsl:when test="@value = 6 or @value = 8">
							<xsl:attribute name="class">hexlabel labelemph</xsl:attribute>
						</xsl:when>
						<xsl:otherwise>
							<xsl:attribute name="class">hexlabel</xsl:attribute>
						</xsl:otherwise>
					</xsl:choose>
					<xsl:value-of select="@value" />
				</svg:text>
			</xsl:if>
		</svg:g>
	</xsl:template>
	
	
	
</xsl:stylesheet>