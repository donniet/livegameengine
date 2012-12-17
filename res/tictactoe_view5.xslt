<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:txsl="http://www.w3.org/1999/XSL/Transform#Client"
	xmlns="http://www.w3.org/1999/xhtml" 
	xmlns:tic="http://www.livegameengine.com/schemas/games/tictactoe.xsd"
	xmlns:view="http://www.livegameengine.com/schemas/view.xsd"
	xmlns:game="http://www.livegameengine.com/schemas/game.xsd"
	xmlns:scxml="http://www.w3.org/2005/07/scxml"
	xmlns:xalan="http://xml.apache.org/xalan"
	xmlns:fn="http://www.w3.org/2005/xpath-functions">
	
	<xsl:param name="game-meta-uri" select="'game://current/meta'" />
	
	
	<xsl:param name="connected" />
	<xsl:param name="playerid" />
	<xsl:param name="x" />
	<xsl:param name="y" />
	
	<xsl:variable name="game-meta-doc" select="document($game-meta-uri)" />
	
	<xsl:template match="/scxml:datamodel">
		<html>
			<head>
				<title>Tic Tac Toe</title>
				<style type="text/css">
					td { width: 100px; height: 100px; border: solid 1px black; }
					.highlight { background-color: rgb(50,50,255); }
				</style>
			</head>
			<body>
				<div>
				<input type="button" value="Start">
					<view:event gameEvent="start" on="click" />
				</input>
				<input type="button" value="Join">
					<view:event gameEvent="join" on="click" />
				</input>
				</div>
				
				<xsl:apply-templates select="scxml:data/tic:board" />
				
				<div>
					<view:eventHandler event="game.update-meta" mode="replace" />
				</div>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template match="/game:event[name='game.update-meta']//game:players">
		<ul>
			<xsl:apply-templates select="game:player" />	
		</ul>
	</xsl:template>
	
	<xsl:template match="/game:event[@name='game.playerConnectionChange']//game:player">
		<xsl:if test="game:param[@name='player']/text() = $playerid">
			<strong>
				<xsl:choose>
					<xsl:when test="game:param[@name='connected']/text() = 'true'">connected</xsl:when>
					<xsl:otherwise>disconnected</xsl:otherwise>
				</xsl:choose>
			</strong>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="game:player">
		<li>
			<span>
				<view:eventHandler event="game.playerConnectionChange" mode="replace">
					<view:param name="playerid" value="{game:gameUser/game:userid}" />
				</view:eventHandler>
			</span>
			<txsl:value-of select="decendent-or-self::game:gameUser/game:nickname" /> 
			<txsl:value-of select="decendent-or-self::game:gameUser/game:userid" />
		</li>
	</xsl:template>
	
	<xsl:template match="tic:board">
		<table>
			<xsl:apply-templates select="tic:row" />
		</table>
	</xsl:template>
	
	<xsl:template match="tic:row">
		<tr>
			<xsl:apply-templates select="tic:col" />
		</tr>
	</xsl:template>
	
	<xsl:template match="/game:event[name='board.placement']">
		<xsl:if test="game:param[@name='x']=$x and game:param[@name='y']=$y">
			<strong><xsl:value-of select="game:param[@name='role']" /></strong>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="tic:col">
		<xsl:variable name="x"><xsl:value-of select="count(preceding-sibling::tic:col)" /></xsl:variable>
		<xsl:variable name="y"><xsl:value-of select="count(../preceding-sibling::tic:row)" /></xsl:variable>
		<td>
			<xsl:if test="@highlight = 'true'">
				<xsl:attribute name="class">highlight</xsl:attribute>
			</xsl:if>
			<xsl:choose>
				<xsl:when test="count(tic:mark) > 0">
					<xsl:value-of select="tic:mark/@player" />
				</xsl:when>
				<xsl:otherwise>
					<view:event on="click" endpointEvent="click">
						<tic:square x="{$x}" y="{$y}" />
					</view:event>
					<span>
						<view:eventHandler event="board.placement" mode="replace">
							<view:param name="x" value="{$x}" />
							<view:param name="y" value="{$y}" />
						</view:eventHandler>
					</span>
				</xsl:otherwise>
			</xsl:choose>
		</td>
	</xsl:template>
	
</xsl:stylesheet>