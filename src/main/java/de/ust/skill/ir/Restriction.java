/*  ___ _  ___ _ _                                                            *\
** / __| |/ (_) | |       The SKilL Generator                                 **
** \__ \ ' <| | | |__     (c) 2013-18 University of Stuttgart                 **
** |___/_|\_\_|_|____|    see LICENSE                                         **
\*                                                                            */
package de.ust.skill.ir;

/**
 * Base class for all restrictions.
 * 
 * @author Timm Felden
 */
public abstract class Restriction {
	/**
	 * @return the name of the restriction
	 */
	public abstract String getName();

	/**
	 * @return the default representation of the restriction
	 */
	@Override
	public abstract String toString();
}
