/*  ___ _  ___ _ _                                                            *\
** / __| |/ (_) | |       The SKilL Generator                                 **
** \__ \ ' <| | | |__     (c) 2013-18 University of Stuttgart                 **
** |___/_|\_\_|_|____|    see LICENSE                                         **
\*                                                                            */
package de.ust.skill.parser

import de.ust.skill.ir

object ParseException {
  def apply(msg : String) : Nothing = throw new ir.ParseException(msg);
  def apply(msg : String, cause : Throwable) : Nothing = throw new ir.ParseException(msg, cause);
}
