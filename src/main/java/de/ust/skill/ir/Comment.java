/*  ___ _  ___ _ _                                                            *\
** / __| |/ (_) | |       The SKilL Generator                                 **
** \__ \ ' <| | | |__     (c) 2013-18 University of Stuttgart                 **
** |___/_|\_\_|_|____|    see LICENSE                                         **
\*                                                                            */
package de.ust.skill.ir;

import java.util.LinkedList;
import java.util.List;

/**
 * A SKilL comment providing abstraction and formatting of documentation
 * provided by the specification.
 * 
 * @author Timm Felden
 */
public class Comment {

    /**
     * Null object of comments, i.e. not a comment. Makes code generation a lot
     * easier. And resulting code visually appealing.
     * 
     * @author Timm Felden
     */
    public static final class NoComment extends Comment {

        private static final NoComment instance = new NoComment();

        public static NoComment get() {
            return instance;
        }

        @Override
        public String format(String prefix, String linePrefix, int lineWidth, String postfix) {
            return "";
        }

        @Override
        public String toString() {
            return "";
        }
    }

    List<String> text = null;
    LinkedList<Tag> tags = new LinkedList<Tag>();

    /**
     * A tag with text. See language specification for details.
     * 
     * @author Timm Felden
     */
    public static final class Tag {
        public final String name;
        List<String> text;

        public Tag(String name) {
            this.name = name;
        }

        public String[] getText() {
            return text.toArray(new String[text.size()]);
        }
    }

    public Comment() {
    }

    /**
     * Merge 2 Comments
     */
    public Comment(Comment c1, Comment c2) {
      this.text.addAll(c1.text);
      this.text.addAll(c2.text);
      this.tags.addAll(c1.tags);
      this.tags.addAll(c2.tags);
    }

    /**
     * called by the parser
     */
    public void init(List<String> text) {
        if (null == this.text)
            this.text = text;
        else
            tags.getLast().text = text;
    }

    /**
     * called by the parser
     */
    public void init(List<String> text, String tag) {
        if (null == this.text)
            this.text = text;
        else
            tags.getLast().text = text;
        tags.addLast(new Tag(tag));
    }

    /**
     * @return the ordinary text of this comment
     */
    public String[] getText() {
        return text.toArray(new String[text.size()]);
    }

    /**
     * @return tags of this comment
     */
    public Tag[] getTags() {
        return tags.toArray(new Tag[tags.size()]);
    }

    /**
     * Creates a nicely formatted String with line breaks and a prefix for a
     * code generators output.
     * 
     * @note examples use ° instead of *
     * @param prefix
     *            Prefix of the comment, e.g. " /°°"
     * @param linePrefix
     *            Prefix of a line, e.g. " ° "
     * @param lineWidth
     *            Maximum characters in a line, e.g. 80 or 120
     * @param postfix
     *            Postfix of a comment, e.g. " °/"
     * @return a nicely formatted string, very similar to scala's mkString,
     *         except that it tries to fill lines
     */
    public String format(String prefix, String linePrefix, int lineWidth, String postfix) {
        StringBuilder sb = new StringBuilder(prefix);

        formatText(text, linePrefix, lineWidth, sb, null);
        for (Tag t : tags)
            formatText(t.text, linePrefix, lineWidth, sb, t.name);

        // finish comment
        sb.append(postfix);
        return sb.toString();
    }

    /**
     * format a list of words
     */
    private static void formatText(List<String> text, String linePrefix, int lineWidth, StringBuilder sb, String tag) {
        StringBuilder line = new StringBuilder(linePrefix);

        if (null != tag)
            line.append(" @").append(tag).append(' ');

        for (String w : text) {
            if (line.length() + w.length() + 1 > lineWidth) {
                // break line
                sb.append(line).append('\n');
                line = new StringBuilder(linePrefix);
            }
            line.append(' ').append(w);
        }
        sb.append(line).append('\n');
    }

    @Override
    public String toString() {
        return text.toString();
    }
}
