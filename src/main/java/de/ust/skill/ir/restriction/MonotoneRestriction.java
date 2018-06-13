/*  ___ _  ___ _ _                                                            *\
** / __| |/ (_) | |       The SKilL Generator                                 **
** \__ \ ' <| | | |__     (c) 2013-18 University of Stuttgart                 **
** |___/_|\_\_|_|____|    see LICENSE                                         **
\*                                                                            */
package de.ust.skill.ir.restriction;

import de.ust.skill.ir.Restriction;

/**
 * Instances of monotone classes can not be deleted or modified, once they have
 * been (de-)serialized.
 * 
 * @author Timm Felden
 */
final public class MonotoneRestriction extends Restriction {

	@Override
	public String getName() {
		return "monotone";
	}

	@Override
	public String toString() {
		return "@monotone";
	}

}
