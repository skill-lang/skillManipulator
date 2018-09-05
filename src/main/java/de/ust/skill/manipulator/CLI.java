package de.ust.skill.skillManipulator.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.ust.skill.common.java.api.SkillException;
import de.ust.skill.ir.TypeContext;
import de.ust.skill.parser.Parser;
import de.ust.skill.skillManipulator.gc.GarbageCollector;
import de.ust.skill.skillManipulator.internal.SkillFile;
import de.ust.skill.skillManipulator.gc.CollectionRoot;
import de.ust.skill.skillManipulator.utils.FieldUtils;
import de.ust.skill.skillManipulator.utils.TypeUtils;
import specificationMapping.SpecificationMapper;

public class CLI {
	private static CommandLine line; 
	private static Options options = new Options();
	private static SkillFile sf;
	private static Path outpath = null;
	
	public static void main(String[] args) {
		CommandLineParser parser = new DefaultParser();

		// create command line options
		createOptions();
		
		try {
		    // parse the command line arguments
		    line = parser.parse(options, args);
		}
		catch( ParseException exp ) {
		    System.out.println("Wrong usage of command line interface.");
		    printHelp();
		    return;
		}    
		
		// get general options
		if(!parseSkillfile()) return;

		if(line.hasOption("o")) outpath = Paths.get(line.getOptionValue("o"));

		// execute chosen mode
		if(line.hasOption("h")) {
			printHelp();
		} else if(line.hasOption("gc")) {
			executeGC();
		} else if(line.hasOption("map")) {
			executeSpecificationMapping();
		} else if(line.hasOption("rm")) {
			executeRemoval();
		}

	}

	private static void executeRemoval() {
		if(line.hasOption("t")) {
			TypeUtils.deleteType(sf, line.getOptionValue("t"));
		} else if(line.hasOption("f")) {
			String field = line.getOptionValue("f");
			if(!field.contains(".")) {
				System.out.println("Please specify field as type.field");
				return;
			}
			String[] splitted = field.split(".");
			String fieldname = splitted[1];
			String ofType = splitted[0];
			FieldUtils.removeField(sf, fieldname, ofType);
		} else {
			System.out.println("Please provide field(-f) or type(-t) to remove.");
			return;
		}
		
	}

	private static void executeSpecificationMapping() {
		if(!line.hasOption("spec")) {
			System.out.println("Please provide a specification file with option -spec.");
			return;
		}
		
		TypeContext tc = Parser.process(new File(line.getOptionValue("spec")), false, false, false, false);
		try {
			tc = tc.removeSpecialDeclarations();
		} catch (de.ust.skill.ir.ParseException e) {
			System.out.println("Error while parsing specification file.");
			return;
		}
		
		if(outpath == null) outpath = sf.currentPath();
		SkillFile newSf = SpecificationMapper.map(tc, sf, outpath);
		
		if(!line.hasOption("d")) newSf.close();
	}

	private static void createOptions() {
		/**
		 * Option group for mode
		 */
		OptionGroup command = new OptionGroup();
		command.setRequired(true);
		command.addOption(new Option("h", "print this help page"));
		command.addOption(new Option("gc", "execute a garbage collection run on given binary file with given roots"));
		command.addOption(new Option("map", "map the SKilL-Graph in the given binary file on the given specification"));
		command.addOption(new Option("rm", "remove a whole type or al field from a type"));
		
		options.addOptionGroup(command);

		/**
		 * general options
		 */
		options.addOption(Option.builder("i")
				.required()
				.hasArg()
				.argName("file")
				.desc("Specifiy the binary file the chosen method is used on")
				.build());
		
		options.addOption(Option.builder("d")
				.hasArg(false)
				.desc("Set this flag for a dry run. No file is written if this option is set.")
				.build());
		
		options.addOption(Option.builder("o")
				.hasArg()
				.argName("file")
				.desc("Specify output file. Otherwise the input file is overwritten.")
				.build());
		
		/**
		 * GC options
		 */
		options.addOption(Option.builder("r")
				.hasArgs()
				.valueSeparator(',')
				.argName("root1,root2,...")
				.desc("Specifiy the roots for the garbage collection. Roots can have the form 'type' or the form 'type#id'."
						+ "Examples: 'metainformation' 'imlgraph#1'")
				.build());
		options.addOption(new Option("kC", "Keep empty collections and their referenced types after garbage colletion."));
		options.addOption(new Option("s", "Print garbage collection statistics."));
		options.addOption(new Option("p", "Print removed objects."));
		
		/**
		 * Specification Mapping options
		 */
		options.addOption(Option.builder("spec")
				.hasArg()
				.argName("specification.skill")
				.desc("The specification file with the typesystem to map the binary file on.")
				.build());
		
		/**
		 * Field/Type remove options
		 */
		OptionGroup rmType = new OptionGroup();
		rmType.addOption(Option.builder("t")
				.hasArg()
				.argName("type")
				.desc("Remove the given type from the typesystem including all subtypes and objects.")
				.build());
		options.addOption(Option.builder("f")
				.hasArg()
				.argName("type.field")
				.desc("Remove specified field from the typesystem.")
				.build());
		options.addOptionGroup(rmType);
	}

	private static void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
    	// TODO help page
    	formatter.setWidth(180);
    	formatter.printHelp("program", options);
	}
	
	private static boolean parseSkillfile() {
		String skillfile = line.getOptionValue("i");
		try {
			sf = SkillFile.open(skillfile);
			return true;
		} catch (SkillException e) {
			System.out.println("Error while parsing file: " + e.getMessage());
			return false;
		} catch (IOException e) {
			System.out.println("Error while reading file.");
			return false;
		}
	}

	private static Set<CollectionRoot> parseRoots(String[] roots) {
		Set<CollectionRoot> retval = new HashSet<>();
		for(String root : roots) {
			if(root.contains("#")) {
				String[] rootSplitted = root.split("#");
				int id = Integer.parseInt(rootSplitted[1]);
				retval.add(new CollectionRoot(rootSplitted[0], id));
			} else {
				retval.add(new CollectionRoot(root));
			}
		}
		return retval;
	}

	private static void executeGC() {
		boolean keepCollectionFields = false;
    	boolean printStatistics = false;
    	boolean printProgress = false;
    	Set<CollectionRoot> collRoots;
    	
    	if(!line.hasOption("r")) {
    		System.out.println("Please provide roots with option -r");
    		return;
    	}
    	
    	if(line.hasOption("kC")) keepCollectionFields = true;
    	if(line.hasOption("s")) printStatistics = true;
    	if(line.hasOption("p")) printProgress = true;
    	
    	String[] roots = line.getOptionValues("r");
    	collRoots = parseRoots(roots);
        
    	GarbageCollector.run(sf, collRoots, keepCollectionFields, printStatistics, printProgress);
    	
    	if(outpath != null && !line.hasOption("d"))
			try {
				sf.changePath(outpath);
			} catch (IOException e) {
				System.out.println("Error while creating outfile.");
			}
    	if(!line.hasOption("d")) sf.close();
	}
	
}
