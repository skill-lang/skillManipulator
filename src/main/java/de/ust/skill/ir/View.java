/*  ___ _  ___ _ _                                                            *\
** / __| |/ (_) | |       The SKilL Generator                                 **
** \__ \ ' <| | | |__     (c) 2013-18 University of Stuttgart                 **
** |___/_|\_\_|_|____|    see LICENSE                                         **
\*                                                                            */
package de.ust.skill.ir;

/**
 * A view onto another field.
 * 
 * @see SKilL V1.0 §4.4.4
 * @author Timm Felden
 * 
 * @note Views will not quite make it through Substitutions.
 */
final public class View extends FieldLike {

	final private Name ownerName;
	final private Type type;
	private FieldLike target;

	public View(Name declaredIn, Type type, Name name, Comment comment) {
		super(name, comment);
		ownerName = declaredIn;
		this.target = null;
		this.type = type;
	}

	public void initialize(FieldLike target) {
		assert null == this.target;
		this.target = target;
	}

	public Name getOwnerName() {
		return ownerName;
	}

	public Type getType() {
		return type;
	}

	public FieldLike getTarget() {
		return target;
	}

	@Override
	public String toString() {
		return "view " + target.getDeclaredIn() + "." + target.getName() + "as\n" + type + " " + name;
	}
}
