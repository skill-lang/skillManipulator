package de.ust.skill.manipulator.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import de.ust.skill.common.java.api.SkillException;

/**
 * An abstract skill file that is hiding all the dirty implementation details
 * from you.
 *
 * @author Timm Felden
 */
//@SuppressWarnings("all")
public interface SkillFile extends de.ust.skill.common.java.api.SkillFile {

    /**
     * Create a new skill file based on argument path and mode.
     *
     * @throws IOException
     *             on IO and mode related errors
     * @throws SkillException
     *             on file or specification consistency errors
     */
    public static SkillFile open(String path, Mode... mode) throws IOException, SkillException {
        File f = new File(path);
        return open(f, mode);
    }

    /**
     * Create a new skill file based on argument path and mode.
     *
     * @throws IOException
     *             on IO and mode related errors
     * @throws SkillException
     *             on file or specification consistency errors
     */
    public static SkillFile open(File path, Mode... mode) throws IOException, SkillException {
        for (Mode m : mode) {
            if (m == Mode.Create && !path.exists())
                path.createNewFile();
        }
        assert path.exists() : "can only open files that already exist in general, because of java.nio restrictions";
        return open(path.toPath(), mode);
    }

    /**
     * Create a new skill file based on argument path and mode.
     *
     * @throws IOException
     *             on IO and mode related errors
     * @throws SkillException
     *             on file or specification consistency errors
     */
    public static SkillFile open(Path path, Mode... mode) throws IOException, SkillException {
        return SkillState.open(path, mode);
    }
}
