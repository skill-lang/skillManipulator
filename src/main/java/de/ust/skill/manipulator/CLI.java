package de.ust.skill.manipulator;

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
import de.ust.skill.manipulator.gc.CollectionRoot;
import de.ust.skill.manipulator.gc.GarbageCollector;
import de.ust.skill.manipulator.internal.SkillFile;
import de.ust.skill.manipulator.specificationMapping.SpecificationMapper;
import de.ust.skill.manipulator.utils.FieldUtils;
import de.ust.skill.manipulator.utils.TypeUtils;
import de.ust.skill.parser.Parser;

public class CLI {
	private CommandLine line; 
	private SkillFile sf;
	private Path outpath = null;
	private final Options options = createOptions();
	
	private final CommandLineParser parser = new DefaultParser();
	
	private CLI(String[] args) {
		try {
		    // parse the command line arguments
		    line = parser.parse(options, args);
		}
		catch(ParseException exp) {
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
		} else if(line.hasOption("specmap")) {
			executeSpecificationMapping();
		} else if(line.hasOption("rm")) {
			executeRemoval();
		}
	}
	
	public static void main(String[] args) {
		new CLI(args);
	}

	private void executeRemoval() {
		if(line.hasOption("t")) {
			TypeUtils.deleteType(sf, line.getOptionValue("t"));
		} else if(line.hasOption("f")) {
			String field = line.getOptionValue("f");
			String[] splitted = field.split("\\.");
			if(splitted.length != 2) {
				System.out.println("Please specify field as type.field");
				return;
			}
			String fieldname = splitted[1];
			String ofType = splitted[0];
			FieldUtils.removeField(sf, fieldname, ofType);
		} else {
			System.out.println("Please provide field(-f) or type(-t) to remove.");
			return;
		}
		
		if(outpath != null)
			try {
				sf.changePath(outpath);
			} catch (IOException e) {
				System.out.println("Error while creating outfile.");
			}
    	if(!line.hasOption("d")) sf.close();
	}

	private void executeSpecificationMapping() {
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
		SkillFile newSf;
		SpecificationMapper mapper = new SpecificationMapper();
		try {
			if(line.hasOption("map")) {
				String mappingFile = line.getOptionValue("map");
				newSf = mapper.map(tc, sf, outpath, mappingFile);
			} else {
				newSf = mapper.map(tc, sf, outpath);
			}
		} catch (IOException e) {
			System.out.println("Error while creating new Skillfile.");
			return;
		} catch (InterruptedException e) {
			System.out.println("Error while transferring objects.");
			return;
		} catch (de.ust.skill.manipulator.specificationMapping.mappingfileParser.ParseException e) {
			System.out.println("Error while parsing mapping file.");
			return;
		} catch (SkillException e) {
			System.out.println("Restriction violated: " + e.getMessage());
			return;
		}
		
		if(!line.hasOption("d") && newSf != null) newSf.close();
	}

	private static Options createOptions() {
		Options options = new Options();
		
		/**
		 * Option group for mode
		 */
		OptionGroup command = new OptionGroup();
		command.setRequired(true);
		command.addOption(new Option("h", "print this help page"));
		command.addOption(new Option("gc", "gc mode"));
		command.addOption(new Option("specmap", "specification mapping mode"));
		command.addOption(new Option("rm", "remove type or field mode"));
		
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
		options.addOption(Option.builder("map")
				.hasArg()
				.argName("mapping.map")
				.desc("The optional mapping file.")
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
		
		return options;
	}

	private void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.setOptionComparator(null);

    	String header = "This tool comes with three modes:\n" +
    			"  gc: Garbage Collection\n" + 
    			"  specmap: Specification mapping\n" + 
    			"  rm: Remove field or type\n" + 
    			"Every mode MUST have an input skillfile and CAN have an outfile.\n" + 
    			"Every mode has its own options.\n\n" + 
    			"If you get an OutOfMemory-Error, try with more heap space.\n\n" + 
    			"Options: ";
    	String footer = "";
    	formatter.setWidth(100);
    	formatter.printHelp("java -jar skillManipulator.jar (-gc/-specmap/-rm) -i skillfile" + ""
    			+ " [-o outfile] [-d] (mode dependent options)", header, options, footer);
	}
	
	private boolean parseSkillfile() {
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

	private void executeGC() {
		boolean keepCollectionFields = false;
    	boolean printStatistics = false;
    	boolean printProgress = false;
    	Set<CollectionRoot> collRoots = null;
    	
    	if(!line.hasOption("r")) {
    		System.out.println("No roots specified. Result will be empty file.");
    	} else {
    		String[] roots = line.getOptionValues("r");
        	collRoots = parseRoots(roots);
    	}
    	
    	if(line.hasOption("kC")) keepCollectionFields = true;
    	if(line.hasOption("s")) printStatistics = true;
    	if(line.hasOption("p")) printProgress = true;
        
    	GarbageCollector.run(sf, collRoots, keepCollectionFields, printStatistics, printProgress);
    	
    	if(outpath != null)
			try {
				sf.changePath(outpath);
			} catch (IOException e) {
				System.out.println("Error while creating outfile.");
			}
    	if(!line.hasOption("d")) sf.close();
	}
	
}
